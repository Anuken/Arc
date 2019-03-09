package io.anuke.arc.function;

/**A runnable where anything might happen.*/
public interface UnsafeRunnable{
    void run() throws Throwable;
}
