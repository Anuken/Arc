package io.anuke.arc.maps;

import io.anuke.arc.graphics.g2d.TextureRegion;
import io.anuke.arc.maps.MapLayer;

public class ImageLayer extends MapLayer{
    public TextureRegion region;
    public float x;
    public float y;

    public ImageLayer(TextureRegion region, float x, float y){
        this.region = region;
        this.x = x;
        this.y = y;
    }

}
