package arc.maps;

import arc.struct.Seq;

/** Map layer containing a set of MapLayers, objects and properties */
public class MapGroupLayer extends MapLayer{
    public Seq<MapLayer> layers = new Seq<>();

    @Override
    public void invalidateRenderOffset(){
        super.invalidateRenderOffset();
        for(int i = 0; i < layers.size; i++){
            MapLayer child = layers.get(i);
            child.invalidateRenderOffset();
        }
    }
}
