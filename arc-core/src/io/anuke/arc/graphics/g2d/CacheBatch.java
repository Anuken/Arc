package io.anuke.arc.graphics.g2d;

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

    public int flushCache(){
        cache.beginCache();
        for(int i = 0; i < rectAmount; i++){
            BatchRect rect = rects.get(i);
            cache.setPackedColor(rect.color);
            cache.add(rect.region, rect.x, rect.y, rect.originX, rect.originY, rect.width, rect.height, rect.scaleX, rect.scaleY, rect.rotation);
        }
        return cache.endCache();
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
