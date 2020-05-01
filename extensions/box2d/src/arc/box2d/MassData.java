package arc.box2d;


import arc.math.geom.*;

/**
 * This holds the mass data computed for a shape.
 * @author mzechner
 */
public class MassData{
    /** The mass of the shape, usually in kilograms. **/
    public float mass;

    /** The position of the shape's centroid relative to the shape's origin. **/
    public final Vec2 center = new Vec2();

    /** The rotational inertia of the shape about the local origin. **/
    public float I;
}
