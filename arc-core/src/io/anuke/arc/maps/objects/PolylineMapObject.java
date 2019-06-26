package io.anuke.arc.maps.objects;

import io.anuke.arc.maps.MapObject;
import io.anuke.arc.math.geom.Polyline;

/** Represents {@link Polyline} map objects */
public class PolylineMapObject extends MapObject{
    public Polyline polyline;

    /** Creates empty polyline */
    public PolylineMapObject(){
        this(new float[0]);
    }

    /** @param vertices polyline defining vertices */
    public PolylineMapObject(float[] vertices){
        polyline = new Polyline(vertices);
    }

    /** @param polyline the polyline */
    public PolylineMapObject(Polyline polyline){
        this.polyline = polyline;
    }

}
