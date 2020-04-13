package arc.graphics.vector;

import java.util.*;

public class Scissor{
    final AffineTransform xform = AffineTransform.obtain();
    final float[] extent = new float[]{-1.0f, -1.0f};

    public void set(Scissor other){
        xform.set(other.xform);
        extent[0] = other.extent[0];
        extent[1] = other.extent[1];
    }

    public void set(float x, float y, float width, float height, AffineTransform canvasXform){
        AffineTransform scissorXform = xform;

        float halfWidth = Math.max(0.0f, width) * 0.5f;
        float halfHeight = Math.max(0.0f, height) * 0.5f;

        scissorXform.idt();
        scissorXform.values[4] = x + halfWidth;
        scissorXform.values[5] = y + halfHeight;
        scissorXform.mul(canvasXform);

        float[] scissorExtent = extent;
        scissorExtent[0] = halfWidth;
        scissorExtent[1] = halfHeight;
    }

    public void intersect(float x, float y, float width, float height, AffineTransform canvasXform){
        // If no previous scissor has been set, set the scissor as current
        // scissor.
        if(extent[0] < 0){
            set(x, y, width, height, canvasXform);
            return;
        }

        // Transform the current scissor rect into current transform space.
        // If there is difference in rotation, this will be approximation.
        AffineTransform pxform = AffineTransform.obtain();
        AffineTransform invxorm = AffineTransform.obtain();
        pxform.set(xform);
        float ex = extent[0];
        float ey = extent[1];
        canvasXform.inv(invxorm);
        invxorm.mul(pxform);
        float tex = ex * Math.abs(pxform.values[0]) + ey * Math.abs(pxform.values[2]);
        float tey = ex * Math.abs(pxform.values[1]) + ey * Math.abs(pxform.values[3]);

        // Intersect rects.
        float x0 = pxform.values[4] - tex;
        float y0 = pxform.values[5] - tey;

        float minx = Math.max(x0, x);
        float miny = Math.max(y0, y);
        float maxx = Math.min(x0 + tex * 2, x + width);
        float maxy = Math.min(y0 + tey * 2, y + height);

        set(minx, miny, Math.max(0.0f, maxx - minx), Math.max(0.0f, maxy - miny), canvasXform);
        pxform.free();
        invxorm.free();
    }

    public void reset(){
        xform.reset();
        extent[0] = -1.0f;
        extent[1] = -1.0f;
    }

    @Override
    public int hashCode(){
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(extent[0]);
        result = prime * result + Float.floatToIntBits(extent[1]);
        result = prime * result + xform.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        Scissor other = (Scissor)obj;
        return Arrays.equals(extent, other.extent) && xform.equals(other.xform);
    }
}
