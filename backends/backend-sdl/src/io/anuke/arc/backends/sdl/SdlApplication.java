package io.anuke.arc.backends.sdl;

import io.anuke.arc.*;
import io.anuke.arc.collection.*;
import io.anuke.arc.function.*;
import io.anuke.arc.graphics.*;
import sdl.*;

public class SdlApplication implements Application{
    private final Array<ApplicationListener> listeners = new Array<>();
    private final Array<Runnable> runnables = new Array<>();
    private final Array<Runnable> executedRunnables = new Array<>();
    private final int[] inputs = new int[8];
    private final SdlConfig config;

    private boolean running = true;
    private long window, context;

    public SdlApplication(ApplicationListener listener, SdlConfig config){
        this.config = config;

        init();

        Core.app = this;
        Core.files = new SdlFiles();
        Core.net = new SdlNet();
        Core.graphics = new SdlGraphics();
        Core.settings = new Settings();

        try{
            loop();
        }finally{
            cleanup();
        }
    }

    private void init(){
        check(() -> SDL.SDL_Init(SDL.SDL_INIT_VIDEO | SDL.SDL_INIT_EVENTS));

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

        while(running){
            while(SDL.SDL_PollEvent(inputs)){
                if(inputs[0] == SDL.SDL_EVENT_QUIT){
                    running = false;
                }
            }

            SDLGL.glClearColor(1f, 0f, 0f, 1f);
            SDLGL.glClear(GL20.GL_COLOR_BUFFER_BIT);

            for(ApplicationListener listener : listeners){
                listener.update();
            }

            synchronized(runnables){
                executedRunnables.clear();
                executedRunnables.addAll(runnables);
                runnables.clear();
            }

            for(Runnable runnable : executedRunnables){
                runnable.run();
            }

            try{
                Thread.sleep(16);
            }catch(Exception e){

            }

            SDL.SDL_GL_SwapWindow(window);
        }
    }

    private void cleanup(){
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
    public long getNativeHeap(){
        return 0;
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
