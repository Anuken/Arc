package arc.backend.robovm;

import arc.*;
import arc.Graphics.Cursor.*;
import arc.backend.robovm.custom.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import com.badlogic.gdx.backends.iosrobovm.bindings.metalangle.*;
import org.robovm.apple.coregraphics.*;
import org.robovm.apple.foundation.*;
import org.robovm.apple.glkit.*;
import org.robovm.apple.opengles.*;
import org.robovm.apple.uikit.*;
import org.robovm.objc.*;
import org.robovm.objc.annotation.*;
import org.robovm.rt.bro.annotation.*;

import java.util.*;

public class IOSGraphics extends Graphics{
    private static final String tag = "IOSGraphics";

    IOSApplication app;
    IOSInput input;
    GL20 gl20;
    GL30 gl30;
    int width;
    int height;
    long lastFrameTime;
    float deltaTime;
    long framesStart;
    int frames;
    int fps;
    BufferFormat bufferFormat;
    String extensions;
    volatile boolean resume = false;
    volatile boolean appPaused;
    IOSApplicationConfiguration config;
    MGLContext context;
    GLVersion glVersion;
    MGLKView view;
    IOSUIViewController viewController;
    boolean created = false;
    private float ppiX;
    private float ppiY;
    private float ppcX;
    private float ppcY;
    private float density;
    private long frameId = -1;
    private boolean isContinuous = true;
    private boolean isFrameRequested = true;
    private int[] insets = new int[4];

    public IOSGraphics(float scale, IOSApplication app, IOSApplicationConfiguration config, IOSInput input, boolean useGLES30){
        this.config = config;

        IOSGraphicsDelegate gdel = new IOSGraphicsDelegate();
        CGRect bounds = app.getBounds();

        // setup view and OpenGL
        width = (int)bounds.getWidth();
        height = (int)bounds.getHeight();

        if(useGLES30){
            context = new MGLContext(MGLRenderingAPI.OpenGLES3);
            gl20 = gl30 = new IOSGLES30();
        }
        if(context == null){
            context = new MGLContext(MGLRenderingAPI.OpenGLES2);
            gl20 = new IOSGLES20();
            gl30 = null;
        }

        view = new MGLKView(new CGRect(0, 0, bounds.getWidth(), bounds.getHeight()), context){
            @Method(selector = "touchesBegan:withEvent:")
            public void touchesBegan(@Pointer long touches, UIEvent event){
                IOSGraphics.this.input.onTouch(touches);
            }

            @Method(selector = "touchesCancelled:withEvent:")
            public void touchesCancelled(@Pointer long touches, UIEvent event){
                IOSGraphics.this.input.onTouch(touches);
            }

            @Method(selector = "touchesEnded:withEvent:")
            public void touchesEnded(@Pointer long touches, UIEvent event){
                IOSGraphics.this.input.onTouch(touches);
            }

            @Method(selector = "touchesMoved:withEvent:")
            public void touchesMoved(@Pointer long touches, UIEvent event){
                IOSGraphics.this.input.onTouch(touches);
            }

            @Override
            public void draw(CGRect rect){
                IOSGraphics.this.draw(this, rect);
            }

        };
        view.setDelegate(gdel);
        view.setDrawableColorFormat(config.colorFormat);
        view.setDrawableDepthFormat(config.depthFormat);
        view.setDrawableStencilFormat(config.stencilFormat);
        view.setDrawableMultisample(config.multisample);
        view.setMultipleTouchEnabled(true);

        viewController = new IOSUIViewController(app, this);
        viewController.setView(view);
        viewController.setDelegate(gdel);
        viewController.setPreferredFramesPerSecond(config.preferredFramesPerSecond);

        this.app = app;
        this.input = input;

        int r, g, b, a, depth, stencil = 0, samples = 0;
        if(config.colorFormat == MGLDrawableColorFormat.RGB565){
            r = 5;
            g = 6;
            b = 5;
            a = 0;
        }else{
            r = g = b = a = 8;
        }
        if(config.depthFormat == MGLDrawableDepthFormat._16){
            depth = 16;
        }else if(config.depthFormat == MGLDrawableDepthFormat._24){
            depth = 24;
        }else{
            depth = 0;
        }
        if(config.stencilFormat == MGLDrawableStencilFormat._8){
            stencil = 8;
        }
        if(config.multisample == MGLDrawableMultisample._4X){
            samples = 4;
        }
        bufferFormat = new BufferFormat(r, g, b, a, depth, stencil, samples, false);

        String machineString = HWMachine.getMachineString();
        IOSDevice device = IOSDevice.getDevice(machineString);
        if(device == null){
            Log.err(tag, "Machine ID: " + machineString + " not found, please report!");
        }else{
            Log.info(tag, "Device: " + device.classifier);
        }

        int ppi =
            device != null ? device.ppi :
            UIDevice.getCurrentDevice().getUserInterfaceIdiom() == UIUserInterfaceIdiom.Pad ? 264 :
            163;

        density = device != null ? device.ppi / 160f : scale;
        ppiX = ppi;
        ppiY = ppi;
        ppcX = ppiX / 2.54f;
        ppcY = ppiY / 2.54f;
        Log.info(tag, "Display: ppi=" + ppi + ", density=" + density);

        // time + FPS
        lastFrameTime = System.nanoTime();
        framesStart = lastFrameTime;

        appPaused = false;
    }

