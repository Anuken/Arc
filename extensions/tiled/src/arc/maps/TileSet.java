package arc.maps;

import arc.struct.IntMap;

import java.util.Iterator;

/** Set of {@link MapTile} instances used to compose a TiledMapLayer */
public class TileSet implements Iterable<MapTile>{
    public String name;
    public MapProperties properties = new MapProperties();
    private IntMap<MapTile> tiles = new IntMap<>();

    /** @return tileset's properties set */
    public MapProperties getProperties(){
        return properties;
    }

    /**
     * Gets the {@link MapTile} that has the given id.
     * @param id the id of the {@link MapTile} to retrieve.
     * @return tile matching id, null if it doesn't exist
     */
    public MapTile get(int id){
        return tiles.get(id);
    }

    /** @return iterator to tiles in this tileset */
    @Override
    public Iterator<MapTile> iterator(){
        return tiles.values().iterator();
    }

    /**
     * Adds or replaces tile with that id
     * @param id the id of the {@link MapTile} to add or replace.
     * @param tile the {@link MapTile} to add or replace.
     */
    public void put(int id, MapTile tile){
        tiles.put(id, tile);
    }

    /** @param id tile's id to be removed */
    public void remove(int id){
        tiles.remove(id);
    }

    /** @return the size of this TiledMapTileSet. */
    public int size(){
        return tiles.size;
    }
}
