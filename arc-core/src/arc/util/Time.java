package arc.util;

import arc.*;
import arc.struct.*;
import arc.func.*;
import arc.util.pooling.Pool.*;
import arc.util.pooling.*;

public class Time{
    /** Conversion factors for ticks to other unit values. */
    public static final float toSeconds = 60f, toMinutes = 60f * 60f, toHours = 60f * 60f * 60f;

    /** Global delta value. Do not change. */
    public static float delta = 1f;

    public static final long nanosPerMilli = 1000000;

    private static double time;
    private static double globalTime;
    private static Seq<DelayRun> runs = new Seq<>();
    private static Seq<DelayRun> removal = new Seq<>();
    private static LongSeq marks = new LongSeq();
    private static Floatp deltaimpl = () -> Math.min(Core.graphics.getDeltaTime() * 60f, 3f);

    public static synchronized void run(float delay, Runnable r){
        DelayRun run = Pools.obtain(DelayRun.class, DelayRun::new);
        run.finish = r;
        run.delay = delay;
        runs.add(run);
    }

    public static synchronized void runTask(float delay, Runnable r){
        Timer.schedule(r, delay / 60f);
    }

    public static float time(){
        return (float)time;
    }

    public static float globalTime(){
        return (float)globalTime;
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

    public static void updateGlobal(){
        globalTime += Core.graphics.getDeltaTime()*60f;
        delta = deltaimpl.get();
    }

    /** Use normal delta time (e. g. delta * 60) */
    public static void update(){
        time += delta;
        removal.clear();

        if(Double.isInfinite(time) || Double.isNaN(time)){
            time = 0;
        }

        for(DelayRun run : runs){
            run.delay -= delta;

            if(run.delay <= 0){
                run.finish.run();
                removal.add(run);
                Pools.free(run);
            }
        }

        runs.removeAll(removal);
    }

    public static synchronized void clear(){
        runs.clear();
    }

    public static void setDeltaProvider(Floatp impl){
        deltaimpl = impl;
        delta = impl.get();
    }

    public static void dispose(){
        time = globalTime = 0;
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

    public static class DelayRun implements Poolable{
        float delay;
        Runnable finish;

        @Override
        public void reset(){
            delay = 0;
            finish = null;
        }
    }
}
