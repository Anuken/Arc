package io.anuke.arc.util;

import io.anuke.arc.Core;
import io.anuke.arc.collection.DelayedRemovalArray;
import io.anuke.arc.collection.LongArray;
import io.anuke.arc.util.Timer.Task;
import io.anuke.arc.util.pooling.Pool.Poolable;
import io.anuke.arc.util.pooling.Pools;

public class Time{
    private static final long nanosPerMilli = 1000000;
    private static double time;
    private static DelayedRemovalArray<DelayRun> runs = new DelayedRemovalArray<>();
    private static LongArray marks = new LongArray();
    private static DeltaProvider deltaimpl = () -> Math.min(Core.graphics.getDeltaTime() * 60f, 3f);

    public static synchronized void run(float delay, Runnable r){
        DelayRun run = Pools.obtain(DelayRun.class, DelayRun::new);
        run.finish = r;
        run.delay = delay;
        runs.add(run);
    }

    public static synchronized void runTask(float delay, Runnable r){
        Timer.schedule(new Task(){
            @Override
            public void run(){
                r.run();
            }
        }, delay / 60f);
    }

    public static float time(){
        return (float)time;
    }

    public static void resetTime(float time){
        Time.time = time;
    }

    public static void mark(){
        marks.add(nanos());
    }

    /** A value of -1 means mark() wasn't called beforehand. */
    public static float elapsed(){
        if(marks.size == 0){
            return -1;
        }else{
            return timeSinceNanos(marks.pop()) / 1000000f;
        }
    }

    /** Use normal delta time (e. g. gdx delta * 60) */
    public static synchronized void update(){
        float delta = delta();

        time += delta;

        runs.begin();

        for(DelayRun run : runs){
            run.delay -= delta;

            if(run.run != null)
                run.run.run();

            if(run.delay <= 0){
                if(run.finish != null)
                    run.finish.run();
                runs.removeValue(run, true);
                Pools.free(run);
            }
        }

        runs.end();
    }

    public static synchronized void clear(){
        runs.clear();
    }

    public static float delta(){
        return deltaimpl.get();
    }

    public static void setDeltaProvider(DeltaProvider impl){
        deltaimpl = impl;
    }

    static void dispose(){
        runs.clear();
    }

    /** @return The current value of the system timer, in nanoseconds. */
    public static long nanos(){
        return System.nanoTime();
    }

    /** @return the difference, measured in milliseconds, between the current time and midnight, January 1, 1970 UTC. */
    public static long millis(){
        return System.currentTimeMillis();
    }

    /**
     * Convert nanoseconds time to milliseconds
     * @param nanos must be nanoseconds
     * @return time value in milliseconds
     */
    public static long nanosToMillis(long nanos){
        return nanos / nanosPerMilli;
    }

    /**
     * Convert milliseconds time to nanoseconds
     * @param millis must be milliseconds
     * @return time value in nanoseconds
     */
    public static long millisToNanos(long millis){
        return millis * nanosPerMilli;
    }

    /**
     * Get the time in nanos passed since a previous time
     * @param prevTime - must be nanoseconds
     * @return - time passed since prevTime in nanoseconds
     */
    public static long timeSinceNanos(long prevTime){
        return nanos() - prevTime;
    }

    /**
     * Get the time in millis passed since a previous time
     * @param prevTime - must be milliseconds
     * @return - time passed since prevTime in milliseconds
     */
    public static long timeSinceMillis(long prevTime){
        return millis() - prevTime;
    }

    public interface DeltaProvider{
        float get();
    }

    public static class DelayRun implements Poolable{
        public float delay;
        public Runnable run;
        public Runnable finish;

        @Override
        public void reset(){
            delay = 0;
            run = finish = null;
        }
    }
}
