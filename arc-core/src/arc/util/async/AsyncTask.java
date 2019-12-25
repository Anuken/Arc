package arc.util.async;

/**
 * Task to be submitted to an {@link AsyncExecutor}, returning a result of type T.
 * @author badlogic
 */
public interface AsyncTask<T>{
    T call() throws Exception;
}
