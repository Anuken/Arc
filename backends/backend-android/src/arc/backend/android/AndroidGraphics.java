package arc.backend.android;

import android.opengl.*;
import android.opengl.GLSurfaceView.*;
import android.util.*;
import android.view.*;
import arc.*;
import arc.Graphics.Cursor.*;
import arc.backend.android.surfaceview.*;
import arc.graphics.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.struct.*;
import arc.util.Log;
import arc.util.*;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.*;
import javax.microedition.khronos.opengles.*;

/**
 * An implementation of {@link Graphics} for Android.
 * @author mzechner
 */
@SuppressWarnings("deprecation")
public class AndroidGraphics extends Graphics implements Renderer{
    private static final String logTag = "AndroidGraphics";

    /**
     * When {@link AndroidApplication#onPause()} call
     * {@link AndroidGraphics#pause()} they <b>MUST</b> enforce continuous rendering. If not, {@link #onDrawFrame(GL10)} will not
     * be called in the GLThread while {@link #pause()} is sleeping in the Android UI Thread which will cause the
     * {@link AndroidGraphics#pause} variable never be set to false. As a result, the {@link AndroidGraphics#pause()} method will
     * kill the current process to avoid ANR
     */
    protected final AndroidApplicationConfiguration config;
    final GLSurfaceView20 view;
    protected long lastFrameTime = System.nanoTime();
    protected float deltaTime = 0;
    protected long frameStart = System.nanoTime();
    protected long frameId = -1;
    protected int frames = 0;
    protected int fps;
    int width;
    int height;
    AndroidApplication app;
    GL20 gl20;
    GL30 gl30;
    EGLContext eglContext;
    GLVersion glVersion;
    String extensions;
    boolean created = false, resumed = false, running = false;
    boolean firstResume = true;
    int[] value = new int[1];
    private float ppiX = 0;
    private float ppiY = 0;
    private float ppcX = 0;
    private float ppcY = 0;
    private float density = 1;
    private BufferFormat bufferFormat = new BufferFormat(8, 8, 8, 0, 16, 0, 0, false);
    private boolean isContinuous = true;

    public AndroidGraphics(AndroidApplication application, AndroidApplicationConfiguration config,
                           ResolutionStrategy resolutionStrategy){
        this(application, config, resolutionStrategy, true);
    }

    public AndroidGraphics(AndroidApplication application, AndroidApplicationConfiguration config,
                           ResolutionStrategy resolutionStrategy, boolean focusableView){
        this.config = config;
        this.app = application;
        view = createGLSurfaceView(application, resolutionStrategy);
        view.setPreserveEGLContextOnPause(true);
        if(focusableView){
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
        }
    }

    protected GLSurfaceView20 createGLSurfaceView(AndroidApplication application, final ResolutionStrategy resolutionStrategy){
        if(!checkGL20()) throw new ArcRuntimeException("Arc requires OpenGL ES 2.0");

        Gl.reset();
        EGLConfigChooser configChooser = getEglConfigChooser();
        GLSurfaceView20 view = new GLSurfaceView20(application, resolutionStrategy, config.useGL30 ? 3 : 2);
        if(configChooser != null)
            view.setEGLConfigChooser(configChooser);
        else
            view.setEGLConfigChooser(config.r, config.g, config.b, config.a, config.depth, config.stencil);
        view.setRenderer(this);
        return view;
    }

    protected EGLConfigChooser getEglConfigChooser(){
        return new ArcEglConfigChooser(config.r, config.g, config.b, config.a, config.depth, config.stencil, config.numSamples);
    }

    protected void updatePpi(){
        DisplayMetrics metrics = new DisplayMetrics();
        app.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        ppiX = metrics.xdpi;
        ppiY = metrics.ydpi;
        ppcX = metrics.xdpi / 2.54f;
        ppcY = metrics.ydpi / 2.54f;
        density = metrics.density;
    }

    protected boolean checkGL20(){
        EGL10 egl = (EGL10)EGLContext.getEGL();
        EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        int[] version = new int[2];
        egl.eglInitialize(display, version);

        int EGL_OPENGL_ES2_BIT = 4;
        int[] configAttribs = {EGL10.EGL_RED_SIZE, 4, EGL10.EGL_GREEN_SIZE, 4, EGL10.EGL_BLUE_SIZE, 4, EGL10.EGL_RENDERABLE_TYPE,
        EGL_OPENGL_ES2_BIT, EGL10.EGL_NONE};

        EGLConfig[] configs = new EGLConfig[10];
        int[] num_config = new int[1];
        egl.eglChooseConfig(display, configAttribs, configs, 10, num_config);
        egl.eglTerminate(display);
        return num_config[0] > 0;
    }

    /** {@inheritDoc} */
    @Override
    public GL20 getGL20(){
        return gl20;
    }

