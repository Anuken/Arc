package io.anuke.arc.backends.teavm.emu;

import io.anuke.arc.backends.teavm.plugin.Annotations.*;
import io.anuke.arc.util.*;
import io.anuke.arc.util.async.*;


@Replace(AsyncExecutor.class)
public class AsyncExecutorEmulator implements Disposable{

    public AsyncExecutorEmulator(int maxConcurrent){}

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