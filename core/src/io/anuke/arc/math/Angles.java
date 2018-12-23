package io.anuke.arc.math;

import io.anuke.arc.Core;
import io.anuke.arc.function.Consumer;
import io.anuke.arc.function.FloatConsumer;
import io.anuke.arc.function.PositionConsumer;
import io.anuke.arc.math.geom.Vector2;

public class Angles{
    private static final RandomXS128 random = new RandomXS128();
    private static final Vector2 rv = new Vector2();

    public static float forwardDistance(float angle1, float angle2){
        return angle1 > angle2 ? angle1 - angle2 : angle2 - angle1;
    }

    public static float backwardDistance(float angle1, float angle2){
        return 360 - forwardDistance(angle1, angle2);
    }

    public static float angleDist(float a, float b){
        a = a % 360f;
        b = b % 360f;
        return Math.min(forwardDistance(a, b), backwardDistance(a, b));
    }

    public static boolean near(float a, float b, float range){
        return angleDist(a, b) < range;
    }

    public static float moveToward(float angle, float to, float speed){
        if(Math.abs(angleDist(angle, to)) < speed) return to;

        if((angle > to && backwardDistance(angle, to) > forwardDistance(angle, to)) ||
                (angle < to && backwardDistance(angle, to) < forwardDistance(angle, to))){
            angle -= speed;
        }else{
            angle += speed;
        }

        return angle;
    }

    public static float angle(float x, float y, float x2, float y2){
        return Mathf.atan2(x2 - x, y2 - y);
    }

    public static float trnsx(float angle, float len){
        return len * Mathf.cos(Mathf.degreesToRadians * angle);
    }

    public static float trnsy(float angle, float len){
        return len * Mathf.sin(Mathf.degreesToRadians * angle);
    }

    public static float trnsx(float angle, float x, float y){
        return rv.set(x, y).rotate(angle).x;
    }

    public static float trnsy(float angle, float x, float y){
        return rv.set(x, y).rotate(angle).y;
    }

    public static float mouseAngle(float cx, float cy){
        Vector2 avector = Core.camera.project(cx, cy);
        return Mathf.atan2(Core.input.mouseX() - avector.x, Core.input.mouseY() - avector.y);
    }

    public static void circle(int points, Consumer<Float> cons){
        for(int i = 0; i < points; i++){
            cons.accept(i * 360f / points);
        }
    }

    public static void circleVectors(int points, float length, PositionConsumer pos){
        for(int i = 0; i < points; i++){
            float f = i * 360f / points;
            pos.accept(trnsx(f, length), trnsy(f, length));
        }
    }

    public static void circleVectors(int points, float length, float offset, PositionConsumer pos){
        for(int i = 0; i < points; i++){
            float f = i * 360f / points + offset;
            pos.accept(trnsx(f, length), trnsy(f, length));
        }
    }

    public static void shotgun(int points, float spacing, float offset, FloatConsumer cons){
        for(int i = 0; i < points; i++){
            cons.accept(i * spacing - (points - 1) * spacing / 2f + offset);
        }
    }

    public static void randVectors(long seed, int amount, float length, PositionConsumer cons){
        random.setSeed(seed);
        for(int i = 0; i < amount; i++){
            float vang = random.nextFloat() * 360f;
            rv.set(length, 0).rotate(vang);
            cons.accept(rv.x, rv.y);
        }
    }

    public static void randLenVectors(long seed, int amount, float length, PositionConsumer cons){
        random.setSeed(seed);
        for(int i = 0; i < amount; i++){
            float scl = length * random.nextFloat();
            float vang = random.nextFloat() * 360f;
            rv.set(scl, 0).rotate(vang);
            cons.accept(rv.x, rv.y);
        }
    }

    public static void randLenVectors(long seed, int amount, float length, float angle, float range, PositionConsumer cons){
        random.setSeed(seed);
        for(int i = 0; i < amount; i++){
            float scl = length * random.nextFloat();
            float vang = angle + random.nextFloat() * range * 2 - range;
            rv.set(scl, 0).rotate(vang);
            cons.accept(rv.x, rv.y);
        }
    }

    public static void randLenVectors(long seed, float fin, int amount, float length,
                                      float angle, float range, ParticleConsumer cons){
        random.setSeed(seed);
        for(int i = 0; i < amount; i++){
            float scl = length * random.nextFloat() * fin;
            float vang = angle + random.nextFloat() * range * 2 - range;
            rv.set(scl, 0).rotate(vang);
            cons.accept(rv.x, rv.y, fin * (random.nextFloat()));
        }
    }

    public interface ParticleConsumer{
        void accept(float x, float y, float fin);
    }

}

