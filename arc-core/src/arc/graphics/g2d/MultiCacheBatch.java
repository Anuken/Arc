package arc.graphics.g2d;

import arc.struct.*;
import arc.graphics.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.util.*;

public class MultiCacheBatch extends SpriteBatch{
    private static final int maxSpritesPerCache = 100000;
    Array<SpriteCache> caches = new Array<>();
    Shader shader = SpriteCache.createDefaultShader();
    int currentid = -1;
    int maxCacheSize;
    int offset;
    /** If true, offset of new sprites isn't taken into account, as they have already been drawn and reserved. */
    boolean recaching = false;

    public MultiCacheBatch(int maxCacheSize){
        this.maxCacheSize = maxCacheSize;
    }

    SpriteCache currentCache(){
        int needed = currentid == -1 ? offset / maxSpritesPerCache : currentid;
        if(needed >= caches.size){
            caches.add(new SpriteCache(maxSpritesPerCache, 16, shader, false));
        }
        return caches.get(needed);
    }

    @Override
    public void flush(){
        //does nothing, since flushing like this isn't needed
    }

    @Override
    public void setColor(Color tint){
        currentCache().setColor(tint);
    }

    @Override
    void setColor(float r, float g, float b, float a){
        currentCache().setColor(r, g, b, a);
    }

    @Override
    public void setPackedColor(float color){
        currentCache().setPackedColor(color);
    }

    @Override
    public Color getColor(){
        return currentCache().getColor();
    }

    @Override
    public float getPackedColor(){
        return currentCache().getPackedColor();
    }

    @Override
    public void setProjection(Mat projection){
        currentid = 0;
        currentCache().setProjectionMatrix(projection);
    }

    public void reserve(int amount){
        offset += currentCache().reserve(amount);
    }

    public void beginCache(int id){
        int cacheID = Pack.leftShort(id), batch = Pack.rightShort(id);
        caches.get(batch).beginCache(cacheID);
        currentid = batch;
        recaching = true;
    }

    public void beginCache(){
        currentid = offset / maxSpritesPerCache;
        //reached a possible barrier
        if(currentid < (offset + maxCacheSize) / maxSpritesPerCache){
            offset += (maxCacheSize - (offset % maxCacheSize)) + 2;
            currentid = offset / maxSpritesPerCache;
        }
        currentCache().beginCache();
        recaching = false;
    }

    /** @return the cache ID as two shorts, with the left being the actual cache and the right being the batch that contains it. */
    public int endCache(){
        int id = Pack.shortInt((short)currentCache().endCache(), (short)currentid);
        currentid = -1;
        recaching = false;
        return id;
    }

    @Override
    protected void draw(Texture texture, float[] spriteVertices, int offset, int count){
        //not implemented
    }

    @Override
    protected void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float rotation){
        currentCache().add(region, x, y, originX, originY, width, height, 1f, 1f, rotation);
        if(!recaching){
            offset += 1;
        }
    }

    @Override
    void setShader(Shader shader, boolean apply){
        boolean drawing = currentCache().isDrawing();

        if(drawing) currentCache().end();
        currentCache().setShader(shader);
        if(drawing) currentCache().begin();
        if(apply && shader != null) shader.apply();
    }

    @Override
    public void dispose(){
        super.dispose();
        for(SpriteCache cache : caches){
            cache.dispose();
        }

        shader.dispose();
    }

    public void beginDraw(){
        currentid = 0;
        currentCache().begin();
    }

    public void endDraw(){
        currentCache().end();
        currentid = -1;
    }

    public void drawCache(int id){
        int cacheID = Pack.leftShort(id), batch = Pack.rightShort(id);

        if(currentid != batch){
            SpriteCache prev = currentCache();
            prev.end();
            currentid = batch;
            currentCache().setProjectionMatrix(prev.getProjectionMatrix());
            currentCache().begin();
        }
        currentCache().draw(cacheID);
    }
}
