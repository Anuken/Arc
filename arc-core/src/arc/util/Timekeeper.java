package arc.util;

public class Timekeeper{
    private final long intervalms;
    private long time;

    public Timekeeper(float seconds){
        intervalms = (int)(seconds * 1000);
    }

    public boolean get(){
        return Time.timeSinceMillis(time) > intervalms;
    }

    public void reset(){
        time = Time.millis();
    }
}
