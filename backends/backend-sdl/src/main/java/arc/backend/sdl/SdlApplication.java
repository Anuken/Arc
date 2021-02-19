package arc.backend.sdl;

import arc.*;
import arc.audio.*;
import arc.func.*;
import arc.graphics.*;
import arc.struct.*;
import arc.util.*;
import arc.util.async.*;

import static arc.backend.sdl.jni.SDL.*;

public class SdlApplication implements Application{
    private final Seq<ApplicationListener> listeners = new Seq<>();
    private final TaskQueue runnables = new TaskQueue();
    private final int[] inputs = new int[34];

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
        Core.net = new Net();
        Core.graphics = this.graphics = new SdlGraphics(this);
        Core.input = this.input = new SdlInput();
        Core.settings = new Settings();
        Core.audio = new Audio();

        initIcon();

        graphics.updateSize(config.width, config.height);

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

    private void initIcon(){
        if(config.windowIconPaths != null && config.windowIconPaths.length > 0){
            String path = config.windowIconPaths[0];
            try{
                Pixmap p = new Pixmap(Core.files.get(path, config.windowIconFileType));
                long surface = SDL_CreateRGBSurfaceFrom(p.getPixels(), p.getWidth(), p.getHeight());
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

        check(() -> SDL_Init(SDL_INIT_VIDEO | SDL_INIT_EVENTS));

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

        window = SDL_CreateWindow(config.title, config.width, config.height, flags);
        if(window == 0) throw new SDLError();

        context = SDL_GL_CreateContext(window);
        if(context == 0) throw new SDLError();

        if(config.vSyncEnabled){
            SDL_GL_SetSwapInterval(1);
        }

        //always have text input on
        SDL_StartTextInput();
    }

    private void loop(){

        graphics.updateSize(config.width, config.height);
        listen(ApplicationListener::init);

        while(running){
            while(SDL_PollEvent(inputs)){
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
                    inputs[0] == SDL_EVENT_KEYBOARD ||
                    inputs[0] == SDL_EVENT_TEXT_INPUT){
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
            throw new SDLError();
        }
    }

    @Override
    public boolean openFolder(String file){
        Threads.daemon(() -> {
            if(OS.isWindows){
                OS.execSafe("explorer.exe /select," + file.replace("/", "\\"));
            }else if(OS.isLinux){
                OS.execSafe("xdg-open " + file);
            }else if(OS.isMac){
                OS.execSafe("open " + file);
            }
        });
        return true;
    }

    @Override
    public boolean openURI(String url){
        try{
            if(OS.isMac){
                Class.forName("com.apple.eio.FileManager").getMethod("openURL", String.class).invoke(null, url);
                return true;
            }else if(OS.isLinux){
                return OS.execSafe("xdg-open " + url);
            }else if(OS.isWindows){
                return OS.execSafe("rundll32 url.dll,FileProtocolHandler " + url);
            }
            return false;
        }catch(Throwable e){
            e.printStackTrace();
            return false;
        }
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
}
