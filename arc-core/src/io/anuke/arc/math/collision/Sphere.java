package io.anuke.arc.math.collision;

import io.anuke.arc.math.Mathf;
import io.anuke.arc.math.geom.Vector3;

import java.io.Serializable;

/**
 * Encapsulates a 3D sphere with a center and a radius
 * @author badlogicgames@gmail.com
 */
public class Sphere implements Serializable{
    private static final long serialVersionUID = -6487336868908521596L;
    private static final float PI_4_3 = Mathf.PI * 4f / 3f;
    /** the center of the sphere **/
    public final Vector3 center;
    /** the radius of the sphere **/
    public float radius;

    /**
     * Constructs a sphere with the given center and radius
     * @param center The center
     * @param radius The radius
     */
    public Sphere(Vector3 center, float radius){
        this.center = new Vector3(center);
        this.radius = radius;
    }

    /**
     * @param sphere the other sphere
     * @return whether this and the other sphere overlap
     */
    public boolean overlaps(Sphere sphere){
        return center.dst2(sphere.center) < (radius + sphere.radius) * (radius + sphere.radius);
    }

    @Override
    public int hashCode(){
        final int prime = 71;
        int result = 1;
        result = prime * result + this.center.hashCode();
        result = prime * result + Float.floatToRawIntBits(this.radius);
        return result;
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || o.getClass() != this.getClass()) return false;
        Sphere s = (Sphere)o;
        return this.radius == s.radius && this.center.equals(s.center);
    }

    public float volume(){
        return PI_4_3 * this.radius * this.radius * this.radius;
    }

    public float surfaceArea(){
        return 4 * Mathf.PI * this.radius * this.radius;
    }
}
