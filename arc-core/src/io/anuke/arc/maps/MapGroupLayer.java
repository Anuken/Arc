package io.anuke.arc.maps;

import io.anuke.arc.collection.Array;

/** Map layer containing a set of MapLayers, objects and properties */
public class MapGroupLayer extends MapLayer{
    public Array<MapLayer> layers = new Array<>();

    @Override
    public void invalidateRenderOffset(){
        super.invalidateRenderOffset();
        for(int i = 0; i < layers.size; i++){
            MapLayer child = layers.get(i);
            child.invalidateRenderOffset();
        }
    }
}
