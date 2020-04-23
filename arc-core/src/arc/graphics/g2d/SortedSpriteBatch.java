package arc.graphics.g2d;

import arc.graphics.*;
import arc.graphics.gl.*;
import arc.struct.*;
import arc.util.pooling.*;

public class SortedSpriteBatch extends SpriteBatch{
    protected Pool<DrawRequest> requestPool = Pools.get(DrawRequest.class, DrawRequest::new, 20000);
    protected Array<DrawRequest> requests = new Array<>();
    protected boolean sort;
    protected boolean flushing;

    @Override
    protected void setSort(boolean sort){
        if(this.sort != sort){
            flush();
        }
        this.sort = sort;
    }

    @Override
    protected void setShader(Shader shader, boolean apply){
        if(!flushing && sort){
            throw new IllegalArgumentException("Shaders cannot be set while sorting is enabled. Set shaders inside Draw.run(...).");
        }
        super.setShader(shader, apply);
    }

    @Override
    protected void setBlending(Blending blending){
        this.blending = blending;
    }

    @Override
    protected void draw(Texture texture, float[] spriteVertices, int offset, int count){
        if(sort && !flushing){
            DrawRequest req = requestPool.obtain();
            req.z = z;
            System.arraycopy(spriteVertices, 0, req.vertices, 0, req.vertices.length);
            req.texture = texture;
            req.blending = blending;
            requests.add(req);
        }else{
            super.draw(texture, spriteVertices, offset, count);
        }
    }

    @Override
    protected void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float rotation){
        if(sort && !flushing){
            DrawRequest req = requestPool.obtain();
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
            req.mixColor = mixColorPacked;
            req.blending = blending;
            requests.add(req);
        }else{
            super.draw(region, x, y, originX, originY, width, height, rotation);
        }
    }

    @Override
    protected void draw(Runnable request){
        if(sort && !flushing){
            DrawRequest req = requestPool.obtain();
            req.run = request;
            req.blending = blending;
            requests.add(req);
        }else{
            super.draw(request);
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
            sortRequests();
            float preColor = colorPacked, preMixColor = mixColorPacked;
            Blending preBlending = blending;

            for(DrawRequest req : requests){
                colorPacked = req.color;
                mixColorPacked = req.mixColor;

                super.setBlending(req.blending);

                if(req.run != null){
                    req.run.run();
                }else if(req.texture != null){
                    super.draw(req.texture, req.vertices, 0, req.vertices.length);
                }else{
                    super.draw(req.region, req.x, req.y, req.originX, req.originY, req.width, req.height, req.rotation);
                }
            }

            colorPacked = preColor;
            mixColorPacked = preMixColor;
            color.abgr8888(colorPacked);
            mixColor.abgr8888(mixColorPacked);
            blending = preBlending;

            Pools.freeAll(requests);
            requests.size = 0;
            flushing = false;
        }
    }

    protected void sortRequests(){
        requests.sort();
    }
}
