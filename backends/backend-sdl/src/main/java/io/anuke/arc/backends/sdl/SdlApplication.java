package io.anuke.arc.backends.sdl;

import io.anuke.arc.*;
import io.anuke.arc.audio.mock.*;
import io.anuke.arc.backends.sdl.audio.*;
import io.anuke.arc.collection.*;
import io.anuke.arc.function.*;
import io.anuke.arc.graphics.*;
import io.anuke.arc.util.*;

public class SdlApplication implements Application{
    private final Array<ApplicationListener> listeners = new Array<>();
    private final Array<Runnable> runnables = new Array<>();
    private final Array<Runnable> executedRunnables = new Array<>();
    private final int[] inputs = new int[34];

    final SdlGraphics graphics;
    final SdlInput input;
    final SdlConfig config;
    OpenALAudio audio;

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

        try{
            Core.audio = config.disableAudio ? new MockAudio() : (audio = new OpenALAudio(config.audioDeviceSimultaneousSources));
        }catch(Throwable t){
            Log.err(t);
            Log.err("Error initializing; disabling audio.");
            Core.audio = new MockAudio();
        }

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
                long surface = io.anuke.arc.backends.sdl.jni.SDL.SDL_CreateRGBSurfaceFrom(p.getPixels(), p.getWidth(), p.getHeight());
                io.anuke.arc.backends.sdl.jni.SDL.SDL_SetWindowIcon(window, surface);
                io.anuke.arc.backends.sdl.jni.SDL.SDL_FreeSurface(surface);
                p.dispose();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

    }

    private void init(){
        ArcNativesLoader.load();

        check(() -> io.anuke.arc.backends.sdl.jni.SDL.SDL_Init(io.anuke.arc.backends.sdl.jni.SDL.SDL_INIT_VIDEO | io.anuke.arc.backends.sdl.jni.SDL.SDL_INIT_EVENTS | io.anuke.arc.backends.sdl.jni.SDL.SDL_INIT_AUDIO));

        //set up openGL 2.1; is this really the lowest version needed?
        check(() -> io.anuke.arc.backends.sdl.jni.SDL.SDL_GL_SetAttribute(io.anuke.arc.backends.sdl.jni.SDL.SDL_GL_CONTEXT_MAJOR_VERSION, 2));
        check(() -> io.anuke.arc.backends.sdl.jni.SDL.SDL_GL_SetAttribute(io.anuke.arc.backends.sdl.jni.SDL.SDL_GL_CONTEXT_MINOR_VERSION, 0));

        check(() -> io.anuke.arc.backends.sdl.jni.SDL.SDL_GL_SetAttribute(io.anuke.arc.backends.sdl.jni.SDL.SDL_GL_RED_SIZE, config.r));
        check(() -> io.anuke.arc.backends.sdl.jni.SDL.SDL_GL_SetAttribute(io.anuke.arc.backends.sdl.jni.SDL.SDL_GL_GREEN_SIZE, config.g));
        check(() -> io.anuke.arc.backends.sdl.jni.SDL.SDL_GL_SetAttribute(io.anuke.arc.backends.sdl.jni.SDL.SDL_GL_BLUE_SIZE, config.b));
        check(() -> io.anuke.arc.backends.sdl.jni.SDL.SDL_GL_SetAttribute(io.anuke.arc.backends.sdl.jni.SDL.SDL_GL_DEPTH_SIZE, config.depth));
        check(() -> io.anuke.arc.backends.sdl.jni.SDL.SDL_GL_SetAttribute(io.anuke.arc.backends.sdl.jni.SDL.SDL_GL_DOUBLEBUFFER, 1));

        int flags = io.anuke.arc.backends.sdl.jni.SDL.SDL_WINDOW_OPENGL;
        if(config.initialVisible) flags |= io.anuke.arc.backends.sdl.jni.SDL.SDL_WINDOW_SHOWN;
        if(!config.decorated) flags |= io.anuke.arc.backends.sdl.jni.SDL.SDL_WINDOW_BORDERLESS;
        if(config.resizable) flags |= io.anuke.arc.backends.sdl.jni.SDL.SDL_WINDOW_RESIZABLE;
        if(config.maximized) flags |= io.anuke.arc.backends.sdl.jni.SDL.SDL_WINDOW_MAXIMIZED;

        window = io.anuke.arc.backends.sdl.jni.SDL.SDL_CreateWindow(config.title, config.width, config.height, flags);
        if(window == 0) throw new SDLError();

        context = io.anuke.arc.backends.sdl.jni.SDL.SDL_GL_CreateContext(window);
        if(context == 0) throw new SDLError();

        if(config.vSyncEnabled){
            io.anuke.arc.backends.sdl.jni.SDL.SDL_GL_SetSwapInterval(1);
        }

        //always have text input on
        io.anuke.arc.backends.sdl.jni.SDL.SDL_StartTextInput();
    }

    private void loop(){

        graphics.updateSize(config.width, config.height);
        listen(ApplicationListener::init);

        while(running){
            while(io.anuke.arc.backends.sdl.jni.SDL.SDL_PollEvent(inputs)){
                if(inputs[0] == io.anuke.arc.backends.sdl.jni.SDL.SDL_EVENT_QUIT){
                    running = false;
                }else if(inputs[0] == io.anuke.arc.backends.sdl.jni.SDL.SDL_EVENT_WINDOW){
                    int type = inputs[1];
                    if(type == io.anuke.arc.backends.sdl.jni.SDL.SDL_WINDOWEVENT_SIZE_CHANGED){
                        graphics.updateSize(inputs[2], inputs[3]);
                        listen(l -> l.resize(inputs[2], inputs[3]));
                    }else if(type == io.anuke.arc.backends.sdl.jni.SDL.SDL_WINDOWEVENT_SHOWN){
                        listen(ApplicationListener::resume);
                    }else if(type == io.anuke.arc.backends.sdl.jni.SDL.SDL_WINDOWEVENT_HIDDEN){
                        listen(ApplicationListener::pause);
                    }
                }else if(inputs[0] == io.anuke.arc.backends.sdl.jni.SDL.SDL_EVENT_MOUSE_MOTION ||
                    inputs[0] == io.anuke.arc.backends.sdl.jni.SDL.SDL_EVENT_MOUSE_BUTTON ||
                    inputs[0] == io.anuke.arc.backends.sdl.jni.SDL.SDL_EVENT_MOUSE_WHEEL ||
                    inputs[0] == io.anuke.arc.backends.sdl.jni.SDL.SDL_EVENT_KEYBOARD ||
                    inputs[0] == io.anuke.arc.backends.sdl.jni.SDL.SDL_EVENT_TEXT_INPUT){
                    input.handleInput(inputs);
                }
            }

            graphics.update();
            input.update();
            if(audio != null){
                audio.update();
            }

            listen(ApplicationListener::update);

            synchronized(runnables){
                executedRunnables.clear();
                executedRunnables.addAll(runnables);
                runnables.clear();
            }

            for(Runnable runnable : executedRunnables){
                runnable.run();
            }

            io.anuke.arc.backends.sdl.jni.SDL.SDL_GL_SwapWindow(window);
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
            try{
                l.dispose();
            }catch(Throwable t){
                t.printStackTrace();
            }
        });
        dispose();
        Core.audio.dispose();

        io.anuke.arc.backends.sdl.jni.SDL.SDL_DestroyWindow(window);
        io.anuke.arc.backends.sdl.jni.SDL.SDL_Quit();
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
        return io.anuke.arc.backends.sdl.jni.SDL.SDL_GetClipboardText();
    }

    @Override
    public void setClipboardText(String text){
        io.anuke.arc.backends.sdl.jni.SDL.SDL_SetClipboardText(text);
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
