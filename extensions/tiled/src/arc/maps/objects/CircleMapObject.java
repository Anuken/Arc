package arc.maps.objects;

import arc.maps.MapObject;
import arc.math.geom.Circle;

/** Represents {@link Circle} shaped map objects */
public class CircleMapObject extends MapObject{
    public final Circle circle;

    /** Creates a circle map object at (0,0) with r=1.0 */
    public CircleMapObject(){
        this(0.0f, 0.0f, 1.0f);
    }

    /**
     * Creates a circle map object
     * @param x X coordinate
     * @param y Y coordinate
     * @param radius Radius of the circle object.
     */
    public CircleMapObject(float x, float y, float radius){
        super();
        circle = new Circle(x, y, radius);
    }
}
