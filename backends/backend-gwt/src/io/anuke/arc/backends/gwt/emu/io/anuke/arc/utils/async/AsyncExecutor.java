package io.anuke.arc.util.async;

import io.anuke.arc.util.Disposable;
import io.anuke.arc.util.ArcRuntimeException;

/**
 * GWT emulation of AsynchExecutor, will call tasks immediately :D
 * @author badlogic
 */
public class AsyncExecutor implements Disposable{

    /**
     * Creates a new AsynchExecutor that allows maxConcurrent {@link Runnable} instances to run in parallel.
     */
    public AsyncExecutor(int maxConcurrent){
    }

    /**
     * Submits a {@link Runnable} to be executed asynchronously. If maxConcurrent runnables are already running, the runnable will
     * be queued.
     * @param task the task to execute asynchronously
     */
    public <T> AsyncResult<T> submit(final AsyncTask<T> task){
        T result;
        try{
            result = task.call();
        }catch(Throwable t){
            throw new ArcRuntimeException("Could not submit AsyncTask: " + t.getMessage(), t);
        }
        return new AsyncResult<T>(result);
    }

    /**
     * Waits for running {@link AsyncTask} instances to finish, then destroys any resources like threads. Can not be used after
     * this method is called.
     */
    @Override
    public void dispose(){
    }
}
