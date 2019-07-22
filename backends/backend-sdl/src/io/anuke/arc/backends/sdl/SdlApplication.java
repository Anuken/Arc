package io.anuke.arc.backends.sdl;

import io.anuke.arc.*;
import io.anuke.arc.collection.*;
import io.anuke.arc.function.*;
import io.anuke.arc.graphics.*;
import io.anuke.arc.util.*;
import sdl.*;

public class SdlApplication implements Application{
    private final Array<ApplicationListener> listeners = new Array<>();
    private final Array<Runnable> runnables = new Array<>();
    private final Array<Runnable> executedRunnables = new Array<>();
    private final int[] inputs = new int[8];

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
        Core.net = new SdlNet();
        Core.graphics = this.graphics = new SdlGraphics(this);
        Core.input = this.input = new SdlInput();
        Core.settings = new Settings();
        Core.audio = new SdlAudio();

        initIcon();

        graphics.updateSize(config.width, config.height);

        try{
            loop();
        }finally{
            cleanup();
        }
    }

    private void initIcon(){
        if(config.windowIconPaths != null && config.windowIconPaths.length > 0){
            String path = config.windowIconPaths[0];
            try{
                Pixmap p = new Pixmap(Core.files.getFileHandle(path, config.windowIconFileType));
                long surface = SDL.SDL_CreateRGBSurfaceFrom(p.getPixels(), p.getWidth(), p.getHeight());
                SDL.SDL_SetWindowIcon(window, surface);
                SDL.SDL_FreeSurface(surface);
                p.dispose();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

    }

    private void init(){
        ArcNativesLoader.load();

        check(() -> SDL.SDL_Init(SDL.SDL_INIT_VIDEO | SDL.SDL_INIT_EVENTS | SDL.SDL_INIT_AUDIO));

        //set up openGL 2.1; is this really the lowest version needed?
        check(() -> SDL.SDL_GL_SetAttribute(SDL.SDL_GL_CONTEXT_MAJOR_VERSION, 2));
        check(() -> SDL.SDL_GL_SetAttribute(SDL.SDL_GL_CONTEXT_MINOR_VERSION, 1));

        check(() -> SDL.SDL_GL_SetAttribute(SDL.SDL_GL_RED_SIZE, config.r));
        check(() -> SDL.SDL_GL_SetAttribute(SDL.SDL_GL_GREEN_SIZE, config.g));
        check(() -> SDL.SDL_GL_SetAttribute(SDL.SDL_GL_BLUE_SIZE, config.b));
        check(() -> SDL.SDL_GL_SetAttribute(SDL.SDL_GL_DEPTH_SIZE, config.depth));
        check(() -> SDL.SDL_GL_SetAttribute(SDL.SDL_GL_DOUBLEBUFFER, 1));

        int flags = SDL.SDL_WINDOW_SHOWN | SDL.SDL_WINDOW_OPENGL;
        if(!config.decorated) flags |= SDL.SDL_WINDOW_BORDERLESS;
        if(config.resizable) flags |= SDL.SDL_WINDOW_RESIZABLE;
        if(config.maximized) flags |= SDL.SDL_WINDOW_MAXIMIZED;

        window = SDL.SDL_CreateWindow(config.title, config.width, config.height, flags);
        if(window == 0) throw new SDLError();

        context = SDL.SDL_GL_CreateContext(window);
        if(context == 0) throw new SDLError();

        SDL.SDL_GL_SetSwapInterval(1);
    }

    private void loop(){

        graphics.updateSize(config.width, config.height);
        listen(ApplicationListener::init);

        while(running){
            while(SDL.SDL_PollEvent(inputs)){
                if(inputs[0] == SDL.SDL_EVENT_QUIT){
                    running = false;
                }else if(inputs[0] == SDL.SDL_EVENT_WINDOW){
                    int type = inputs[1];
                    if(type == SDL.SDL_WINDOWEVENT_SIZE_CHANGED){
                        graphics.updateSize(inputs[2], inputs[3]);
                        listen(l -> l.resize(inputs[2], inputs[3]));
                    }else if(type == SDL.SDL_WINDOWEVENT_SHOWN){
                        listen(ApplicationListener::resume);
                    }else if(type == SDL.SDL_WINDOWEVENT_HIDDEN){
                        listen(ApplicationListener::pause);
                    }
                }else if(inputs[0] == SDL.SDL_EVENT_MOUSE_MOTION ||
                    inputs[0] == SDL.SDL_EVENT_MOUSE_BUTTON ||
                    inputs[0] == SDL.SDL_EVENT_MOUSE_WHEEL ||
                    inputs[0] == SDL.SDL_EVENT_KEYBOARD){
                    input.handleInput(inputs);
                }
            }

            graphics.update();
            input.update();

            listen(ApplicationListener::update);

            synchronized(runnables){
                executedRunnables.clear();
                executedRunnables.addAll(runnables);
                runnables.clear();
            }

            for(Runnable runnable : executedRunnables){
                runnable.run();
            }

            SDL.SDL_GL_SwapWindow(window);
            input.prepareNext();
        }
    }

    private void listen(Consumer<ApplicationListener> cons){
        synchronized(listeners){
            for(ApplicationListener l : listeners){
                cons.accept(l);
            }
        }
    }

    private void cleanup(){
        listen(l -> {
            l.pause();
            l.dispose();
        });
        dispose();
        Core.audio.dispose();

        SDL.SDL_DestroyWindow(window);
        SDL.SDL_Quit();
    }

    private void check(IntProvider run){
        if(run.get() != 0){
            throw new SDLError();
        }
    }

    @Override
    public Array<ApplicationListener> getListeners(){
        return listeners;
    }

    @Override
    public ApplicationType getType(){
        return ApplicationType.Desktop;
    }

    @Override
    public long getJavaHeap(){
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    @Override
    public String getClipboardText(){
        return SDL.SDL_GetClipboardText();
    }

    @Override
    public void setClipboardText(String text){
        SDL.SDL_SetClipboardText(text);
    }

    @Override
    public void post(Runnable runnable){
        synchronized(runnables){
            runnables.add(runnable);
        }
    }

    @Override
    public void exit(){
        running = false;
    }
}
