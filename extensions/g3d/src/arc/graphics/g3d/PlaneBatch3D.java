package arc.graphics.g3d;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;

/** A SpriteBatch that projects sprites onto a plane in 3D space. */
public class PlaneBatch3D extends SpriteBatch{
    protected final Vec3 up = new Vec3(), right = new Vec3(), origin = new Vec3(), vec = new Vec3();
    protected final VertexBatch3D batch;
    protected final float[] vertex = new float[6]; //format: xyzcuv
    protected float scaling = 1f;

    public PlaneBatch3D(){
        this(5000);
    }

    public PlaneBatch3D(int vertices){
        super(0);
        batch = new VertexBatch3D(vertices, false, true, 1);
    }

    /** Sets scaling of sprite units. */
    public void setScaling(float scaling){
        this.scaling = scaling;
    }

    /** Sets the plane that this batch projects sprites onto. */
    public void setPlane(Vec3 origin, Vec3 up, Vec3 right){
        this.origin.set(origin);
        this.up.set(up).nor();
        this.right.set(right).nor();
    }

    public void proj(Mat3D mat){
        batch.proj(mat);
    }

    @Override
    protected void flush(){
        if(lastTexture == null || idx == 0){
            return;
        }

        //disable depth mask when flushing to prevent Z fighting
        Gl.depthMask(false);

        lastTexture.bind();
        batch.flush(Gl.triangles);
        idx = 0;

        Gl.depthMask(true);
    }

    @Override
    protected void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float rotation){

        Texture texture = region.getTexture();
        if(texture != lastTexture){
            switchTexture(texture);
        }

        checkFlush();

        //if(!Mathf.zero(rotation)){
            //bottom left and top right corner points relative to origin
        final float worldOriginX = x + originX;
        final float worldOriginY = y + originY;
        float fx = -originX;
        float fy = -originY;
        float fx2 = width - originX;
        float fy2 = height - originY;

        float x1;
        float y1;
        float x2;
        float y2;
        float x3;
        float y3;
        float x4;
        float y4;

        // rotate
        final float cos = Mathf.cosDeg(rotation);
        final float sin = Mathf.sinDeg(rotation);

        x1 = cos * fx - sin * fy;
        y1 = sin * fx + cos * fy;

        x2 = cos * fx - sin * fy2;
        y2 = sin * fx + cos * fy2;

        x3 = cos * fx2 - sin * fy2;
        y3 = sin * fx2 + cos * fy2;

        x4 = x1 + (x3 - x2);
        y4 = y3 - (y2 - y1);

        x1 += worldOriginX;
        y1 += worldOriginY;
        x2 += worldOriginX;
        y2 += worldOriginY;
        x3 += worldOriginX;
        y3 += worldOriginY;
        x4 += worldOriginX;
        y4 += worldOriginY;

        final float u = region.getU();
        final float v = region.getV2();
        final float u2 = region.getU2();
        final float v2 = region.getV();
        final float color = this.colorPacked;

        vertex(x2, y2, color, u, v2);
        vertex(x1, y1, color, u, v);
        vertex(x3, y3, color, u2, v2);

        vertex(x4, y4, color, u2, v);
        vertex(x3, y3, color, u2, v2);
        vertex(x1, y1, color, u, v);

        idx ++;
    }

    @Override
    protected void draw(Texture texture, float[] v, int offset, int count){
        if(texture != lastTexture){
            switchTexture(texture);
        }

        for(int i = offset; i < count; i += SPRITE_SIZE){
            checkFlush();

            vertex(v[i], v[i + 1], v[i + 2], v[i + 3], v[i + 4]);
            vertex(v[i + 12], v[i + 13], v[i + 14], v[i + 15], v[i + 16]);
            vertex(v[i + 6], v[i + 7], v[i + 8], v[i + 9], v[i + 10]);

            vertex(v[i + 12], v[i + 13], v[i + 14], v[i + 15], v[i + 16]);
            vertex(v[i], v[i + 1], v[i + 2], v[i + 3], v[i + 4]);
            vertex(v[i + 18], v[i + 19], v[i + 20], v[i + 21], v[i + 22]);

            idx ++;
        }
    }

    private void checkFlush(){
        if(idx >= batch.getMaxVertices() / 6 / 6){
            flush();
        }
    }

    private void vertex(float x1, float y1, float c1, float u1, float v1){
        vec.set(origin).add(right, x1 * scaling).add(up, y1 * scaling);
        vertex[0] = vec.x;
        vertex[1] = vec.y;
        vertex[2] = vec.z;
        vertex[3] = c1;
        vertex[4] = u1;
        vertex[5] = v1;
        batch.vertex(vertex);
    }
}
