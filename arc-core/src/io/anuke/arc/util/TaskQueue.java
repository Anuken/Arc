package io.anuke.arc.util;

import io.anuke.arc.collection.*;

public class TaskQueue{
    private final Array<Runnable> runnables = new Array<>();
    private final Array<Runnable> executedRunnables = new Array<>();

    public void run(){
        synchronized(runnables){
            executedRunnables.clear();
            executedRunnables.addAll(runnables);
            runnables.clear();
        }

        for(Runnable runnable : executedRunnables){
            runnable.run();
        }
    }

    public void clear(){
        synchronized(runnables){
            runnables.clear();
        }
    }

    public void post(Runnable runnable){
        synchronized(runnables){
            runnables.add(runnable);
        }
    }
}
