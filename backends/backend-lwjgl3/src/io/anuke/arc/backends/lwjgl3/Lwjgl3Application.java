package io.anuke.arc.backends.lwjgl3;

import io.anuke.arc.*;
import io.anuke.arc.backends.lwjgl3.audio.OpenALAudio;
import io.anuke.arc.backends.lwjgl3.audio.mock.MockAudio;
import io.anuke.arc.collection.Array;
import io.anuke.arc.graphics.glutils.GLVersion;
import io.anuke.arc.util.ArcRuntimeException;
import io.anuke.arc.util.Clipboard;
import io.anuke.arc.util.Log;
import io.anuke.arc.util.SharedLibraryLoader;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.*;
import org.lwjgl.system.Callback;

import java.io.PrintStream;
import java.nio.IntBuffer;

public class Lwjgl3Application implements Application{
    private static GLFWErrorCallback errorCallback;
    private static GLVersion glVersion;
    private static Callback glDebugCallback;
    private final Lwjgl3ApplicationConfiguration config;
    private final Array<Lwjgl3Window> windows = new Array<>();
    private final Lwjgl3Clipboard clipboard;
    private final Array<ApplicationListener> listeners = new Array<>();
    private final Array<Runnable> runnables = new Array<>();
    private final Array<Runnable> executedRunnables = new Array<>();
    private Audio audio;
    private volatile boolean running = true;

    public Lwjgl3Application(ApplicationListener listener, Lwjgl3ApplicationConfiguration config){
        initializeGlfw();
        this.config = Lwjgl3ApplicationConfiguration.copy(config);
        if(this.config.title == null) this.config.title = listener.getClass().getSimpleName();
        Core.app = this;
        if(!config.disableAudio){
            try{
                this.audio = Core.audio = new OpenALAudio(config.audioDeviceSimultaneousSources,
                config.audioDeviceBufferCount, config.audioDeviceBufferSize);
            }catch(Throwable t){
                Log.err("[Lwjgl3Application] Couldn't initialize audio, disabling audio", t);
                this.audio = Core.audio = new MockAudio();
            }
        }else{
            this.audio = Core.audio = new MockAudio();
        }

        Core.files = new Lwjgl3Files();
        Core.net = new Lwjgl3Net();
        Core.settings = new Settings();
        this.clipboard = new Lwjgl3Clipboard();

        Lwjgl3Window window = createWindow(config, listener, 0);
        windows.add(window);
        try{
            loop();
            cleanupWindows();
        }catch(Throwable t){
            if(t instanceof RuntimeException)
                throw (RuntimeException)t;
            else
                throw new ArcRuntimeException(t);
        }finally{
            cleanup();
        }
    }

    static void initializeGlfw(){
        if(errorCallback == null){
            Lwjgl3NativesLoader.load();
            errorCallback = GLFWErrorCallback.createPrint(System.err);
            GLFW.glfwSetErrorCallback(errorCallback);
            GLFW.glfwInitHint(GLFW.GLFW_JOYSTICK_HAT_BUTTONS, GLFW.GLFW_FALSE);
            if(!GLFW.glfwInit()){
                throw new ArcRuntimeException("Unable to initialize GLFW");
            }
        }
    }

    static long createGlfwWindow(Lwjgl3ApplicationConfiguration config, long sharedContextWindow){
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, config.windowResizable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_MAXIMIZED, config.windowMaximized ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_AUTO_ICONIFY, config.autoIconify ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);

