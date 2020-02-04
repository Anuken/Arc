package arc.maps;

import arc.struct.Array;

import java.util.Iterator;

/** Collection of {@link TileSet} */
public class TileSets implements Iterable<TileSet>{
    private Array<TileSet> tilesets;

    /** Creates an empty struct of tilesets. */
    public TileSets(){
        tilesets = new Array<>();
    }

    /**
     * @param index index to get the desired {@link TileSet} at.
     * @return tileset at index
     */
    public TileSet getTileSet(int index){
        return tilesets.get(index);
    }

    /**
     * @param name Name of the {@link TileSet} to retrieve.
     * @return tileset with matching name, null if it doesn't exist
     */
    public TileSet getTileSet(String name){
        for(TileSet tileset : tilesets){
            if(name.equals(tileset.name)){
                return tileset;
            }
        }
        return null;
    }

    /** @param tileset set to be added to the struct */
    public void addTileSet(TileSet tileset){
        tilesets.add(tileset);
    }

    /**
     * Removes tileset at index
     * @param index index at which to remove a tileset.
     */
    public void removeTileSet(int index){
        tilesets.remove(index);
    }

    /** @param tileset set to be removed */
    public void removeTileSet(TileSet tileset){
        tilesets.remove(tileset, true);
    }

    /**
     * @param id id of the {@link MapTile} to get.
     * @return tile with matching id, null if it doesn't exist
     */
    public MapTile getTile(int id){
        // The purpose of backward iteration here is to maintain backwards compatibility
        // with maps created with earlier versions of a shared tileset.  The assumption
        // is that the tilesets are in order of ascending firstgid, and by backward
        // iterating precedence for conflicts is given to later tilesets in the list,
        // which are likely to be the earlier version of any given gid.
        // See TiledMapModifiedExternalTilesetTest for example of this issue.
        for(int i = tilesets.size - 1; i >= 0; i--){
            TileSet tileset = tilesets.get(i);
            MapTile tile = tileset.get(id);
            if(tile != null){
                return tile;
            }
        }
        return null;
    }

    @Override
    public Iterator<TileSet> iterator(){
        return tilesets.iterator();
    }

}
