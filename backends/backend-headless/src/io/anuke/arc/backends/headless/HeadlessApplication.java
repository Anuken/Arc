package io.anuke.arc.backends.headless;

import io.anuke.arc.*;
import io.anuke.arc.audio.mock.*;
import io.anuke.arc.backends.headless.mock.*;
import io.anuke.arc.collection.Array;
import io.anuke.arc.function.Consumer;
import io.anuke.arc.util.*;

/**
 * a headless implementation of a GDX Application primarily intended to be used in servers
 * @author Jon Renner
 */
public class HeadlessApplication implements Application{
    protected final HeadlessFiles files;
    protected final HeadlessNet net;
    protected final MockAudio audio;
    protected final MockInput input;
    protected final MockGraphics graphics;
    protected final Array<ApplicationListener> listeners = new Array<>();
    protected final Array<Runnable> runnables = new Array<>();
    protected final Array<Runnable> executedRunnables = new Array<>();
    protected final Consumer<Throwable> exceptionHandler;
    private final long renderInterval;
    protected Thread mainLoopThread;
    protected boolean running = true;

    public HeadlessApplication(ApplicationListener listener){
        this(listener, null, t -> { throw new RuntimeException(t); });
    }

    public HeadlessApplication(ApplicationListener listener, HeadlessApplicationConfiguration config, Consumer<Throwable> exceptionHandler){
        if(config == null)
            config = new HeadlessApplicationConfiguration();

        addListener(listener);
        this.files = new HeadlessFiles();
        this.exceptionHandler = exceptionHandler;
        this.net = new HeadlessNet();
        // the following elements are not applicable for headless applications
        // they are only implemented as mock objects
        this.graphics = new MockGraphics();
        this.audio = new MockAudio();
        this.input = new MockInput();

        Core.settings = new Settings();
        Core.app = this;
        Core.files = files;
        Core.net = net;
        Core.audio = audio;
        Core.graphics = graphics;
        Core.input = input;

        renderInterval = config.renderInterval > 0 ? (long)(config.renderInterval * 1000000000f) : (config.renderInterval < 0 ? -1 : 0);

        initialize();
    }

    private void initialize(){
        mainLoopThread = new Thread("HeadlessApplication"){
            @Override
            public void run(){
                try{
                    HeadlessApplication.this.mainLoop();
                }catch(Throwable t){
                    exceptionHandler.accept(t);
                }
            }
        };
        mainLoopThread.start();
    }

    void mainLoop(){
        synchronized(listeners){
            for(ApplicationListener listener : listeners){
                listener.init();
            }
        }

        // unlike LwjglApplication, a headless application will eat up CPU in this while loop
        // it is up to the implementation to call Thread.sleep as necessary
        long t = Time.nanos() + renderInterval;
        if(renderInterval >= 0f){
            while(running){
                final long n = Time.nanos();
                if(t > n){
                    try{
                        Thread.sleep((t - n) / 1000000);
                    }catch(InterruptedException e){
                    }
                    t = Time.nanos() + renderInterval;
                }else
                    t = n + renderInterval;

                executeRunnables();
                graphics.incrementFrameId();
                synchronized(listeners){
                    for(ApplicationListener listener : listeners){
                        listener.update();
                    }
                }
                graphics.updateTime();

                // If one of the runnables set running to false, for example after an exit().
                if(!running) break;
            }
        }

        synchronized(listeners){
            for(ApplicationListener listener : listeners){
                listener.pause();
                listener.dispose();
            }
            dispose();
        }
    }

    public boolean executeRunnables(){
        synchronized(runnables){
            for(int i = runnables.size - 1; i >= 0; i--)
                executedRunnables.add(runnables.get(i));
            runnables.clear();
        }
        if(executedRunnables.size == 0) return false;
        for(int i = executedRunnables.size - 1; i >= 0; i--)
            executedRunnables.remove(i).run();
        return true;
    }

    @Override
    public ApplicationType getType(){
        return ApplicationType.HeadlessDesktop;
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
    public String getClipboardText(){
        return null;
    }

    @Override
    public void setClipboardText(String text){

    }

    @Override
    public Array<ApplicationListener> getListeners(){
        return listeners;
    }

    @Override
    public void post(Runnable runnable){
        synchronized(runnables){
            runnables.add(runnable);
        }
    }

    @Override
    public void exit(){
        post(() -> running = false);
    }
}
