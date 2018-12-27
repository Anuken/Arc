package io.anuke.arc.graphics.g2d;

import io.anuke.arc.graphics.Color;
import io.anuke.arc.graphics.Texture;
import io.anuke.arc.math.Matrix3;

public class CacheBatch extends SpriteBatch{
    SpriteCache cache;

    public CacheBatch(int size){
        super(0);
        cache = new SpriteCache(size, false);
    }

    @Override
    public void flush(){
        //does nothing, since flushing like this isn't needed
    }

    @Override
    public void setColor(Color tint){
        cache.setColor(tint);
    }

    @Override
    public void setProjection(Matrix3 projection){
        cache.setProjectionMatrix(projection);
    }

    public void beginCache(){
        cache.beginCache();
    }

    public int endCache(){
        return cache.endCache();
    }

    @Override
    void draw(Texture texture, float[] spriteVertices, int offset, int count){
        cache.add(texture, spriteVertices, offset, count);
    }

    @Override
    void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float rotation){
        cache.add(region, x, y, originX, originY, width, height, 1f, 1f, rotation);
    }

    public void beginDraw(){
        cache.begin();
    }

    public void endDraw(){
        cache.end();
    }

    public void drawCache(int id){
        cache.draw(id);
    }
}