    public void resume(){
        if(!appPaused) return;
        appPaused = false;

        Seq<ApplicationListener> listeners = app.listeners;
        synchronized(listeners){
            for(ApplicationListener listener : listeners){
                listener.resume();
            }
        }
        resume = true;
    }

    public void pause(){
        if(appPaused) return;
        appPaused = true;

        Seq<ApplicationListener> listeners = app.listeners;
        synchronized(listeners){
            for(ApplicationListener listener : listeners){
                listener.pause();
            }
        }
    }

    public void draw(MGLKView view, CGRect rect){
        makeCurrent();
        // massive hack, MGLKView resets the viewport on each draw call, so IOSGLES20
        // stores the last known viewport and we reset it here...
        gl20.glViewport(IOSGLES20.x, IOSGLES20.y, IOSGLES20.width, IOSGLES20.height);

        if(!created){
            app.mainThread = Thread.currentThread();
            gl20.glViewport(0, 0, width, height);

            String versionString = gl20.glGetString(GL20.GL_VERSION);
            String vendorString = gl20.glGetString(GL20.GL_VENDOR);
            String rendererString = gl20.glGetString(GL20.GL_RENDERER);
            glVersion = new GLVersion(Application.ApplicationType.iOS, versionString, vendorString, rendererString);

            updateSafeInsets();
            for(ApplicationListener listener : app.listeners){
                listener.init();
                listener.resize(width, height);
            }
            created = true;
        }
        if(appPaused){
            return;
        }

        long time = System.nanoTime();
        if(!resume){
            deltaTime = (time - lastFrameTime) / 1000000000.0f;
        }else{
            resume = false;
            deltaTime = 0;
        }
        if(Mathf.equal(deltaTime, 0f)) deltaTime = 1f / 60f;
        lastFrameTime = time;

        frames++;
        if(time - framesStart >= 1000000000L){
            framesStart = time;
            fps = frames;
            frames = 0;
        }

        input.processEvents();
        frameId++;
        app.defaultUpdate();
        runProtected(() -> {
            for(ApplicationListener listener : app.listeners){
                listener.update();
            }
        });
        input.processDevices();
    }

    void makeCurrent(){
        MGLContext.setCurrentContext(context);
    }

    public void update(MGLKViewController controller){
        makeCurrent();
        runProtected(app::processRunnables);
        // pause the MGLKViewController render loop if we are no longer continuous
        // and if we haven't requested a frame in the last loop iteration
        if(!isContinuous && !isFrameRequested){
            viewController.setPaused(true);
        }
        isFrameRequested = false;
    }

