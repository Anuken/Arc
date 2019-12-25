package arc.maps;

import arc.graphics.g2d.TextureRegion;

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
