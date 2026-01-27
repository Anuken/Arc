package arc.backend.sdl;

import arc.*;
import arc.audio.*;
import arc.files.*;
import arc.func.*;
import arc.graphics.*;
import arc.math.geom.*;
import arc.scene.ui.*;
import arc.struct.*;
import arc.util.*;
import arc.util.TaskQueue;
import org.lwjgl.sdl.*;
import org.lwjgl.system.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class SdlApplication implements Application{
    private final Seq<ApplicationListener> listeners = new Seq<>();
    private final TaskQueue runnables = new TaskQueue();

    final SdlGraphics graphics;
    final SdlInput input;
    final SdlConfig config;
    final Thread mainThread;

    boolean running = true;
    long window, context;

    public SdlApplication(ApplicationListener listener, SdlConfig config){
        this.config = config;
        this.listeners.add(listener);

        mainThread = Thread.currentThread();

        init();

        Core.app = this;
        Core.files = new SdlFiles();
        Core.graphics = this.graphics = new SdlGraphics(this);
        Core.input = this.input = new SdlInput();
        Core.settings = new Settings();
        Core.audio = new Audio(!config.disableAudio);

        initIcon();

        graphics.updateSize(config.width, config.height);

        addTextInputListener();

        try{
            loop();
            listen(ApplicationListener::exit);
        }finally{
            try{
                cleanup();
            }catch(Throwable error){
                error.printStackTrace();
            }
        }
    }

    /** Used for Scene text fields. */
    private void addTextInputListener(){
        addListener(new ApplicationListener(){
            TextField lastFocus;

            @Override
            public void update(){
                if(Core.scene != null && Core.scene.getKeyboardFocus() instanceof TextField){
                    TextField next = (TextField)Core.scene.getKeyboardFocus();
                    if(lastFocus == null){
                        SDLKeyboard.SDL_StartTextInput(window);
                    }
                    lastFocus = next;
                }else if(lastFocus != null){
                    SDLKeyboard.SDL_StopTextInput(window);
                    lastFocus = null;
                }

                if(lastFocus != null){
                    Vec2 pos = lastFocus.localToStageCoordinates(Tmp.v1.setZero());
                    try(MemoryStack stack = MemoryStack.stackPush()){
                        SDL_Rect rect = SDL_Rect.malloc(stack)
                        .set((int)pos.x,
                        Core.graphics.getHeight() - 1 - (int)(pos.y + lastFocus.getHeight()),
                        (int)lastFocus.getWidth(), (int)lastFocus.getHeight());

                        SDLKeyboard.nSDL_SetTextInputArea(window, rect.address(), 0);
                    }
                }
            }
        });
    }

    private void initIcon(){
        if(config.windowIconPaths != null && config.windowIconPaths.length > 0){
            String path = config.windowIconPaths[0];
            try{
                Pixmap p = new Pixmap(Core.files.get(path, config.windowIconFileType));
                try(SDL_Surface surface = SDLSurface.SDL_CreateSurfaceFrom(p.width, p.height, SDLPixels.SDL_PIXELFORMAT_RGBA32, p.pixels, 4 * p.width)){
                    if(surface != null){
                        SDLVideo.SDL_SetWindowIcon(window, surface);
                    }
                }
                p.dispose();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private void init(){
        ArcNativesLoader.load();

        if(OS.isMac) restartMac();

        check(SDLInit.SDL_Init(SDLInit.SDL_INIT_VIDEO | SDLInit.SDL_INIT_EVENTS));

        //show native IME candidate UI
        SDLHints.SDL_SetHint("SDL_IME_SHOW_UI","1");
        SDLHints.SDL_SetHint("SDL_WINDOWS_DPI_SCALING", "1");

        check(SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_PROFILE_MASK, OS.isMac || config.coreProfile ? SDLVideo.SDL_GL_CONTEXT_PROFILE_CORE : SDLVideo.SDL_GL_CONTEXT_PROFILE_COMPATIBILITY));

        check(SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_RED_SIZE, config.r));
        check(SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_GREEN_SIZE, config.g));
        check(SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_BLUE_SIZE, config.b));
        check(SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_DEPTH_SIZE, config.depth));
        check(SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_STENCIL_SIZE, config.stencil));
        check(SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_DOUBLEBUFFER, 1));

        //this doesn't seem to do anything, but at least I tried
        if(config.samples > 0){
            check(SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_MULTISAMPLEBUFFERS, 1));
            check(SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_MULTISAMPLESAMPLES, config.samples));
        }

        long flags = SDLVideo.SDL_WINDOW_OPENGL;
        if(!config.initialVisible) flags |= SDLVideo.SDL_WINDOW_HIDDEN;
        if(!config.decorated) flags |= SDLVideo.SDL_WINDOW_BORDERLESS;
        if(config.resizable) flags |= SDLVideo.SDL_WINDOW_RESIZABLE;
        if(config.maximized) flags |= SDLVideo.SDL_WINDOW_MAXIMIZED;
        if(config.fullscreen) flags |= SDLVideo.SDL_WINDOW_FULLSCREEN;

        window = SDLVideo.SDL_CreateWindow(config.title, config.width, config.height, flags);
        if(window == 0) throw new SdlError();

        SdlError finalError = null;
        boolean createdContext = false;

        for(int[] attemptedVersion : config.glVersions){
            //always run a compatibility profile for 2.x; only 3.2+ allows core profiles
            if(attemptedVersion[0] == 2){
                check(SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_PROFILE_MASK, SDLVideo.SDL_GL_CONTEXT_PROFILE_COMPATIBILITY));
            }

            check(SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_MAJOR_VERSION, attemptedVersion[0]));
            check(SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_MINOR_VERSION, attemptedVersion[1]));

            try{
                context = SDLVideo.SDL_GL_CreateContext(window);
                if(context == 0) throw new SdlError();

                createdContext = true;
                break;
            }catch(SdlError error){
                finalError = error;
                Log.err("Failed to initialize OpenGL @.@: @", attemptedVersion[0], attemptedVersion[1], Strings.getSimpleMessage(error));
            }
        }

        if(finalError != null && !createdContext) throw finalError;

        if(config.vSyncEnabled){
            SDLVideo.SDL_GL_SetSwapInterval(1);
        }

        String ver = SDLVersion.SDL_GetRevision();

        Log.info("[Core] Initialized @", ver);
    }

    private void loop(){

        graphics.updateSize(config.width, config.height);
        listen(ApplicationListener::init);
        try(MemoryStack stack = MemoryStack.stackPush()){
            SDL_Event event = SDL_Event.malloc(stack);

            while(running){
                while(SDLEvents.SDL_PollEvent(event)){
                    int type = event.type();
                    switch(type){
                        case SDLEvents.SDL_EVENT_QUIT:
                            running = false;
                            break;

                        case SDLEvents.SDL_EVENT_WINDOW_PIXEL_SIZE_CHANGED:
                            int w = event.window().data1(), h = event.window().data2();
                            graphics.updateSize(w, h);
                            listen(l -> l.resize(w, h));
                            break;

                        case SDLEvents.SDL_EVENT_WINDOW_FOCUS_GAINED:
                            listen(ApplicationListener::resume);
                            break;

                        case SDLEvents.SDL_EVENT_WINDOW_FOCUS_LOST:
                            listen(ApplicationListener::pause);
                            break;

                        default:
                            input.handleInput(event);
                    }
                }

                graphics.update();
                input.update();
                defaultUpdate();

                listen(ApplicationListener::update);

                runnables.run();

                SDLVideo.SDL_GL_SwapWindow(window);
                input.postUpdate();
            }
        }


    }

    private void listen(Cons<ApplicationListener> cons){
        synchronized(listeners){
            for(ApplicationListener l : listeners){
                cons.get(l);
            }
        }
    }

    private void cleanup(){
        listen(l -> {
            l.pause();
            try{
                l.dispose();
            }catch(Throwable t){
                t.printStackTrace();
            }
        });
        dispose();

        SDLVideo.SDL_DestroyWindow(window);
        SDLInit.SDL_Quit();
    }

    private void check(boolean code){
        if(!code){
            throw new SdlError();
        }
    }

    public long getWindow(){
        return window;
    }

    @Override
    public Thread getMainThread(){
        return mainThread;
    }

    @Override
    public boolean openFolder(String file){
        Threads.daemon(() -> {
            if(OS.isWindows){
                OS.execSafe("explorer.exe /select," + file.replace("/", "\\"));
            }else if(OS.isLinux){
                OS.execSafe("xdg-open", file);
            }else if(OS.isMac){
                OS.execSafe("open", file);
            }
        });
        return true;
    }

    @Override
    public boolean openURI(String url){

        //make sure it's a valid URI
        if(url.isEmpty()) return false;
        try{
            URI.create(url);
        }catch(Exception wrong){
            return false;
        }

        Threads.daemon(() -> {
            if(OS.isMac){
                OS.execSafe("open", url);
            }else if(OS.isLinux){
                OS.execSafe("xdg-open", url);
            }else if(OS.isWindows){
                OS.execSafe("rundll32", "url.dll,FileProtocolHandler", url);
            }
        });
        return true;
    }

    @Override
    public Seq<ApplicationListener> getListeners(){
        return listeners;
    }

    @Override
    public ApplicationType getType(){
        return ApplicationType.desktop;
    }

    @Override
    public String getClipboardText(){
        return SDLClipboard.SDL_GetClipboardText();
    }

    @Override
    public void setClipboardText(String text){
        SDLClipboard.SDL_SetClipboardText(text);
    }

    @Override
    public void post(Runnable runnable){
        runnables.post(runnable);
    }

    @Override
    public void exit(){
        running = false;
    }

    public static class SdlError extends RuntimeException{
        public SdlError(){
            super(SDLError.SDL_GetError());
        }
    }

    /** MacOS doesn't work when -XstartOnFirstThread is not passed, this will restart the program with that argument if it isn't already present. */
    @SuppressWarnings("unchecked")
    private void restartMac(){
        try{
            Class<?> mgmt = Class.forName("java.lang.management.ManagementFactory");
            Class<?> beanClass = Class.forName("java.lang.management.RuntimeMXBean");
            Object bean = Reflect.invoke(mgmt, "getRuntimeMXBean");
            String id = ((String)beanClass.getMethod("getName").invoke(bean)).split("@")[0];

            if(!OS.hasEnv("JAVA_STARTED_ON_FIRST_THREAD_" + id) || OS.env("JAVA_STARTED_ON_FIRST_THREAD_" + id).equals("0")){ //check if equal to 0 just in case
                Log.warn("Applying -XstartOnFirstThread for macOS support.");
                String javaPath = //attempt to locate java
                    new Fi(OS.prop("java.home")).child("bin/java").exists() ? new Fi(OS.prop("java.home")).child("bin/java").absolutePath() :
                    Core.files.local("jre/bin/java").exists() ? Core.files.local("jre/bin/java").absolutePath() :
                    "java";
                try{
                    Fi jar = Fi.get(SdlApplication.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                    Seq<String> launchOptions = Seq.with(javaPath);
                    launchOptions.addAll((List<String>)beanClass.getMethod("getInputArguments").invoke(bean));
                    launchOptions.addAll(System.getProperties().entrySet().stream().map(it -> "-D" + it).toArray(String[]::new));
                    launchOptions.addAll("-XstartOnFirstThread", "-jar", jar.absolutePath(), "-firstThread");

                    Process proc = new ProcessBuilder(launchOptions.toArray(String.class)).inheritIO().start();
                    System.exit(proc.waitFor());
                }catch(IOException | URISyntaxException e){ //some part of this failed, likely failed to find java
                    Log.err(e);
                    Log.err("Failed to apply the -XstartOnFirstThread argument, it is required in order to work on mac.");
                }catch(InterruptedException ignored){}
            }
        }catch(Exception ignored){} //likely using bundled java, do nothing as the arg is already added
    }
}
