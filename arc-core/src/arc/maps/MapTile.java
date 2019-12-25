package arc.maps;

import arc.struct.Array;
import arc.graphics.g2d.TextureRegion;

public class MapTile{
    public int id;
    public TextureRegion region;
    public float offsetX;
    public float offsetY;

    private MapProperties properties;
    private Array<MapObject> objects;

    public MapTile(TextureRegion region){
        this.region = region;
    }

    public MapProperties getProperties(){
        if(properties == null){
            properties = new MapProperties();
        }
        return properties;
    }

    public Array<MapObject> getObjects(){
        if(objects == null){
            objects = new Array<>();
        }
        return objects;
    }

}