    /** {@inheritDoc} */
    @Override
    public void setGL20(GL20 gl20){
        this.gl20 = gl20;
        if(gl30 == null){
            Core.gl = gl20;
            Core.gl20 = gl20;
        }
    }

    /** {@inheritDoc} */
    @Override
    public GL30 getGL30(){
        return gl30;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public int getHeight(){
        return height;
    }

    /** {@inheritDoc} */
    @Override
    public int getWidth(){
        return width;
    }

    @Override
    public int getBackBufferWidth(){
        return width;
    }

    @Override
    public int getBackBufferHeight(){
        return height;
    }

    /** This instantiates the GL20 and GL30 instances. */
    protected void setupGL(GL10 gl){
        String versionString = gl.glGetString(GL10.GL_VERSION);
        String vendorString = gl.glGetString(GL10.GL_VENDOR);
        String rendererString = gl.glGetString(GL10.GL_RENDERER);
        glVersion = new GLVersion(Application.ApplicationType.android, versionString, vendorString, rendererString);
        if(config.useGL30 && glVersion.majorVersion > 2){
            if(gl30 != null) return;
            gl20 = gl30 = new AndroidGL30();

            Core.gl = gl30;
            Core.gl20 = gl30;
            Core.gl30 = gl30;
        }else{
            if(gl20 != null) return;
            gl20 = new AndroidGL20();

            Core.gl = gl20;
            Core.gl20 = gl20;
        }

        Log.infoTag(logTag, "OGL renderer: " + gl.glGetString(GL10.GL_RENDERER));
        Log.infoTag(logTag, "OGL vendor: " + gl.glGetString(GL10.GL_VENDOR));
        Log.infoTag(logTag, "OGL version: " + gl.glGetString(GL10.GL_VERSION));
        Log.infoTag(logTag, "OGL extensions: " + gl.glGetString(GL10.GL_EXTENSIONS));
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height){
        this.width = width;
        this.height = height;
        updatePpi();
        gl.glViewport(0, 0, this.width, this.height);
        if(!created){
            for(ApplicationListener list : app.getListeners()){
                list.init();
            }
            created = true;
            synchronized(this){
                running = true;
            }
        }

        for(ApplicationListener list : app.getListeners()){
            list.resize(width, height);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config){
        eglContext = ((EGL10)EGLContext.getEGL()).eglGetCurrentContext();
        setupGL(gl);
        logConfig(config);
        updatePpi();

        Display display = app.getWindowManager().getDefaultDisplay();
        this.width = display.getWidth();
        this.height = display.getHeight();
        this.lastFrameTime = System.nanoTime();

        gl.glViewport(0, 0, this.width, this.height);
    }

    protected void logConfig(EGLConfig config){
        EGL10 egl = (EGL10)EGLContext.getEGL();
        EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        int r = getAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0);
        int g = getAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0);
        int b = getAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0);
        int a = getAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0);
        int d = getAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0);
        int s = getAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0);
        int samples = Math.max(getAttrib(egl, display, config, EGL10.EGL_SAMPLES, 0),
        getAttrib(egl, display, config, ArcEglConfigChooser.EGL_COVERAGE_SAMPLES_NV, 0));
        boolean coverageSample = getAttrib(egl, display, config, ArcEglConfigChooser.EGL_COVERAGE_SAMPLES_NV, 0) != 0;

        Log.infoTag(logTag, "framebuffer: (" + r + ", " + g + ", " + b + ", " + a + ")");
        Log.infoTag(logTag, "depthbuffer: (" + d + ")");
        Log.infoTag(logTag, "stencilbuffer: (" + s + ")");
        Log.infoTag(logTag, "samples: (" + samples + ")");
        Log.infoTag(logTag, "coverage sampling: (" + coverageSample + ")");

        bufferFormat = new BufferFormat(r, g, b, a, d, s, samples, coverageSample);
    }

    private int getAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attrib, int defValue){
        if(egl.eglGetConfigAttrib(display, config, attrib, value)){
            return value[0];
        }
        return defValue;
    }

    void resume(){
        //do not call resume() on the first resume, which is called on application start
        if(!firstResume){
            view.onResume();
            view.queueEvent(() -> {
                running = true;
                resumed = true;
                Gl.reset();
                Seq<ApplicationListener> listeners = app.getListeners();
                synchronized(listeners){
                    for(int i = 0, n = listeners.size; i < n; ++i){
                        listeners.get(i).resume();
                    }
                }
                Log.infoTag(logTag, "[resume]");
            });
        }else{
            firstResume = false;
        }
    }

    void pause(){
        view.queueEvent(() -> {
            Seq<ApplicationListener> listeners = app.getListeners();
            synchronized(listeners){
                for(int i = 0, n = listeners.size; i < n; ++i){
                    listeners.get(i).pause();
                }
            }
            Log.infoTag(logTag, "[pause]");

            running = false;
        });
        view.onPause();
    }

    void destroy(){
        view.queueEvent(() -> {
            running = false;

            Seq<ApplicationListener> listeners = app.getListeners();
            synchronized(listeners){
                //call pause first
                for(int i = 0, n = listeners.size; i < n; ++i){
                    listeners.get(i).pause();
                }
                for(int i = 0, n = listeners.size; i < n; ++i){
                    try{
                        listeners.get(i).exit();
                        listeners.get(i).dispose();
                    }catch(Exception e){
                        //suppress dispose errors
                        Log.err(e);
                    }
                }
            }

            app.dispose();
            Log.infoTag(logTag, "[destroy]");
            //force exit to reset statics and free resources
            System.exit(0);
        });
    }

    @Override
    public void onDrawFrame(GL10 gl){
        long time = System.nanoTime();
        deltaTime = (time - lastFrameTime) / 1000000000.0f;
        lastFrameTime = time;

        //After pause deltaTime can have somewhat huge value that destabilizes the mean, so let's cut it off
        if(resumed){
            deltaTime = 0;
            resumed = false;
        }

        if(Mathf.equal(deltaTime, 0f)) deltaTime = 1f / 60f;

        if(running){
            synchronized(app.runnables){
                app.executedRunnables.clear();
                app.executedRunnables.addAll(app.runnables);
                app.runnables.clear();
            }

            for(int i = 0; i < app.executedRunnables.size; i++){
                app.executedRunnables.get(i).run();
            }

            ((AndroidInput)Core.input).processEvents();
            frameId++;
            app.defaultUpdate();

            Seq<ApplicationListener> listeners = app.getListeners();
            synchronized(listeners){
                for(int i = 0, n = listeners.size; i < n; ++i){
                    listeners.get(i).update();
                }
            }

            ((AndroidInput)Core.input).processDevices();
        }

        if(time - frameStart > 1000000000){
            fps = frames;
            frames = 0;
            frameStart = time;
        }
        frames++;
    }

    @Override
    public long getFrameId(){
        return frameId;
    }

    /** {@inheritDoc} */
    @Override
    public float getDeltaTime(){
        return deltaTime;
    }

    /** {@inheritDoc} */
    @Override
    public GLVersion getGLVersion(){
        return glVersion;
    }

    /** {@inheritDoc} */
    @Override
    public int getFramesPerSecond(){
        return fps;
    }

    public View getView(){
        return view;
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
    public boolean supportsDisplayModeChange(){
        return false;
    }

    @Override
    public boolean setFullscreenMode(DisplayMode displayMode){
        return false;
    }

    @Override
    public Monitor getPrimaryMonitor(){
        return new Monitor(0, 0, "Primary Monitor");
    }

    @Override
    public Monitor getMonitor(){
        return getPrimaryMonitor();
    }

    @Override
    public Monitor[] getMonitors(){
        return new Monitor[]{getPrimaryMonitor()};
    }

    @Override
    public DisplayMode[] getDisplayModes(Monitor monitor){
        return getDisplayModes();
    }

    @Override
    public DisplayMode getDisplayMode(Monitor monitor){
        return getDisplayMode();
    }

    @Override
    public DisplayMode[] getDisplayModes(){
        return new DisplayMode[]{getDisplayMode()};
    }

    @Override
    public boolean setWindowedMode(int width, int height){
        return false;
    }

    @Override
    public void setTitle(String title){
    }

    @Override
    public void setBorderless(boolean undecorated){
    }

    @Override
    public void setResizable(boolean resizable){

    }

    @Override
    public DisplayMode getDisplayMode(){
        DisplayMetrics metrics = new DisplayMetrics();
        app.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return new DisplayMode(metrics.widthPixels, metrics.heightPixels, 0, 0);
    }

    @Override
    public BufferFormat getBufferFormat(){
        return bufferFormat;
    }

    @Override
    public void setVSync(boolean vsync){
    }

    @Override
    public boolean supportsExtension(String extension){
        if(extensions == null) extensions = Gl.getString(GL10.GL_EXTENSIONS);
        return extensions.contains(extension);
    }

    @Override
    public boolean isContinuousRendering(){
        return isContinuous;
    }

    @Override
    public void setContinuousRendering(boolean isContinuous){
        if(view != null){
            // ignore setContinuousRendering(false) while pausing
            this.isContinuous = isContinuous;
            view.setRenderMode(this.isContinuous ? GLSurfaceView.RENDERMODE_CONTINUOUSLY : GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        }
    }

    @Override
    public void requestRendering(){
        if(view != null){
            view.requestRender();
        }
    }

    @Override
    public boolean isFullscreen(){
        return true;
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
}