    private void runProtected(Runnable run){
        try{
            run.run();
        }catch(Throwable t){
            if(config.errorHandler != null){
                app.getListeners().clear();
                app.executedRunnables.clear();
                app.runnables.clear();
                Cons<Throwable> handler = config.errorHandler;
                config.errorHandler = null;
                handler.get(t);
            }else{
                throw new RuntimeException(t);
            }
        }
    }

    protected void updateSafeInsets() {
        if(Foundation.getMajorSystemVersion() >= 11){
            UIEdgeInsets edgeInsets = viewController.getView().getSafeAreaInsets();
            insets[0] = (int)(edgeInsets.getLeft() * view.getContentScaleFactor());
            insets[1] = (int)(edgeInsets.getRight() * view.getContentScaleFactor());
            insets[2] = (int)(edgeInsets.getTop() * view.getContentScaleFactor());
            insets[3] = (int)(edgeInsets.getBottom() * view.getContentScaleFactor());

            Log.info("[IOSApplication] Insets: @", Arrays.toString(insets));
        }
    }

    public void willPause(MGLKViewController controller, boolean pause){ }

    @Override
    public int[] getSafeInsets(){
        return insets;
    }

    @Override
    public GL20 getGL20(){
        return gl20;
    }

    @Override
    public void setGL20(GL20 gl20){
        this.gl20 = gl20;
        if(gl30 == null){
            Core.gl = gl20;
            Core.gl20 = gl20;
        }
    }

    @Override
    public boolean isGL30Available(){
        return gl30 != null;
    }

    @Override
    public GL30 getGL30(){
        return gl30;
    }

    @Override
    public void setGL30(GL30 gl30){
        this.gl30 = gl30;
        if(gl30 != null){
            this.gl20 = gl30;

            Core.gl = gl20;
            Core.gl20 = gl20;
            Core.gl30 = gl30;
        }
    }

    @Override
    public int getWidth(){
        return width;
    }

    @Override
    public int getHeight(){
        return height;
    }

    @Override
    public int getBackBufferWidth(){
        return width;
    }

    @Override
    public int getBackBufferHeight(){
        return height;
    }

    @Override
    public float getDeltaTime(){
        return deltaTime;
    }

    @Override
    public int getFramesPerSecond(){
        return fps;
    }

    @Override
    public GLVersion getGLVersion(){
        return glVersion;
    }

    @Override
    public float getPpiX(){
        return ppiX;
    }

    @Override
    public float getPpiY(){
        return ppiY;
    }

    @Override
    public float getPpcX(){
        return ppcX;
    }

    @Override
    public float getPpcY(){
        return ppcY;
    }

    @Override
    public float getDensity(){
        return density;
    }

    @Override
    public boolean setWindowedMode(int width, int height){
        return false;
    }

    @Override
    public void setTitle(String title){
    }

    @Override
    public void setVSync(boolean vsync){
    }

    @Override
    public BufferFormat getBufferFormat(){
        return bufferFormat;
    }

    @Override
    public boolean supportsExtension(String extension){
        if(extensions == null) extensions = Gl.getString(GL20.GL_EXTENSIONS);
        return extensions.contains(extension);
    }

    @Override
    public boolean isContinuousRendering(){
        return isContinuous;
    }

    @Override
    public void setContinuousRendering(boolean isContinuous){
        if(isContinuous != this.isContinuous){
            this.isContinuous = isContinuous;
            // start the MGLKViewController if we go from non-continuous -> continuous
            if(isContinuous) viewController.setPaused(false);
        }
    }

    @Override
    public void requestRendering(){
        isFrameRequested = true;
        // start the MGLKViewController if we are in non-continuous mode
        // (we should already be started in continuous mode)
        if(!isContinuous) viewController.setPaused(false);
    }

    @Override
    public boolean isFullscreen(){
        return true;
    }

    @Override
    public long getFrameId(){
        return frameId;
    }

    @Override
    public Cursor newCursor(Pixmap pixmap, int xHotspot, int yHotspot){
        return null;
    }

