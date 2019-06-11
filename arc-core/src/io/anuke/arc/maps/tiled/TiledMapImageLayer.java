package io.anuke.arc.maps.tiled;

import io.anuke.arc.graphics.g2d.TextureRegion;
import io.anuke.arc.maps.MapLayer;

public class TiledMapImageLayer extends MapLayer{

    private TextureRegion region;

    private float x;
    private float y;

    public TiledMapImageLayer(TextureRegion region, float x, float y){
        this.region = region;
        this.x = x;
        this.y = y;
    }

    public TextureRegion getTextureRegion(){
        return region;
    }

    public void setTextureRegion(TextureRegion region){
        this.region = region;
    }

    public float getX(){
        return x;
    }

    public void setX(float x){
        this.x = x;
    }

    public float getY(){
        return y;
    }

    public void setY(float y){
        this.y = y;
    }

}
