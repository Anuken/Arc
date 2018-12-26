package io.anuke.arc.maps.tiled;

import io.anuke.arc.graphics.g2d.TextureRegion;
import io.anuke.arc.maps.MapObjects;
import io.anuke.arc.maps.MapProperties;

/** @brief Generalises the concept of tile in a TiledMap */
public interface TiledMapTile{

    int getId();

    void setId(int id);

    /** @return the {@link BlendMode} to use for rendering the tile */
    BlendMode getBlendMode();

    /**
     * Sets the {@link BlendMode} to use for rendering the tile
     * @param blendMode the blend mode to use for rendering the tile
     */
    void setBlendMode(BlendMode blendMode);

    /** @return texture region used to render the tile */
    TextureRegion getTextureRegion();

    /** Sets the texture region used to render the tile */
    void setTextureRegion(TextureRegion textureRegion);

    /** @return the amount to offset the x position when rendering the tile */
    float getOffsetX();

    /** Set the amount to offset the x position when rendering the tile */
    void setOffsetX(float offsetX);

    /** @return the amount to offset the y position when rendering the tile */
    float getOffsetY();

    /** Set the amount to offset the y position when rendering the tile */
    void setOffsetY(float offsetY);

    /** @return tile's properties set */
    MapProperties getProperties();

    /** @return collection of objects contained in the tile */
    MapObjects getObjects();

    enum BlendMode{
        NONE, ALPHA
    }

}
