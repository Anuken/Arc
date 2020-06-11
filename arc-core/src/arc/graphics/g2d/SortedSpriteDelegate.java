package arc.graphics.g2d;

import arc.graphics.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.struct.*;

public class SortedSpriteDelegate extends Batch{
    protected Batch batch;
    protected Seq<DrawRequest> requestPool = new Seq<>(10000);
    protected Seq<DrawRequest> requests = new Seq<>(DrawRequest.class);
    protected boolean sort;
    protected boolean flushing;

    public SortedSpriteDelegate(Batch batch){
        this.batch = batch;
    }

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
        batch.setShader(shader, apply);
    }

    @Override
    protected void setBlending(Blending blending){
        this.blending = blending;
    }

    @Override
    protected void draw(Texture texture, float[] spriteVertices, int offset, int count){
        if(sort && !flushing){
            for(int i = offset; i < count; i += SpriteBatch.SPRITE_SIZE){
                DrawRequest req = obtain();
                req.z = z;
                System.arraycopy(spriteVertices, i, req.vertices, 0, req.vertices.length);
                req.texture = texture;
                req.blending = blending;
                req.run = null;
                requests.add(req);
            }
        }else{
            batch.draw(texture, spriteVertices, offset, count);
        }
    }

    @Override
    protected void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float rotation){
        if(sort && !flushing){
            DrawRequest req = obtain();
            req.x = x;
            req.y = y;
            req.z = z;
            req.originX = originX;
            req.originY = originY;
            req.width = width;
            req.height = height;
            req.color = batch.colorPacked;
            req.rotation = rotation;
            req.region.set(region);
            req.mixColor = batch.mixColorPacked;
            req.blending = batch.blending;
            req.texture = null;
            req.run = null;
            requests.add(req);
        }else{
            batch.draw(region, x, y, originX, originY, width, height, rotation);
        }
    }

    @Override
    protected void draw(Runnable request){
        if(sort && !flushing){
            DrawRequest req = obtain();
            req.run = request;
            req.blending = batch.blending;
            req.mixColor = batch.mixColorPacked;
            req.color = batch.colorPacked;
            req.z = z;
            req.texture = null;
            requests.add(req);
        }else{
            batch.draw(request);
        }
    }

    protected DrawRequest obtain(){
        return requestPool.size > 0 ? requestPool.pop() : new DrawRequest();
    }

    @Override
    protected void flush(){
        flushRequests();
        batch.flush();
    }

    protected void flushRequests(){
        if(!flushing && !requests.isEmpty()){
            flushing = true;
            sortRequests();
            float preColor = batch.colorPacked, preMixColor = batch.mixColorPacked;
            Blending preBlending = batch.blending;

            for(int j = 0; j < requests.size; j++){
                DrawRequest req = requests.items[j];

                batch.colorPacked = req.color;
                batch.mixColorPacked = req.mixColor;

                batch.setBlending(req.blending);

                if(req.run != null){
                    req.run.run();
                }else if(req.texture != null){
                    batch.draw(req.texture, req.vertices, 0, req.vertices.length);
                }else{
                    batch.draw(req.region, req.x, req.y, req.originX, req.originY, req.width, req.height, req.rotation);
                }
            }

            batch.colorPacked = preColor;
            batch.mixColorPacked = preMixColor;
            batch.color.abgr8888(batch.colorPacked);
            batch.mixColor.abgr8888(batch.mixColorPacked);
            batch.blending = preBlending;

            requestPool.addAll(requests);
            requests.size = 0;

            flushing = false;
        }
    }

    protected void sortRequests(){
        requests.sort();
    }

    public void setColor(Color tint){
        batch.setColor(tint);
    }

    public void setColor(float r, float g, float b, float a){
        batch.setColor(r, g, b, a);
    }

    public Color getColor(){
        return batch.getColor();
    }

    public void setPackedColor(float packedColor){
        batch.setPackedColor(packedColor);
    }

    public float getPackedColor(){
        return batch.getPackedColor();
    }

    public void setMixColor(Color tint){
        batch.setMixColor(tint);
    }

    public void setMixColor(float r, float g, float b, float a){
        batch.setMixColor(r, g, b, a);
    }

    public Color getMixColor(){
        return batch.getMixColor();
    }

    public void setPackedMixColor(float packedColor){
        batch.setPackedMixColor(packedColor);
    }

    public float getPackedMixColor(){
        return batch.getPackedMixColor();
    }

    public void dispose(){
        batch.dispose();
    }

    public Mat getProjection(){
        return batch.getProjection();
    }

    public Mat getTransform(){
        return batch.getTransform();
    }

    public void setProjection(Mat projection){
        batch.setProjection(projection);
    }

    public void setTransform(Mat transform){
        batch.setTransform(transform);
    }

    public void setupMatrices(){
        batch.setupMatrices();
    }

    public void switchTexture(Texture texture){
        batch.switchTexture(texture);
    }

    public void setShader(Shader shader){
        batch.setShader(shader);
    }

    public Shader getShader(){
        return batch.getShader();
    }

    public boolean isDisposed(){
        return batch.isDisposed();
    }
}
