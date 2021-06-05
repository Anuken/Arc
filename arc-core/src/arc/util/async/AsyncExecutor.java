package arc.util.async;

import arc.util.ArcRuntimeException;
import arc.util.Disposable;

import java.util.concurrent.*;

/**
 * Allows asynchronous execution of {@link AsyncTask} instances on a separate thread. Needs to be disposed via a call to
 * {@link #dispose()} when no longer used, in which case the executor waits for running tasks to finish. Scheduled but not yet
 * running tasks will not be executed.
 * @author badlogic
 */
//TODO this is just a crappy wrapper for newFixedThreadPool and offers no additional utility
public class AsyncExecutor implements Disposable{
    private final ExecutorService executor;

    /** Creates an AsyncExecutor that allows one thread per core. */
    public AsyncExecutor(){
        this(Runtime.getRuntime().availableProcessors());
    }

    /** Creates a new AsyncExecutor that allows maxConcurrent {@link Runnable} instances to run in parallel. */
    public AsyncExecutor(int maxConcurrent){
        executor = Executors.newFixedThreadPool(maxConcurrent, r -> {
            Thread thread = new Thread(r, "AsyncExecutor-Thread");
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
            return thread;
        });
    }

    public AsyncResult<Void> submit(Runnable run){
        return submit(() -> {
           run.run();
           return null;
        });
    }

    /**
     * Submits a {@link Runnable} to be executed asynchronously. If maxConcurrent runnables are already running, the runnable will
     * be queued.
     * @param task the task to execute asynchronously
     */
    public <T> AsyncResult<T> submit(final AsyncTask<T> task){
        if(executor.isShutdown()){
            throw new ArcRuntimeException("Cannot run tasks on an executor that has been shutdown (disposed)");
        }
        return new AsyncResult<>(executor.submit(task::call));
    }

    /**
     * Waits for running {@link AsyncTask} instances to finish, then destroys any resources like threads. Can not be used after
     * this method is called.
     */
    @Override
    public void dispose(){
        Threads.await(executor);
    }
}
