package arc.graphics.g2d;

import arc.graphics.*;
import arc.struct.*;
import arc.util.pooling.*;

public class QueueBatch extends SpriteBatch{
    private Array<DrawRequest> requests = new Array<>();
    private boolean flushing;

    /** Set blending without flushing. */
    public void blend(Blending blending){
        this.blending = blending;
    }

    @Override
    protected void draw(Texture texture, float[] spriteVertices, int offset, int count){
        DrawRequest req = Pools.obtain(DrawRequest.class, DrawRequest::new);
        req.z = z;
        System.arraycopy(spriteVertices, 0, req.vertices, 0, req.vertices.length);
        req.texture = texture;
        requests.add(req);
    }

    @Override
    protected void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float rotation){
        DrawRequest req = Pools.obtain(DrawRequest.class, DrawRequest::new);
        req.x = x;
        req.y = y;
        req.z = z;
        req.originX = originX;
        req.originY = originY;
        req.width = width;
        req.height = height;
        req.color = colorPacked;
        req.rotation = rotation;
        req.region.set(region);
        req.blendColor = mixColorPacked;
        requests.add(req);
    }

    @Override
    public void flush(){

        if(!flushing && !requests.isEmpty()){
            flushing = true;

            float preColor = colorPacked, preMixColor = mixColorPacked;

            for(DrawRequest req : requests){
                colorPacked = req.color;
                mixColorPacked = req.blendColor;
                if(req.texture != null){
                    super.draw(req.texture, req.vertices, 0, req.vertices.length);
                }else{
                    super.draw(req.region, req.x, req.y, req.originX, req.originY, req.width, req.height, req.rotation);
                }
            }

            colorPacked = preColor;
            mixColorPacked = preMixColor;
            color.abgr8888(colorPacked);
            mixColor.abgr8888(mixColorPacked);

            Pools.freeAll(requests);
            requests.clear();
            flushing = false;
        }
        super.flush();
    }
}
