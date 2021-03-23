package arc.maps.objects;

import arc.maps.MapObject;
import arc.math.geom.Polygon;

/** Represents {@link Polygon} map objects */
public class PolygonMapObject extends MapObject{
    public Polygon polygon;

    /** Creates empty polygon map object */
    public PolygonMapObject(){
        this(new float[0]);
    }

    /** @param vertices polygon defining vertices (at least 3) */
    public PolygonMapObject(float[] vertices){
        polygon = new Polygon(vertices);
    }

    /** @param polygon the polygon */
    public PolygonMapObject(Polygon polygon){
        this.polygon = polygon;
    }

}
