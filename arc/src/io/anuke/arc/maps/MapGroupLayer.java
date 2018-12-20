package io.anuke.arc.maps;

/** Map layer containing a set of MapLayers, objects and properties */
public class MapGroupLayer extends MapLayer{

    private MapLayers layers = new MapLayers();

    /**
     * @return the {@link MapLayers} owned by this group
     */
    public MapLayers getLayers(){
        return layers;
    }

    @Override
    public void invalidateRenderOffset(){
        super.invalidateRenderOffset();
        for(int i = 0; i < layers.size(); i++){
            MapLayer child = layers.get(i);
            child.invalidateRenderOffset();
        }
    }
}
