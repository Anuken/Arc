package io.anuke.arc.maps.tiled.tiles;

import io.anuke.arc.graphics.g2d.TextureRegion;
import io.anuke.arc.maps.MapObjects;
import io.anuke.arc.maps.MapProperties;
import io.anuke.arc.maps.tiled.TiledMapTile;

/** @brief Represents a non changing {@link TiledMapTile} (can be cached) */
public class StaticTiledMapTile implements TiledMapTile{

    private int id;

    private BlendMode blendMode = BlendMode.ALPHA;

    private MapProperties properties;

    private MapObjects objects;

    private TextureRegion textureRegion;

    private float offsetX;

    private float offsetY;

    /**
     * Creates a static tile with the given region
     * @param textureRegion the {@link TextureRegion} to use.
     */
    public StaticTiledMapTile(TextureRegion textureRegion){
        this.textureRegion = textureRegion;
    }

    /**
     * Copy constructor
     * @param copy the StaticTiledMapTile to copy.
     */
    public StaticTiledMapTile(StaticTiledMapTile copy){
        if(copy.properties != null){
            getProperties().putAll(copy.properties);
        }
        this.objects = copy.objects;
        this.textureRegion = copy.textureRegion;
        this.id = copy.id;
    }

    @Override
    public int getId(){
        return id;
    }

    @Override
    public void setId(int id){
        this.id = id;
    }

    @Override
    public BlendMode getBlendMode(){
        return blendMode;
    }

    @Override
    public void setBlendMode(BlendMode blendMode){
        this.blendMode = blendMode;
    }

    @Override
    public MapProperties getProperties(){
        if(properties == null){
            properties = new MapProperties();
        }
        return properties;
    }

    @Override
    public MapObjects getObjects(){
        if(objects == null){
            objects = new MapObjects();
        }
        return objects;
    }

    @Override
    public TextureRegion getTextureRegion(){
        return textureRegion;
    }

    @Override
    public void setTextureRegion(TextureRegion textureRegion){
        this.textureRegion = textureRegion;
    }

    @Override
    public float getOffsetX(){
        return offsetX;
    }

    @Override
    public void setOffsetX(float offsetX){
        this.offsetX = offsetX;
    }

    @Override
    public float getOffsetY(){
        return offsetY;
    }

    @Override
    public void setOffsetY(float offsetY){
        this.offsetY = offsetY;
    }

}
