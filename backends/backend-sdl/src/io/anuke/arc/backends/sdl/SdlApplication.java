package io.anuke.arc.backends.sdl;

import io.anuke.arc.*;
import io.anuke.arc.collection.*;
import io.anuke.arc.function.*;
import sdl.*;

public class SdlApplication implements Application{
    private final Array<ApplicationListener> listeners = new Array<>();
    private final Array<Runnable> runnables = new Array<>();
    private final Array<Runnable> executedRunnables = new Array<>();
    private final int[] inputs = new int[8];
    private final SdlConfig config;

    private boolean running = true;
    private long window;

    public SdlApplication(SdlConfig config){
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

        int flags = SDL.SDL_WINDOW_SHOWN;
        if(!config.windowDecorated) flags |= SDL.SDL_WINDOW_BORDERLESS;
        if(config.windowResizable) flags |= SDL.SDL_WINDOW_RESIZABLE;
        if(config.windowMaximized) flags |= SDL.SDL_WINDOW_MAXIMIZED;

        window = SDL.SDL_CreateWindow(config.title, config.windowWidth, config.windowHeight, flags);
        if(window == 0) throw new SDLError();
    }

    private void loop(){

        while(running){
            //todo poll events here
            while(SDL.SDL_PollEvent(inputs)){
                if(inputs[0] == SDL.SDL_EVENT_QUIT){
                    running = false;
                }
            }

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

            //todo swap buffers/etc
        }
    }

    private void cleanup(){

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
