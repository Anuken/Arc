package arc.graphics.vector;

import arc.util.pooling.Pool.*;
import arc.util.pooling.*;

public class Point implements Poolable{
    float x, y;
    float dx, dy;
    float length;
    float dmx, dmy;
    int flags;

    public Point(){
    }

    public static Point obtain(){
        return Pools.obtain(Point.class, Point::new);
    }

    public static Point obtain(Point other){
        return obtain().set(other);
    }

    public static boolean pointEquals(float x1, float y1, float x2, float y2, float tolerance){
        float dx = x2 - x1;
        float dy = y2 - y1;
        return dx * dx + dy * dy < tolerance * tolerance;
    }

    private Point set(Point other){
        x = other.x;
        y = other.y;
        dx = other.dx;
        dy = other.dy;
        length = other.length;
        dmx = other.dmx;
        dmy = other.dmy;
        flags = other.flags;
        return this;
    }

    public boolean isFlagSet(int flag){
        return (flags & flag) != 0;
    }

    public void orFlags(int flag){
        flags |= flag;
    }

    public boolean pointEquals(float x2, float y2, float tolerance){
        return pointEquals(x, y, x2, y2, tolerance);
    }

    @Override
    public void reset(){
        x = 0;
        y = 0;
        dx = 0;
        dy = 0;
        length = 0;
        dmx = 0;
        dmy = 0;
        flags = 0;
    }

    public void free(){
        Pools.free(this);
    }

    @Override
    public String toString(){
        return "Point[x= " + x + ", y=" + y + ", dx=" + dx + ", dy=" + dy + ", len=" + length + ", dmx=" + dmx + ", dmy=" + dmy + ", flags=" + flags + "]";
    }
}
