package io.anuke.arc.maps.tiled;

import io.anuke.arc.assets.AssetManager;
import io.anuke.arc.collection.Array;
import io.anuke.arc.maps.Map;
import io.anuke.arc.utils.Disposable;

/**
 * @brief Represents a tiled map, adds the concept of tiles and tilesets.
 * @see Map
 */
public class TiledMap extends Map{
    private TiledMapTileSets tilesets;
    private Array<? extends Disposable> ownedResources;

    /** Creates an empty TiledMap. */
    public TiledMap(){
        tilesets = new TiledMapTileSets();
    }

    /** @return collection of tilesets for this map. */
    public TiledMapTileSets getTileSets(){
        return tilesets;
    }

    /**
     * Used by loaders to set resources when loading the map directly, without {@link AssetManager}. To be disposed in
     * {@link #dispose()}.
     */
    public void setOwnedResources(Array<? extends Disposable> resources){
        this.ownedResources = resources;
    }

    @Override
    public void dispose(){
        if(ownedResources != null){
            for(Disposable resource : ownedResources){
                resource.dispose();
            }
        }
    }
}
