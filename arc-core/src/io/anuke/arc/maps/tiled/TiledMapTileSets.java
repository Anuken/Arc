package io.anuke.arc.maps.tiled;

import io.anuke.arc.collection.Array;

import java.util.Iterator;

/** @brief Collection of {@link TiledMapTileSet} */
public class TiledMapTileSets implements Iterable<TiledMapTileSet>{

    private Array<TiledMapTileSet> tilesets;

    /** Creates an empty collection of tilesets. */
    public TiledMapTileSets(){
        tilesets = new Array<>();
    }

    /**
     * @param index index to get the desired {@link TiledMapTileSet} at.
     * @return tileset at index
     */
    public TiledMapTileSet getTileSet(int index){
        return tilesets.get(index);
    }

    /**
     * @param name Name of the {@link TiledMapTileSet} to retrieve.
     * @return tileset with matching name, null if it doesn't exist
     */
    public TiledMapTileSet getTileSet(String name){
        for(TiledMapTileSet tileset : tilesets){
            if(name.equals(tileset.getName())){
                return tileset;
            }
        }
        return null;
    }

    /** @param tileset set to be added to the collection */
    public void addTileSet(TiledMapTileSet tileset){
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
    public void removeTileSet(TiledMapTileSet tileset){
        tilesets.removeValue(tileset, true);
    }

    /**
     * @param id id of the {@link TiledMapTile} to get.
     * @return tile with matching id, null if it doesn't exist
     */
    public TiledMapTile getTile(int id){
        // The purpose of backward iteration here is to maintain backwards compatibility
        // with maps created with earlier versions of a shared tileset.  The assumption
        // is that the tilesets are in order of ascending firstgid, and by backward
        // iterating precedence for conflicts is given to later tilesets in the list,
        // which are likely to be the earlier version of any given gid.
        // See TiledMapModifiedExternalTilesetTest for example of this issue.
        for(int i = tilesets.size - 1; i >= 0; i--){
            TiledMapTileSet tileset = tilesets.get(i);
            TiledMapTile tile = tileset.getTile(id);
            if(tile != null){
                return tile;
            }
        }
        return null;
    }

    /** @return iterator to tilesets */
    @Override
    public Iterator<TiledMapTileSet> iterator(){
        return tilesets.iterator();
    }

}
