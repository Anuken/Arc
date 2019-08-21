package io.anuke.arc.util;

import java.util.Arrays;

public class Interval{
    float[] times;

    public Interval(int capacity){
        times = new float[capacity];
    }

    public Interval(){
        this(1);
    }

    public boolean get(float time){
        return get(0, time);
    }

    public boolean get(int id, float time){
        if(id >= times.length) throw new RuntimeException("Out of bounds! Max timer size is " + times.length + "!");

        if(Time.time() - times[id] >= time ||
                Time.time() < times[id]){ //when 'time travel' happens, reset.
            times[id] = Time.time();
            return true;
        }else{
            return false;
        }
    }

    public void reset(int id, float time){
        times[id] = Time.time() - time;
    }

    public void clear(){
        Arrays.fill(times, Time.time());
    }

    public float getTime(int id){
        return Time.time() - times[id];
    }

    public float[] getTimes(){
        return times;
    }
}
