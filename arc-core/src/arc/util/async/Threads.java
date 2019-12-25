package arc.util.async;

import arc.util.*;

/**
 * Utilities for threaded programming.
 * @author badlogic
 */
public class Threads{

    public static void yield(){
        Thread.yield();
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

    /** Starts a new daemon thread.*/
    public static Thread daemon(String name, Runnable runnable){
        Thread thread = new Thread(runnable, name);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }
}
