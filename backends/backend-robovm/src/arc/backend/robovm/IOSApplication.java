package arc.backend.robovm;

import arc.*;
import arc.audio.*;
import arc.backend.robovm.custom.*;
import arc.graphics.*;
import arc.struct.*;
import arc.util.*;
import org.robovm.apple.coregraphics.*;
import org.robovm.apple.dispatch.*;
import org.robovm.apple.foundation.*;
import org.robovm.apple.uikit.*;
import org.robovm.rt.bro.*;

import static org.robovm.apple.foundation.NSPathUtilities.*;

public class IOSApplication implements Application{
    UIApplication uiApp;
    UIWindow uiWindow;
    IOSViewControllerListener viewControllerListener;
    IOSApplicationConfiguration config;
    IOSGraphics graphics;
    IOSInput input;
    Thread mainThread;
    @Nullable IOSDevice device;
    float pixelsPerPoint;

    private IOSScreenBounds lastScreenBounds = null;

    final Seq<ApplicationListener> listeners = new Seq<>();
    final Seq<Runnable> runnables = new Seq<>(), executedRunnables = new Seq<>();

    public IOSApplication(ApplicationListener listener, IOSApplicationConfiguration config){
        addListener(listener);
        this.config = config;
    }

    final boolean didFinishLaunching(UIApplication uiApp, UIApplicationLaunchOptions options){
        Core.app = this;
        this.uiApp = uiApp;

        // enable or disable screen dimming
        UIApplication.getSharedApplication().setIdleTimerDisabled(config.preventScreenDimming);

        Log.info("[IOSApplication] iOS version: " + UIDevice.getCurrentDevice().getSystemVersion());
        Log.info("[IOSApplication] Running in " + (Bro.IS_64BIT ? "64-bit" : "32-bit") + " mode");

        pixelsPerPoint = (float)UIScreen.getMainScreen().getNativeScale();

        this.uiWindow = new UIWindow(UIScreen.getMainScreen().getBounds());
        this.uiWindow.makeKeyAndVisible();

        this.input = new IOSInput(this);
        this.graphics = new IOSGraphics(this, config, input, config.useGL30);
        Core.gl = Core.gl20 = graphics.gl20;
        Core.gl30 = graphics.gl30;
        Core.audio = new Audio();
        Core.settings = new Settings();
        Core.files = new IOSFiles();
        Core.graphics = this.graphics;
        Core.input = this.input;

        device = IOSDevice.getDevice(HWMachine.getMachineString());

        this.uiWindow.setRootViewController(this.graphics.viewController);
        this.input.setupPeripherals();
        this.graphics.updateSafeInsets();
        Log.info("[IOSApplication] created");
        // Trigger first render, special case that is caught and returned
        this.graphics.view.display();
        for(ApplicationListener list : listeners){
            list.init();
        }
        for(ApplicationListener list : listeners){
            list.resize(graphics.getWidth(), graphics.getHeight());
        }
        // make sure the OpenGL view has contents before displaying it
        this.graphics.view.display();
        return true;
    }

    /**
     * Return the UI view controller of IOSApplication
     * @return the view controller of IOSApplication
     */
    public UIViewController getUIViewController(){
        return graphics.viewController;
    }

    /**
     * Return the UI Window of IOSApplication
     * @return the window
     */
    public UIWindow getUIWindow(){
        return uiWindow;
    }

    /**
     * GL View spans whole screen, that is, even under the status bar. iOS can also rotate the screen, which is not handled
     * consistently over iOS versions. This method returns, in pixels, rectangle in which Arc draws.
     * @return dimensions of space we draw to, adjusted for device orientation
     */
    protected IOSScreenBounds computeBounds(){
        CGRect screenBounds = uiWindow.getBounds();
        final CGRect statusBarFrame = uiApp.getStatusBarFrame();
        double statusBarHeight = statusBarFrame.getHeight();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();
        if(statusBarHeight != 0.0){
            Log.debug("IOSApplication", "Status bar is visible (height = " + statusBarHeight + ")");
            screenHeight -= statusBarHeight;
        }else{
            Log.debug("IOSApplication", "Status bar is not visible");
        }
        int offsetX = 0;
        int offsetY = (int)Math.round(statusBarHeight);
        int width = (int)Math.round(screenWidth);
        int height = (int)Math.round(screenHeight);
        int backBufferWidth = (int)Math.round(screenWidth * pixelsPerPoint);
        int backBufferHeight = (int)Math.round(screenHeight * pixelsPerPoint);
        Log.debug("IOSApplication", "Computed bounds are x=" + offsetX + " y=" + offsetY + " w=" + width + " h=" + height + " bbW= "
        + backBufferWidth + " bbH= " + backBufferHeight);
        return lastScreenBounds = new IOSScreenBounds(offsetX, offsetY, width, height, backBufferWidth, backBufferHeight);
    }

