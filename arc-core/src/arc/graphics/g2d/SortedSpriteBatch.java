package arc.graphics.g2d;

import arc.graphics.*;
import arc.struct.*;
import arc.util.pooling.*;

public class SortedSpriteBatch extends SpriteBatch{
    private Array<DrawRequest> requests = new Array<>();
    private boolean sort;
    private boolean flushing;

    public void sort(boolean sort){
        if(this.sort != sort){
            flush();
        }
        this.sort = sort;
    }

    @Override
    protected void draw(Texture texture, float[] spriteVertices, int offset, int count){
        if(sort){
            DrawRequest req = Pools.obtain(DrawRequest.class, DrawRequest::new);
            req.z = z;
            System.arraycopy(spriteVertices, 0, req.vertices, 0, req.vertices.length);
            req.texture = texture;
            requests.add(req);
        }else{
            super.draw(texture, spriteVertices, offset, count);
        }
    }

    @Override
    protected void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float rotation){
        if(sort){
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
            req.region = region;
            req.blendColor = mixColorPacked;
            requests.add(req);
        }else{
            super.draw(region, x, y, originX, originY, width, height, rotation);
        }
    }

    @Override
    protected void flush(){
        flushRequests();
        super.flush();
    }

    protected void flushRequests(){
        if(!flushing && !requests.isEmpty()){
            flushing = true;
            requests.sort();

            for(DrawRequest req : requests){
                colorPacked = req.color;
                mixColorPacked = req.blendColor;
                if(req.texture != null){
                    super.draw(req.texture, req.vertices, 0, req.vertices.length);
                }else{
                    super.draw(req.region, req.x, req.y, req.originX, req.originY, req.width, req.height, req.rotation);
                }
            }

            Pools.freeAll(requests);
            requests.clear();
            flushing = false;
        }
    }
}
