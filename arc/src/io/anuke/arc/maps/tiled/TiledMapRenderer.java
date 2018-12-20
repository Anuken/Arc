package io.anuke.arc.maps.tiled;

import io.anuke.arc.maps.MapLayer;
import io.anuke.arc.maps.MapObject;
import io.anuke.arc.maps.MapRenderer;

public interface TiledMapRenderer extends MapRenderer{
    void renderObjects(MapLayer layer);

    void renderObject(MapObject object);

    void renderTileLayer(TiledMapTileLayer layer);

    void renderImageLayer(TiledMapImageLayer layer);
}