    /** @return area of screen in UIKit points on which Arc draws, with 0,0 being upper left corner */
    public IOSScreenBounds getScreenBounds () {
        return lastScreenBounds == null ? computeBounds() : lastScreenBounds;
    }

    /** Returns device ppi using a best guess approach when device is unknown. Overwrite to customize strategy. */
    protected int guessUnknownPpi () {
        return UIDevice.getCurrentDevice().getUserInterfaceIdiom() == UIUserInterfaceIdiom.Pad ?
            132 * (int)pixelsPerPoint : 164 * (int)pixelsPerPoint;
    }

    final void didBecomeActive(UIApplication uiApp){
        Log.info("[IOSApplication] resumed");
        graphics.makeCurrent();
        graphics.resume();
    }

    final void willEnterForeground(UIApplication uiApp){
    }

    final void willResignActive(UIApplication uiApp){
        Log.info("[IOSApplication] paused");
        graphics.makeCurrent();
        graphics.pause();
        Gl.finish();
    }

    final void willTerminate(UIApplication uiApp){
        Log.info("[IOSApplication] disposed");
        graphics.makeCurrent();
        Seq<ApplicationListener> listeners = this.listeners;
        synchronized(listeners){
            for(ApplicationListener listener : listeners){
                listener.pause();
            }
            for(ApplicationListener listener : listeners){
                listener.exit();
            }
        }
        Gl.finish();
    }

    @Override
    public Thread getMainThread(){
        return mainThread;
    }

    @Override
    public ApplicationType getType(){
        return ApplicationType.iOS;
    }

    @Override
    public int getVersion(){
        return (int)NSProcessInfo.getSharedProcessInfo().getOperatingSystemVersion().getMajorVersion();
    }

    @Override
    public boolean openURI(String URI){
        NSURL url = new NSURL(URI);
        if(uiApp.canOpenURL(url)){
            try{
                DispatchQueue.getMainQueue().async(() -> {
                    uiApp.openURL(url, new UIApplicationOpenURLOptions(), null);
                });
                return true;
            }catch(Throwable t){
                t.printStackTrace();
                return false;
            }
        }
        return false;
    }

    @Override
    public void post(Runnable runnable){
        synchronized(runnables){
            runnables.add(runnable);
            Core.graphics.requestRendering();
        }
    }

    public void processRunnables(){
        synchronized(runnables){
            executedRunnables.clear();
            executedRunnables.addAll(runnables);
            runnables.clear();
        }
        for(int i = 0; i < executedRunnables.size; i++){
            executedRunnables.get(i).run();
        }
    }

    @Override
    public void exit(){
        NSThread.exit();
    }

    @Override
    public String getClipboardText(){
        return UIPasteboard.getGeneralPasteboard().getString();
    }

    @Override
    public void setClipboardText(String text){
        UIPasteboard.getGeneralPasteboard().setString(text);
    }

    @Override
    public Seq<ApplicationListener> getListeners(){
        return listeners;
    }

    /**
     * Add a listener to handle events from the root view controller
     * @param listener The {#link IOSViewControllerListener} to add
     */
    public void addViewControllerListener(IOSViewControllerListener listener){
        viewControllerListener = listener;
    }

    public static abstract class Delegate extends UIApplicationDelegateAdapter{
        private IOSApplication app;

        protected abstract IOSApplication createApplication();

        @Override
        public boolean didFinishLaunching(UIApplication application, UIApplicationLaunchOptions options){
            application.addStrongRef(this); // Prevent this from being GCed until the ObjC UIApplication is deallocated
            this.app = createApplication();

            boolean result = app.didFinishLaunching(application, options);
            if(options != null && options.has(UIApplicationLaunchOptions.Keys.URL())){
                openURL(((NSURL)options.get(UIApplicationLaunchOptions.Keys.URL())));
            }
            return result;
        }

        @Override
        public void didBecomeActive(UIApplication application){
            app.didBecomeActive(application);
        }

        @Override
        public void willEnterForeground(UIApplication application){
            app.willEnterForeground(application);
        }

        @Override
        public void willResignActive(UIApplication application){
            app.willResignActive(application);
        }

        @Override
        public void willTerminate(UIApplication application){
            app.willTerminate(application);
        }

        @Override
        public boolean openURL(UIApplication app, NSURL url, UIApplicationOpenURLOptions options) {
            openURL(url);
            return false;
        }

        void openURL(NSURL url){
            if(Core.app == null) return;
            Core.app.post(() -> {
                for(ApplicationListener list : Core.app.getListeners()){
                    list.fileDropped(Core.files.absolute(getDocumentsDirectory()).child(url.getLastPathComponent()));
                }
            });
        }
    }
}
