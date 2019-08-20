package io.anuke.arc.util.async;

import io.anuke.arc.util.*;

/**
 * Utilities for threaded programming.
 * @author badlogic
 */
public class Threads{

    public static void yield(){
        Thread.yield();
    }

    /** Throws an exception in the main game thread.*/
    public static void throwAppException(Throwable t){
        Time.runTask(0f, () -> {
            throw new RuntimeException(t);
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
}
