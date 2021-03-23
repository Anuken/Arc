package arc.maps.objects;

import arc.graphics.g2d.TextureRegion;
import arc.maps.MapObject;

/** Represents a map object containing a texture (region) */
public class TextureMapObject extends MapObject{
    public float x = 0.0f;
    public float y = 0.0f;
    public float originX = 0.0f;
    public float originY = 0.0f;
    public float scaleX = 1.0f;
    public float scaleY = 1.0f;
    public float rotation = 0.0f;
    public TextureRegion textureRegion;

    public TextureMapObject(){

    }

    public TextureMapObject(TextureRegion textureRegion){
        this.textureRegion = textureRegion;
    }

}
