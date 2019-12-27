package arc.maps.objects;

import arc.maps.MapObject;
import arc.math.geom.Rect;

/** Represents a rectangle shaped map object */
public class RectangleMapObject extends MapObject{
    public Rect rect;

    /** Creates a rectangle object which lower left corner is at (0, 0) with width=1 and height=1 */
    public RectangleMapObject(){
        this(0.0f, 0.0f, 1.0f, 1.0f);
    }

    /**
     * Creates a {@link Rect} object with the given X and Y coordinates along with a given width and height.
     * @param x X coordinate
     * @param y Y coordinate
     * @param width Width of the {@link Rect} to be created.
     * @param height Height of the {@link Rect} to be created.
     */
    public RectangleMapObject(float x, float y, float width, float height){
        super();
        rect = new Rect(x, y, width, height);
    }

}
