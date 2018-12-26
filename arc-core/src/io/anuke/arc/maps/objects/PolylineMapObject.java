package io.anuke.arc.maps.objects;

import io.anuke.arc.maps.MapObject;
import io.anuke.arc.math.geom.Polyline;

/** @brief Represents {@link Polyline} map objects */
public class PolylineMapObject extends MapObject{

    private Polyline polyline;

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

    /** @return polyline shape */
    public Polyline getPolyline(){
        return polyline;
    }

    /** @param polyline new object's polyline shape */
    public void setPolyline(Polyline polyline){
        this.polyline = polyline;
    }

}
