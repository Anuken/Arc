package arc.util.async;

import arc.*;
import arc.func.*;
import arc.util.*;

import java.util.concurrent.*;

/**
 * Utilities for threaded programming.
 */
public class Threads{

    public static <T> ThreadLocal<T> local(Prov<T> prov){
        return new ThreadLocal<T>(){
            @Override
            protected T initialValue(){
                return prov.get();
            }
        };
    }

    public static <T> T await(Future<T> future){
        try{
            return future.get();
        }catch(ExecutionException | InterruptedException ex){
            throw new ArcRuntimeException(ex.getCause());
        }
    }

    public static ExecutorService executor(int threads, boolean daemon){
        return Executors.newFixedThreadPool(threads, r -> {
            Thread thread = new Thread(r, "AsyncExecutor-Thread");
            thread.setDaemon(daemon);
            thread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
            return thread;
        });
    }

    public static ExecutorService executor(int threads){
        return executor(threads, true);
    }

    public static ExecutorService cachedExecutor(){
        return cachedExecutor(1, Integer.MAX_VALUE);
    }

    public static ExecutorService cachedExecutor(int min){
        return cachedExecutor(min, Integer.MAX_VALUE);
    }

    public static ExecutorService cachedExecutor(int min, int max){
        return new ThreadPoolExecutor(min, max,
        60L, TimeUnit.SECONDS,
        new SynchronousQueue<>(),
        r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
            return thread;
        });
    }

    /** Shuts down the executor and waits for its termination indefinitely. */
    public static void await(ExecutorService exec){
        try{
            exec.shutdown();
            exec.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        }catch(InterruptedException e){
            throw new RuntimeException(e);
        }
    }

    public static void sleep(long ms){
        try{
            Thread.sleep(ms);
        }catch(InterruptedException e){
            throw new RuntimeException(e);
        }
    }

    public static void sleep(long ms, int ns){
        try{
            Thread.sleep(ms, ns);
        }catch(InterruptedException e){
            throw new RuntimeException(e);
        }
    }

    /** Throws an exception in the main game thread.*/
    public static void throwAppException(Throwable t){
        Core.app.post(() -> {
            if(t instanceof RuntimeException){
                throw ((RuntimeException)t);
            }else{
                throw new RuntimeException(t);
            }
        });
    }

    /** Starts a new non-daemon thread.*/
    public static Thread thread(Runnable runnable){
        Thread thread = new Thread(runnable);
        thread.start();
        return thread;
    }

    /** Starts a new daemon thread.*/
    public static Thread daemon(Runnable runnable){
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    /** Starts a new daemon thread.*/
    public static Thread daemon(String name, Runnable runnable){
        Thread thread = new Thread(runnable, name);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }
}
