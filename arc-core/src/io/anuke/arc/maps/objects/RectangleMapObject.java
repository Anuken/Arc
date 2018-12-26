package io.anuke.arc.maps.objects;

import io.anuke.arc.maps.MapObject;
import io.anuke.arc.math.geom.Rectangle;

/** @brief Represents a rectangle shaped map object */
public class RectangleMapObject extends MapObject{

    private Rectangle rectangle;

    /** Creates a rectangle object which lower left corner is at (0, 0) with width=1 and height=1 */
    public RectangleMapObject(){
        this(0.0f, 0.0f, 1.0f, 1.0f);
    }

    /**
     * Creates a {@link Rectangle} object with the given X and Y coordinates along with a given width and height.
     * @param x X coordinate
     * @param y Y coordinate
     * @param width Width of the {@link Rectangle} to be created.
     * @param height Height of the {@link Rectangle} to be created.
     */
    public RectangleMapObject(float x, float y, float width, float height){
        super();
        rectangle = new Rectangle(x, y, width, height);
    }

    /** @return rectangle shape */
    public Rectangle getRectangle(){
        return rectangle;
    }

}
