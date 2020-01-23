package arc.ecs.weaver.impl;

import java.util.concurrent.*;

abstract class CallableTransmuter<T> implements Callable<T>{
    private final String file;

    protected CallableTransmuter(String file){
        this.file = file;
    }

    protected abstract T process(String file);

    @Override
    public final T call() throws Exception{
        T t = process(file);
        return t;
    }
}
