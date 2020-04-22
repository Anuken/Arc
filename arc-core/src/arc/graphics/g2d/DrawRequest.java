package arc.graphics.g2d;

import arc.graphics.*;
import arc.util.pooling.Pool.*;

class DrawRequest implements Comparable<DrawRequest>, Poolable{
    TextureRegion region = new TextureRegion();
    float x, y, z, originX, originY, width, height, rotation, color, mixColor;
    float[] vertices = new float[24];
    Texture texture;
    Blending blending;
    Runnable run;

    @Override
    public int compareTo(DrawRequest o){
        return Float.compare(o.z, z);
    }

    @Override
    public void reset(){
        x = y = z = originX = originY = width = height = rotation = color = mixColor = 0f;
        region.texture = null;
        texture = null;
        blending = null;
        run = null;
    }
}