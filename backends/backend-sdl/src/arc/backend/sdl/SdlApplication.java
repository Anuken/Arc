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

import java.io.*;
import java.net.*;
import java.util.*;

import static arc.backend.sdl.jni.SDL.*;

public class SdlApplication implements Application{
    private final Seq<ApplicationListener> listeners = new Seq<>();
    private final TaskQueue runnables = new TaskQueue();
    private final int[] inputs = new int[64];

    final SdlGraphics graphics;
    final SdlInput input;
    final SdlConfig config;

    boolean running = true;
    long window, context;

    public SdlApplication(ApplicationListener listener, SdlConfig config){
        this.config = config;
        this.listeners.add(listener);

        init();

        Core.app = this;
        Core.files = new SdlFiles();
        Core.graphics = this.graphics = new SdlGraphics(this);
        Core.input = this.input = new SdlInput();
        Core.settings = new Settings();
        Core.audio = new Audio(!config.disableAudio);

        initIcon();

        graphics.updateSize(config.width, config.height);

        //can't be bothered to recompile arc for mac
        if(!OS.isMac){
            addTextInputListener();
        }

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
                        SDL_StartTextInput();
                    }
                    lastFocus = next;
                }else if(lastFocus != null){
                    SDL_StopTextInput();
                    lastFocus = null;
                }

                if(lastFocus != null){
                    Vec2 pos = lastFocus.localToStageCoordinates(Tmp.v1.setZero());
                    SDL_SetTextInputRect((int)pos.x, Core.graphics.getHeight() - 1 - (int)(pos.y + lastFocus.getHeight()), (int)lastFocus.getWidth(), (int)lastFocus.getHeight());
                }
            }
        });
    }

    private void initIcon(){
        if(config.windowIconPaths != null && config.windowIconPaths.length > 0){
            String path = config.windowIconPaths[0];
            try{
                Pixmap p = new Pixmap(Core.files.get(path, config.windowIconFileType));
                long surface = SDL_CreateRGBSurfaceFrom(p.pixels, p.width, p.height);
                SDL_SetWindowIcon(window, surface);
                SDL_FreeSurface(surface);
                p.dispose();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private void init(){
        ArcNativesLoader.load();

        if(OS.isMac) restartMac();

        check(() -> SDL_Init(SDL_INIT_VIDEO | SDL_INIT_EVENTS));

        //show native IME candidate UI
        SDL_SetHint("SDL_IME_SHOW_UI","1");
        
        //set up openGL 2.0 profile
        check(() -> SDL_GL_SetAttribute(SDL_GL_CONTEXT_MAJOR_VERSION, config.gl30 ? config.gl30Major : 2));
        check(() -> SDL_GL_SetAttribute(SDL_GL_CONTEXT_MINOR_VERSION,  config.gl30 ? config.gl30Minor : 0));

        if(config.gl30 && OS.isMac){
            check(() -> SDL_GL_SetAttribute(SDL_GL_CONTEXT_PROFILE_MASK, SDL_GL_CONTEXT_PROFILE_CORE));
        }

        check(() -> SDL_GL_SetAttribute(SDL_GL_RED_SIZE, config.r));
        check(() -> SDL_GL_SetAttribute(SDL_GL_GREEN_SIZE, config.g));
        check(() -> SDL_GL_SetAttribute(SDL_GL_BLUE_SIZE, config.b));
        check(() -> SDL_GL_SetAttribute(SDL_GL_DEPTH_SIZE, config.depth));
        check(() -> SDL_GL_SetAttribute(SDL_GL_STENCIL_SIZE, config.stencil));
        check(() -> SDL_GL_SetAttribute(SDL_GL_DOUBLEBUFFER, 1));

        //this doesn't seem to do anything, but at least I tried
        if(config.samples > 0){
            check(() -> SDL_GL_SetAttribute(SDL_GL_MULTISAMPLEBUFFERS, 1));
            check(() -> SDL_GL_SetAttribute(SDL_GL_MULTISAMPLESAMPLES, config.samples));
        }

        int flags = SDL_WINDOW_OPENGL;
        if(config.initialVisible) flags |= SDL_WINDOW_SHOWN;
        if(!config.decorated) flags |= SDL_WINDOW_BORDERLESS;
        if(config.resizable) flags |= SDL_WINDOW_RESIZABLE;
        if(config.maximized) flags |= SDL_WINDOW_MAXIMIZED;
        if(config.fullscreen) flags |= SDL_WINDOW_FULLSCREEN;

        window = SDL_CreateWindow(config.title, config.width, config.height, flags);
        if(window == 0) throw new SdlError();

        context = SDL_GL_CreateContext(window);
        if(context == 0) throw new SdlError();

        if(config.vSyncEnabled){
            SDL_GL_SetSwapInterval(1);
        }

        int[] ver = new int[3];
        SDL_GetVersion(ver);
        Log.info("[Core] Initialized SDL v@.@.@", ver[0], ver[1], ver[2]);
    }

    private void loop(){

        graphics.updateSize(config.width, config.height);
        listen(ApplicationListener::init);

        while(running){
            while(SDL_PollEvent(inputs, Core.graphics.getWidth(), Core.graphics.getHeight())){
                if(inputs[0] == SDL_EVENT_QUIT){
                    running = false;
                }else if(inputs[0] == SDL_EVENT_WINDOW){
                    int type = inputs[1];
                    if(type == SDL_WINDOWEVENT_SIZE_CHANGED){
                        graphics.updateSize(inputs[2], inputs[3]);
                        listen(l -> l.resize(inputs[2], inputs[3]));
                    }else if(type == SDL_WINDOWEVENT_FOCUS_GAINED){
                        listen(ApplicationListener::resume);
                    }else if(type == SDL_WINDOWEVENT_FOCUS_LOST){
                        listen(ApplicationListener::pause);
                    }
                }else if(inputs[0] == SDL_EVENT_MOUSE_MOTION ||
                    inputs[0] == SDL_EVENT_MOUSE_BUTTON ||
                    inputs[0] == SDL_EVENT_MOUSE_WHEEL ||
                    inputs[0] == SDL_EVENT_TOUCH ||
                    inputs[0] == SDL_EVENT_KEYBOARD ||
                    inputs[0] == SDL_EVENT_TEXT_INPUT ||
                    inputs[0] == SDL_EVENT_TEXT_EDIT){
                    input.handleInput(inputs);
                }
            }

            graphics.update();
            input.update();
            defaultUpdate();

            listen(ApplicationListener::update);

            runnables.run();

            SDL_GL_SwapWindow(window);
            input.postUpdate();
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

        SDL_DestroyWindow(window);
        SDL_Quit();
    }

    private void check(Intp run){
        if(run.get() != 0){
            throw new SdlError();
        }
    }

    public long getWindow(){
        return window;
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
        return SDL_GetClipboardText();
    }

    @Override
    public void setClipboardText(String text){
        SDL_SetClipboardText(text);
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
            super(SDL_GetError());
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