    @Override
    public void setCursor(Cursor cursor){
    }

    @Override
    public void setSystemCursor(SystemCursor systemCursor){
    }

    static class IOSUIViewController extends MGLKViewController{
        final IOSApplication app;
        final IOSGraphics graphics;
        boolean created = false;

        IOSUIViewController(IOSApplication app, IOSGraphics graphics){
            this.app = app;
            this.graphics = graphics;
        }

        @Callback
        @BindSelector("shouldAutorotateToInterfaceOrientation:")
        private static boolean shouldAutorotateToInterfaceOrientation(IOSUIViewController self, Selector sel,
                                                                      UIInterfaceOrientation orientation){
            return self.shouldAutorotateToInterfaceOrientation(orientation);
        }

        @Override
        public void viewWillAppear(boolean arg0){
            super.viewWillAppear(arg0);
            // start MGLKViewController even though we may only draw a single frame
            // (we may be in non-continuous mode)
            setPaused(false);
        }

        @Override
        public void viewDidAppear(boolean animated){
            if(app.viewControllerListener != null) app.viewControllerListener.viewDidAppear(animated);
        }

        @Override
        public UIInterfaceOrientationMask getSupportedInterfaceOrientations(){
            long mask = 0;
            if(app.config.orientationLandscape){
                mask |= ((1 << UIInterfaceOrientation.LandscapeLeft.value()) | (1 << UIInterfaceOrientation.LandscapeRight.value()));
            }
            if(app.config.orientationPortrait){
                mask |= ((1 << UIInterfaceOrientation.Portrait.value()) | (1 << UIInterfaceOrientation.PortraitUpsideDown.value()));
            }
            return new UIInterfaceOrientationMask(mask);
        }

        @Override
        public boolean shouldAutorotate(){
            return true;
        }

        public boolean shouldAutorotateToInterfaceOrientation(UIInterfaceOrientation orientation){
            // we return "true" if we support the orientation
            switch(orientation){
                case LandscapeLeft:
                case LandscapeRight:
                    return app.config.orientationLandscape;
                default:
                    // assume portrait
                    return app.config.orientationPortrait;
            }
        }

        @Override
        public UIRectEdge getPreferredScreenEdgesDeferringSystemGestures(){
            return app.config.screenEdgesDeferringSystemGestures;
        }

        @Override
        public void viewDidLayoutSubviews(){
            super.viewDidLayoutSubviews();
            // get the view size and update graphics
            CGRect bounds = app.getBounds();
            graphics.width = (int)bounds.getWidth();
            graphics.height = (int)bounds.getHeight();
            graphics.makeCurrent();
            graphics.updateSafeInsets();
            if(graphics.created){
                for(ApplicationListener list : app.listeners){
                    list.resize(graphics.width, graphics.height);
                }
            }
        }

        @Override
        public boolean prefersHomeIndicatorAutoHidden(){
            return app.config.hideHomeIndicator;
        }

        @Override
        public void pressesBegan(NSSet<UIPress> presses, UIPressesEvent event){
            if(presses == null || presses.isEmpty() || !app.input.onKey(presses.getValues().first().getKey(), true)){
                super.pressesBegan(presses, event);
            }
        }

        @Override
        public void pressesEnded(NSSet<UIPress> presses, UIPressesEvent event){
            if(presses == null || presses.isEmpty() || !app.input.onKey(presses.getValues().first().getKey(), false)){
                super.pressesEnded(presses, event);
            }
        }
    }

    class IOSGraphicsDelegate extends NSObject implements MGLKViewDelegate, MGLKViewControllerDelegate{
        @Override
        public void update(MGLKViewController MGLKViewController){
            IOSGraphics.this.update(MGLKViewController);
        }

        @Override
        public void draw(MGLKView MGLKView, CGRect cgRect){
            IOSGraphics.this.draw(MGLKView, cgRect);
        }
    }

    static class IOSUIView extends MGLKView{

        public IOSUIView(CGRect frame, MGLContext context){
            super(frame, context);
        }
    }
}