        if(sharedContextWindow == 0){
            GLFW.glfwWindowHint(GLFW.GLFW_RED_BITS, config.r);
            GLFW.glfwWindowHint(GLFW.GLFW_GREEN_BITS, config.g);
            GLFW.glfwWindowHint(GLFW.GLFW_BLUE_BITS, config.b);
            GLFW.glfwWindowHint(GLFW.GLFW_ALPHA_BITS, config.a);
            GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, config.stencil);
            GLFW.glfwWindowHint(GLFW.GLFW_DEPTH_BITS, config.depth);
            GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, config.samples);
        }

        if(config.useGL30){
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, config.gles30ContextMajorVersion);
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, config.gles30ContextMinorVersion);
            if(SharedLibraryLoader.isMac){
                // hints mandatory on OS X for GL 3.2+ context creation, but fail on Windows if the
                // WGL_ARB_create_context extension is not available
                // see: http://www.glfw.org/docs/latest/compat.html
                GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
                GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
            }
        }

        if(config.transparentFramebuffer){
            GLFW.glfwWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, GLFW.GLFW_TRUE);
        }

        if(config.debug){
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE);
        }

        long windowHandle;

        if(config.fullscreenMode != null){
            GLFW.glfwWindowHint(GLFW.GLFW_REFRESH_RATE, config.fullscreenMode.refreshRate);
            windowHandle = GLFW.glfwCreateWindow(config.fullscreenMode.width, config.fullscreenMode.height, config.title, config.fullscreenMode.getMonitor(), sharedContextWindow);
        }else{
            GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, config.windowDecorated ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
            windowHandle = GLFW.glfwCreateWindow(config.windowWidth, config.windowHeight, config.title, 0, sharedContextWindow);
        }
        if(windowHandle == 0){
            throw new ArcRuntimeException("Couldn't create window");
        }
        Lwjgl3Window.setSizeLimits(windowHandle, config.windowMinWidth, config.windowMinHeight, config.windowMaxWidth, config.windowMaxHeight);
        if(config.fullscreenMode == null && !config.windowMaximized){
            if(config.windowX == -1 && config.windowY == -1){
                int windowWidth = Math.max(config.windowWidth, config.windowMinWidth);
                int windowHeight = Math.max(config.windowHeight, config.windowMinHeight);
                if(config.windowMaxWidth > -1) windowWidth = Math.min(windowWidth, config.windowMaxWidth);
                if(config.windowMaxHeight > -1) windowHeight = Math.min(windowHeight, config.windowMaxHeight);
                GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
                GLFW.glfwSetWindowPos(windowHandle, vidMode.width() / 2 - windowWidth / 2, vidMode.height() / 2 - windowHeight / 2);
            }else{
                GLFW.glfwSetWindowPos(windowHandle, config.windowX, config.windowY);
            }
        }
        if(config.windowIconPaths != null){
            Lwjgl3Window.setIcon(windowHandle, config.windowIconPaths, config.windowIconFileType);
        }
        GLFW.glfwMakeContextCurrent(windowHandle);
        GLFW.glfwSwapInterval(config.vSyncEnabled ? 1 : 0);
        GL.createCapabilities();

        initiateGL();
        if(!glVersion.isVersionEqualToOrHigher(2, 0))
            throw new ArcRuntimeException("OpenGL 2.0 or higher with the FBO extension is required. OpenGL version: "
            + GL11.glGetString(GL11.GL_VERSION) + "\n" + glVersion.getDebugVersionString());

        if(!supportsFBO()){
            throw new ArcRuntimeException("OpenGL 2.0 or higher with the FBO extension is required. OpenGL version: "
            + GL11.glGetString(GL11.GL_VERSION) + ", FBO extension: false\n" + glVersion.getDebugVersionString());
        }

        if(config.debug){
            glDebugCallback = GLUtil.setupDebugMessageCallback(config.debugStream);
            setGLDebugMessageControl(GLDebugMessageSeverity.NOTIFICATION, false);
        }

        return windowHandle;
    }

    private static void initiateGL(){
        String versionString = GL11.glGetString(GL11.GL_VERSION);
        String vendorString = GL11.glGetString(GL11.GL_VENDOR);
        String rendererString = GL11.glGetString(GL11.GL_RENDERER);
        glVersion = new GLVersion(Application.ApplicationType.Desktop, versionString, vendorString, rendererString);
    }

    private static boolean supportsFBO(){
        // FBO is in core since OpenGL 3.0, see https://www.opengl.org/wiki/Framebuffer_Object
        return glVersion.isVersionEqualToOrHigher(3, 0) || GLFW.glfwExtensionSupported("GL_EXT_framebuffer_object")
        || GLFW.glfwExtensionSupported("GL_ARB_framebuffer_object");
    }

    /**
     * Enables or disables GL debug messages for the specified severity level. Returns false if the severity
     * level could not be set (e.g. the NOTIFICATION level is not supported by the ARB and AMD extensions).
     * <p>
     * See {@link Lwjgl3ApplicationConfiguration#enableGLDebugOutput(boolean, PrintStream)}
     */
    public static boolean setGLDebugMessageControl(GLDebugMessageSeverity severity, boolean enabled){
        GLCapabilities caps = GL.getCapabilities();
        final int GL_DONT_CARE = 0x1100; // not defined anywhere yet

        if(caps.OpenGL43){
            GL43.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, severity.gl43, (IntBuffer)null, enabled);
            return true;
        }

        if(caps.GL_KHR_debug){
            KHRDebug.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, severity.khr, (IntBuffer)null, enabled);
            return true;
        }

        if(caps.GL_ARB_debug_output && severity.arb != -1){
            ARBDebugOutput.glDebugMessageControlARB(GL_DONT_CARE, GL_DONT_CARE, severity.arb, (IntBuffer)null, enabled);
            return true;
        }

        if(caps.GL_AMD_debug_output && severity.amd != -1){
            AMDDebugOutput.glDebugMessageEnableAMD(GL_DONT_CARE, severity.amd, (IntBuffer)null, enabled);
            return true;
        }

        return false;
    }

    private void loop(){
        Array<Lwjgl3Window> closedWindows = new Array<Lwjgl3Window>();
        while(running && windows.size > 0){
            // FIXME put it on a separate thread
            if(audio instanceof OpenALAudio){
                ((OpenALAudio)audio).update();
            }

            boolean haveWindowsRendered = false;
            closedWindows.clear();
            for(Lwjgl3Window window : windows){
                window.makeCurrent();
                synchronized(listeners){
                    haveWindowsRendered |= window.update();
                }
                if(window.shouldClose()){
                    closedWindows.add(window);
                }
            }
            for(ApplicationListener listener : listeners){
                listener.update();
            }
            GLFW.glfwPollEvents();

            boolean shouldRequestRendering;
            synchronized(runnables){
                shouldRequestRendering = runnables.size > 0;
                executedRunnables.clear();
                executedRunnables.addAll(runnables);
                runnables.clear();
            }
            for(Runnable runnable : executedRunnables){
                runnable.run();
            }
            if(shouldRequestRendering){
                // Must follow Runnables execution so changes done by Runnables are reflected
                // in the following render.
                for(Lwjgl3Window window : windows){
                    if(!window.getGraphics().isContinuousRendering())
                        window.requestRendering();
                }
            }

            for(Lwjgl3Window closedWindow : closedWindows){
                if(windows.size == 1){
                    // Lifecycle listener methods have to be called before ApplicationListener methods. The
                    // application will be disposed when _all_ windows have been disposed, which is the case,
                    // when there is only 1 window left, which is in the process of being disposed.
                    for(int i = listeners.size - 1; i >= 0; i--){
                        ApplicationListener l = listeners.get(i);
                        l.pause();
                        l.dispose();
                    }
                    listeners.clear();
                }
                closedWindow.dispose();

                windows.remove(closedWindow);
            }

            if(!haveWindowsRendered){
                // Sleep a few milliseconds in case no rendering was requested
                // with continuous rendering disabled.
                try{
                    Thread.sleep(1000 / config.idleFPS);
                }catch(InterruptedException e){
                    // ignore
                }
            }
        }
    }

    private void cleanupWindows(){
        synchronized(listeners){
            for(ApplicationListener lifecycleListener : listeners){
                lifecycleListener.pause();
                lifecycleListener.dispose();
            }
        }
        for(Lwjgl3Window window : windows){
            window.dispose();
        }
        windows.clear();
    }

    private void cleanup(){
        dispose();
        Lwjgl3Cursor.disposeSystemCursors();
        if(audio instanceof OpenALAudio){
            ((OpenALAudio)audio).dispose();
        }
        errorCallback.free();
        errorCallback = null;
        if(glDebugCallback != null){
            glDebugCallback.free();
            glDebugCallback = null;
        }
        GLFW.glfwTerminate();
    }

    @Override
    public ApplicationType getType(){
        return ApplicationType.Desktop;
    }

    @Override
    public int getVersion(){
        return 0;
    }

    @Override
    public long getJavaHeap(){
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    @Override
    public long getNativeHeap(){
        return getJavaHeap();
    }

    @Override
    public Clipboard getClipboard(){
        return clipboard;
    }

    @Override
    public void post(Runnable runnable){
        synchronized(runnables){
            runnables.add(runnable);
        }
    }

    @Override
    public Array<ApplicationListener> getListeners(){
        return listeners;
    }

    @Override
    public void exit(){
        running = false;
    }

    /**
     * Creates a new {@link Lwjgl3Window} using the provided listener and {@link Lwjgl3WindowConfiguration}.
     * <p>
     * This function only just instantiates a {@link Lwjgl3Window} and returns immediately. The actual window creation
     * is postponed with {@link Application#post(Runnable)} until after all existing windows are updated.
     */
    public Lwjgl3Window newWindow(ApplicationListener listener, Lwjgl3WindowConfiguration config){
        Lwjgl3ApplicationConfiguration appConfig = Lwjgl3ApplicationConfiguration.copy(this.config);
        appConfig.setWindowConfiguration(config);
        return createWindow(appConfig, listener, windows.get(0).getWindowHandle());
    }

    private Lwjgl3Window createWindow(final Lwjgl3ApplicationConfiguration config, ApplicationListener listener,
                                      final long sharedContext){
        final Lwjgl3Window window = new Lwjgl3Window(listener, config);
        if(sharedContext == 0){
            // the main window is created immediately
            createWindow(window, config, sharedContext);
        }else{
            // creation of additional windows is deferred to avoid GL context trouble
            post(() -> {
                createWindow(window, config, sharedContext);
                windows.add(window);
            });
        }
        return window;
    }

    private void createWindow(Lwjgl3Window window, Lwjgl3ApplicationConfiguration config, long sharedContext){
        long windowHandle = createGlfwWindow(config, sharedContext);
        window.create(windowHandle);
        window.setVisible(config.initialVisible);

        for(int i = 0; i < 2; i++){
            GL11.glClearColor(config.initialBackgroundColor.r, config.initialBackgroundColor.g, config.initialBackgroundColor.b,
            config.initialBackgroundColor.a);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
            GLFW.glfwSwapBuffers(windowHandle);
        }
    }

    public enum GLDebugMessageSeverity{
        HIGH(
        GL43.GL_DEBUG_SEVERITY_HIGH,
        KHRDebug.GL_DEBUG_SEVERITY_HIGH,
        ARBDebugOutput.GL_DEBUG_SEVERITY_HIGH_ARB,
        AMDDebugOutput.GL_DEBUG_SEVERITY_HIGH_AMD),
        MEDIUM(
        GL43.GL_DEBUG_SEVERITY_MEDIUM,
        KHRDebug.GL_DEBUG_SEVERITY_MEDIUM,
        ARBDebugOutput.GL_DEBUG_SEVERITY_MEDIUM_ARB,
        AMDDebugOutput.GL_DEBUG_SEVERITY_MEDIUM_AMD),
        LOW(
        GL43.GL_DEBUG_SEVERITY_LOW,
        KHRDebug.GL_DEBUG_SEVERITY_LOW,
        ARBDebugOutput.GL_DEBUG_SEVERITY_LOW_ARB,
        AMDDebugOutput.GL_DEBUG_SEVERITY_LOW_AMD),
        NOTIFICATION(
        GL43.GL_DEBUG_SEVERITY_NOTIFICATION,
        KHRDebug.GL_DEBUG_SEVERITY_NOTIFICATION,
        -1,
        -1);

        final int gl43, khr, arb, amd;

        GLDebugMessageSeverity(int gl43, int khr, int arb, int amd){
            this.gl43 = gl43;
            this.khr = khr;
            this.arb = arb;
            this.amd = amd;
        }
    }

}
