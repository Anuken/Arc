package io.anuke.arc.math.geom;

import io.anuke.arc.math.Angles;
import io.anuke.arc.math.Mathf;

/** Represents a point in 2-D space. */
public interface Position{
    float getX();

    float getY();

    default float angleTo(Position other){
        return Angles.angle(getX(), getY(), other.getX(), other.getY());
    }

    default float angleTo(float x, float y){
        return Angles.angle(getX(), getY(), x, y);
    }

    default float dst2(Position other){
        return dst2(other.getX(), other.getY());
    }

    default float dst(Position other){
        return dst(other.getX(), other.getY());
    }

    default float dst(float x, float y){
        final float xd = getX() - x;
        final float yd = getY() - y;
        return Mathf.sqrt(xd * xd + yd * yd);
    }

    default float dst2(float x, float y){
        final float xd = getX() - x;
        final float yd = getY() - y;
        return (xd * xd + yd * yd);
    }

    default boolean withinDst(float x, float y, float dst){
        return Mathf.dst2(getX(), getY(), x, y) < dst*dst;
    }
}
