package arc.maps;

import arc.struct.Seq;
import arc.graphics.g2d.TextureRegion;

public class MapTile{
    public int id;
    public TextureRegion region;
    public float offsetX;
    public float offsetY;

    private MapProperties properties;
    private Seq<MapObject> objects;

    public MapTile(TextureRegion region){
        this.region = region;
    }

    public MapProperties getProperties(){
        if(properties == null){
            properties = new MapProperties();
        }
        return properties;
    }

    public Seq<MapObject> getObjects(){
        if(objects == null){
            objects = new Seq<>();
        }
        return objects;
    }

}
