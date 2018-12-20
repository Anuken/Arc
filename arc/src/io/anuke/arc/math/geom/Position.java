package io.anuke.arc.math.geom;

import io.anuke.arc.math.Mathf;

/** Represents a point in 2-D space. */
public interface Position{
    Position x = new FixedPosition(1, 0);
    Position y = new FixedPosition(0, 1);
    Position zero = new FixedPosition(0, 0);

    float getX();

    float getY();

    default float angleTo(Position other){
        return Mathf.atan2(other.getX() - getX(), other.getY() - getY());
    }

    default float dst(Position other){
        return dst(other.getX(), other.getY());
    }

    default float dst(float x, float y){
        final float xd = getX() - x;
        final float yd = getX() - y;
        return (float)Math.sqrt(xd * xd + yd * yd);
    }
}
