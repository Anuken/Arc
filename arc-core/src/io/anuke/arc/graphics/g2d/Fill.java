package io.anuke.arc.graphics.g2d;

import io.anuke.arc.Core;
import io.anuke.arc.math.Angles;

import static io.anuke.arc.Core.atlas;

public class Fill{
    private static float[] vertices = new float[24];
    private static TextureRegion circleRegion;

    public static void quad(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4){
        TextureRegion region = atlas.white();
        float color = Core.batch.getPackedColor();
        float mcolor = Core.batch.getPackedMixColor();
        float u = region.getU();
        float v = region.getV();
        vertices[0] = x1;
        vertices[1] = y1;
        vertices[2] = color;
        vertices[3] = u;
        vertices[4] = v;
        vertices[5] = mcolor;

        vertices[6] = x2;
        vertices[7] = y2;
        vertices[8] = color;
        vertices[9] = u;
        vertices[10] = v;
        vertices[11] = mcolor;

        vertices[12] = x3;
        vertices[13] = y3;
        vertices[14] = color;
        vertices[15] = u;
        vertices[16] = v;
        vertices[17] = mcolor;

        vertices[18] = x4;
        vertices[19] = y4;
        vertices[20] = color;
        vertices[21] = u;
        vertices[22] = v;
        vertices[23] = mcolor;

        Draw.vert(region.getTexture(), vertices, 0, vertices.length);
    }

    public static void tri(float x1, float y1, float x2, float y2, float x3, float y3){
        quad(x1, y1, x2, y2, x3, y3, x3, y3);
    }

    public static void poly(float x, float y, int sides, float radius){
        poly(x, y, sides, radius, 0f);
    }

    public static void poly(float x, float y, int sides, float radius, float rotation){
        float space = 360f / sides;

        for(int i = 0; i < sides - 2; i += 3){
            float px = Angles.trnsx(space * i + rotation, radius);
            float py = Angles.trnsy(space * i + rotation, radius);
            float px2 = Angles.trnsx(space * (i + 1) + rotation, radius);
            float py2 = Angles.trnsy(space * (i + 1) + rotation, radius);
            float px3 = Angles.trnsx(space * (i + 2) + rotation, radius);
            float py3 = Angles.trnsy(space * (i + 2) + rotation, radius);
            float px4 = Angles.trnsx(space * (i + 3) + rotation, radius);
            float py4 = Angles.trnsy(space * (i + 3) + rotation, radius);
            quad(x + px, y + py, x + px2, y + py2, x + px3, y + py3, x + px4, y + py4);
        }

        int mod = sides % 3;

        for(int i = sides - mod - 1; i < sides; i++){
            float px = Angles.trnsx(space * i + rotation, radius);
            float py = Angles.trnsy(space * i + rotation, radius);
            float px2 = Angles.trnsx(space * (i + 1) + rotation, radius);
            float py2 = Angles.trnsy(space * (i + 1) + rotation, radius);
            tri(x, y, x + px, y + py, x + px2, y + py2);
        }
    }

    public static void circle(float x, float y, float radius){
        if(circleRegion == null){
            circleRegion = atlas.find("circle");
        }

        Draw.rect(circleRegion, x, y, radius * 2, radius * 2);
    }

    public static void rect(float x, float y, float w, float h){
        Draw.rect(atlas.white(), x, y, w, h);
    }

    public static void rect(float x, float y, float w, float h, float rot){
        Draw.rect(atlas.white(), x, y, w, h, rot);
    }

    public static void crect(float x, float y, float w, float h){
        Draw.rect(atlas.white(), x + w/2f, y + h/2f, w, h);
    }

    public static void square(float x, float y, float radius){
        rect(x, y, radius*2, radius*2);
    }

    public static void square(float x, float y, float radius, float rotation){
        Draw.rect(atlas.white(), x, y, radius*2, radius*2, rotation);
    }
}
