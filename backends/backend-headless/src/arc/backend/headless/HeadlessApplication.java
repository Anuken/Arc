package arc.backend.headless;

import arc.*;
import arc.func.*;
import arc.mock.*;
import arc.struct.*;
import arc.util.*;
import arc.util.async.*;

/**
 * a headless implementation of an application primarily intended to be used in servers
 * @author Jon Renner
 */
public class HeadlessApplication implements Application{
    protected final MockGraphics graphics;
    protected final Seq<ApplicationListener> listeners = new Seq<>();
    protected final Seq<Runnable> runnables = new Seq<>();
    protected final Seq<Runnable> executedRunnables = new Seq<>();
    protected final Cons<Throwable> exceptionHandler;
    protected long renderInterval;
    protected Thread mainLoopThread;
    protected boolean running = true;

    public HeadlessApplication(ApplicationListener listener){
        this(listener, null, t -> { throw new RuntimeException(t); });
    }

    public HeadlessApplication(ApplicationListener listener, HeadlessApplicationConfiguration config, Cons<Throwable> exceptionHandler){
        if(config == null)
            config = new HeadlessApplicationConfiguration();

        addListener(listener);
        this.exceptionHandler = exceptionHandler;
        // the following elements are not applicable for headless applications
        // they are only implemented as mock objects
        this.graphics = new MockGraphics();

        Core.settings = new Settings();
        Core.app = this;
        Core.files = new MockFiles();
        Core.net = new Net();
        Core.audio = new MockAudio();
        Core.graphics = graphics;
        Core.input = new MockInput();

        renderInterval = config.renderInterval > 0 ? (long)(config.renderInterval * 1000000000f) : (config.renderInterval < 0 ? -1 : 0);

        initialize();
    }

    protected void initialize(){
        mainLoopThread = new Thread("HeadlessApplication"){
            @Override
            public void run(){
                try{
                    HeadlessApplication.this.mainLoop();
                }catch(Throwable t){
                    exceptionHandler.get(t);
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

        long t = Time.nanos() + renderInterval;
        if(renderInterval >= 0f){
            while(running){
                final long n = Time.nanos();
                if(t > n){
                    long sleep = t - n;
                    Threads.sleep(sleep / 1000000, (int)(sleep % 1000000));

                    t += renderInterval;
                }else{
                    t = n + renderInterval;
                }

                executeRunnables();
                graphics.incrementFrameId();
                defaultUpdate();

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

    public void executeRunnables(){
        synchronized(runnables){
            for(int i = runnables.size - 1; i >= 0; i--)
                executedRunnables.add(runnables.get(i));
            runnables.clear();
        }
        if(executedRunnables.size == 0) return;
        for(int i = executedRunnables.size - 1; i >= 0; i--)
            executedRunnables.remove(i).run();
    }

    @Override
    public ApplicationType getType(){
        return ApplicationType.headless;
    }

    @Override
    public long getJavaHeap(){
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    @Override
    public String getClipboardText(){
        return null;
    }

    @Override
    public void setClipboardText(String text){

    }

    @Override
    public Seq<ApplicationListener> getListeners(){
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

    public static class HeadlessApplicationConfiguration{
        /** The minimum time (in seconds) between each call to the render method or negative to not call the render method at all. */
        public float renderInterval = 1f / 60f;
    }
}
