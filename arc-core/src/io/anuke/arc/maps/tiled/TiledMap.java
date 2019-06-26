package io.anuke.arc.maps.tiled;

import io.anuke.arc.assets.AssetManager;
import io.anuke.arc.collection.Array;
import io.anuke.arc.maps.*;
import io.anuke.arc.util.Disposable;

/** Represents a tiled map, adds the concept of tiles and tilesets.*/
public class TiledMap implements Disposable{
    public final Array<MapLayer> layers = new Array<>();
    public final MapProperties properties = new MapProperties();
    public final TileSets tilesets = new TileSets();

    private Array<? extends Disposable> ownedResources;

    /**
     * Used by loaders to set resources when loading the map directly, without {@link AssetManager}. To be disposed in
     * {@link #dispose()}.
     */
    public void setOwnedResources(Array<? extends Disposable> resources){
        this.ownedResources = resources;
    }

    @SuppressWarnings("unchecked")
    public <T extends MapLayer> T getLayer(String name){
        return (T)layers.find(l -> name.equals(l.name));
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
