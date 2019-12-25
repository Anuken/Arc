package arc.backend.teavm.emu;

import arc.backend.teavm.plugin.Annotations.*;
import arc.util.*;
import arc.util.async.*;


@Replace(AsyncExecutor.class)
public class AsyncExecutorEmu implements Disposable{

    public AsyncExecutorEmu(int maxConcurrent){}

    public <T> AsyncResult<T> submit(final AsyncTask<T> task){
        try{
             task.call();
        }catch(Throwable t){
            throw new ArcRuntimeException("Could not submit AsyncTask: " + t.getMessage(), t);
        }
        return null;
    }

    @Override
    public void dispose(){
    }
}