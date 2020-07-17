package arc.util;

import arc.struct.Seq;

/** @author xoppa */
public class PerformanceCounters{
    private static final float nano2seconds = 1f / 1000000000.0f;
    public final Seq<PerformanceCounter> counters = new Seq<>();
    private long lastTick = 0L;

    public PerformanceCounter add(final String name, final int windowSize){
        PerformanceCounter result = new PerformanceCounter(name, windowSize);
        counters.add(result);
        return result;
    }

    public PerformanceCounter add(final String name){
        PerformanceCounter result = new PerformanceCounter(name);
        counters.add(result);
        return result;
    }

    public void tick(){
        final long t = Time.nanos();
        if(lastTick > 0L) tick((t - lastTick) * nano2seconds);
        lastTick = t;
    }

    public void tick(final float deltaTime){
        for(int i = 0; i < counters.size; i++)
            counters.get(i).tick(deltaTime);
    }

    @Override
    public String toString(){
        counters.sort((a, b) -> -Float.compare(a.load.value, b.load.value));
        StringBuilder sb = new StringBuilder();
        sb.setLength(0);
        for(int i = 0; i < counters.size; i++){
            counters.get(i).toString(sb);
            if(i != counters.size - 1) sb.append("\n");
        }
        return sb.toString();
    }
}
