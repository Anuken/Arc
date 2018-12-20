/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package io.anuke.arc.maps.tiled.objects;

import io.anuke.arc.graphics.g2d.TextureRegion;
import io.anuke.arc.maps.MapObject;
import io.anuke.arc.maps.objects.TextureMapObject;
import io.anuke.arc.maps.tiled.TiledMapTile;
import io.anuke.arc.maps.tiled.tiles.AnimatedTiledMapTile;
import io.anuke.arc.maps.tiled.tiles.StaticTiledMapTile;

/**
 * A {@link MapObject} with a {@link TiledMapTile}. Can be both {@link StaticTiledMapTile} or {@link AnimatedTiledMapTile}. For
 * compatibility reasons, this extends {@link TextureMapObject}. Use {@link TiledMapTile#getTextureRegion()} instead of
 * {@link #getTextureRegion()}.
 * @author Daniel Holderbaum
 */
public class TiledMapTileMapObject extends TextureMapObject{

    private boolean flipHorizontally;
    private boolean flipVertically;

    private TiledMapTile tile;

    public TiledMapTileMapObject(TiledMapTile tile, boolean flipHorizontally, boolean flipVertically){
        this.flipHorizontally = flipHorizontally;
        this.flipVertically = flipVertically;
        this.tile = tile;

        TextureRegion textureRegion = new TextureRegion(tile.getTextureRegion());
        textureRegion.flip(flipHorizontally, flipVertically);
        setTextureRegion(textureRegion);
    }

    public boolean isFlipHorizontally(){
        return flipHorizontally;
    }

    public void setFlipHorizontally(boolean flipHorizontally){
        this.flipHorizontally = flipHorizontally;
    }

    public boolean isFlipVertically(){
        return flipVertically;
    }

    public void setFlipVertically(boolean flipVertically){
        this.flipVertically = flipVertically;
    }

    public TiledMapTile getTile(){
        return tile;
    }

    public void setTile(TiledMapTile tile){
        this.tile = tile;
    }

}
