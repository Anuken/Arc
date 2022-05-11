package arc.util;

import arc.*;
import arc.func.*;
import arc.struct.*;

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

    public static void awaitAll(Seq<Future<?>> futures){
        try{
            for(Future<?> f : futures){
                f.get();
            }
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

    public static ExecutorService executor(){
        return executor(Runtime.getRuntime().availableProcessors(), true);
    }

    /** @return an executor that has no limit on the amount of threads it will create. */
    public static ExecutorService unboundedExecutor(){
        return cachedExecutor(1, Integer.MAX_VALUE, false);
    }

    public static ExecutorService cachedExecutor(){
        return cachedExecutor(1, Integer.MAX_VALUE, true);
    }

    public static ExecutorService cachedExecutor(int min){
        return cachedExecutor(min, Integer.MAX_VALUE, true);
    }

    /** @param blocking uses a BlockingQueue rather than a SynchronousQueue. Note that min is ignored when this is true. */
    public static ExecutorService cachedExecutor(int min, int max, boolean blocking){
        return cachedExecutor(min, max, blocking, null);
    }

    /** @param blocking uses a BlockingQueue rather than a SynchronousQueue. Note that min is ignored when this is true. */
    public static ExecutorService cachedExecutor(int min, int max, boolean blocking, String name){
        return new ThreadPoolExecutor(blocking ? max : min, max,
        30L, TimeUnit.SECONDS,
        blocking ? new LinkedBlockingQueue<>() : new SynchronousQueue<>(),
        r -> {
            Thread thread = name != null ? new Thread(r, name) : new Thread(r);
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
