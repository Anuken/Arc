package arc.maps.objects;

import arc.graphics.g2d.TextureRegion;
import arc.maps.*;

/**
 * A {@link MapObject} with a {@link MapTile}.
 */
public class TileMapObject extends TextureMapObject{
    public boolean flipHorizontally;
    public boolean flipVertically;
    public MapTile tile;

    public TileMapObject(MapTile tile, boolean flipHorizontally, boolean flipVertically){
        this.flipHorizontally = flipHorizontally;
        this.flipVertically = flipVertically;
        this.tile = tile;

        TextureRegion textureRegion = new TextureRegion(tile.region);
        textureRegion.flip(flipHorizontally, flipVertically);
        this.textureRegion = textureRegion;
    }

}
