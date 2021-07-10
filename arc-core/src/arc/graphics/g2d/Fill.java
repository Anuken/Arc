package arc.graphics.g2d;

import arc.*;
import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;

import static arc.Core.atlas;

public class Fill{
    private static float[] vertices = new float[24];
    private static TextureRegion circleRegion;
    private static FloatSeq polyFloats = new FloatSeq();

    public static void quad(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4){
        float color = Core.batch.getPackedColor();
        quad(x1, y1, color, x2, y2, color, x3, y3, color, x4, y4, color);
    }

    public static void quad(float x1, float y1, float c1, float x2, float y2, float c2, float x3, float y3, float c3, float x4, float y4, float c4){
        TextureRegion region = atlas.white();
        float mcolor = Core.batch.getPackedMixColor();
        float u = region.u;
        float v = region.v;
        vertices[0] = x1;
        vertices[1] = y1;
        vertices[2] = c1;
        vertices[3] = u;
        vertices[4] = v;
        vertices[5] = mcolor;

        vertices[6] = x2;
        vertices[7] = y2;
        vertices[8] = c2;
        vertices[9] = u;
        vertices[10] = v;
        vertices[11] = mcolor;

        vertices[12] = x3;
        vertices[13] = y3;
        vertices[14] = c3;
        vertices[15] = u;
        vertices[16] = v;
        vertices[17] = mcolor;

        vertices[18] = x4;
        vertices[19] = y4;
        vertices[20] = c4;
        vertices[21] = u;
        vertices[22] = v;
        vertices[23] = mcolor;

        Draw.vert(region.texture, vertices, 0, vertices.length);
    }

    public static void quad(TextureRegion region, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4){
        float u = region.u, v = region.v, u2 = region.u2, v2 = region.v2, color = Core.batch.getPackedColor(), mcolor = Core.batch.getPackedMixColor();

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
        vertices[10] = v2;
        vertices[11] = mcolor;

        vertices[12] = x3;
        vertices[13] = y3;
        vertices[14] = color;
        vertices[15] = u2;
        vertices[16] = v2;
        vertices[17] = mcolor;

        vertices[18] = x4;
        vertices[19] = y4;
        vertices[20] = color;
        vertices[21] = u2;
        vertices[22] = v;
        vertices[23] = mcolor;

        Draw.vert(region.texture, vertices, 0, vertices.length);
    }

    public static void tri(float x1, float y1, float x2, float y2, float x3, float y3){
        quad(x1, y1, x2, y2, x3, y3, x3, y3);
    }

    /**
     * Draws an uncentered drop shadow.
     * @param x shadow bottom left X
     * @param y shadow bottom left Y
     * */
    public static void dropShadowRect(float x, float y, float width, float height, float blur, float opacity){
        dropShadow(x + width/2f, y + height/2f, width, height, blur, opacity);
    }

    /**
     * Draws a centered drop shadow.
     * @param blur shadow size in units
     * @param opacity shadow opacity
     * @param x shadow center X
     * @param y shadow center Y
     * */
    public static void dropShadow(float x, float y, float width, float height, float blur, float opacity){
        float edge = Color.clearFloatBits;
        float center = Color.toFloatBits(0, 0, 0, opacity);
        float inside = blur/2f, outside = blur;

        float x1 = x - Math.max(width/2f - inside, 0), y1 = y - Math.max(height/2f - inside, 0), x2 = x + Math.max(width/2f - inside, 0), y2 = y + Math.max(height/2f - inside, 0);
        float bx1 = x1 - outside, by1 = y1 - outside, bx2 = x2 + outside, by2 = y2 + outside;

        //center
        quad(x1, y1, center, x2, y1, center, x2, y2, center, x1, y2, center);

        //bottom
        quad(
        x1, y1, center,
        bx1, by1, edge,
        bx2, by1, edge,
        x2, y1, center
        );

        //right
        quad(
        x2, y1, center,
        bx2, by1, edge,
        bx2, by2, edge,
        x2, y2, center
        );

        //top
        quad(
        x1, y2, center,
        bx1, by2, edge,
        bx2, by2, edge,
        x2, y2, center
        );

        //left
        quad(
        x1, y1, center,
        bx1, by1, edge,
        bx1, by2, edge,
        x1, y2, center
        );
    }

    public static void light(float x, float y, int sides, float radius, Color center, Color edge){
        float centerf = center.toFloatBits(), edgef = edge.toFloatBits();

        sides = Mathf.ceil(sides / 2f) * 2;

        float space = 360f / sides;

        for(int i = 0; i < sides; i += 2){
            float px = Angles.trnsx(space * i, radius);
            float py = Angles.trnsy(space * i, radius);
            float px2 = Angles.trnsx(space * (i + 1), radius);
            float py2 = Angles.trnsy(space * (i + 1), radius);
            float px3 = Angles.trnsx(space * (i + 2), radius);
            float py3 = Angles.trnsy(space * (i + 2), radius);
            quad(x, y, centerf, x + px, y + py, edgef, x + px2, y + py2, edgef, x + px3, y + py3, edgef);
        }
    }

    public static void polyBegin(){
        polyFloats.clear();
    }

    public static void polyPoint(float x, float y){
        polyFloats.add(x, y);
    }

    public static void polyEnd(){
        poly(polyFloats.items, polyFloats.size);
    }

    public static void poly(Polygon p){
        poly(p.getTransformedVertices(), p.getTransformedVertices().length);
    }

    public static void poly(FloatSeq vertices){
        poly(vertices.items, vertices.size);
    }

    public static void poly(float[] vertices, int length){
        if(length < 2*3) return;

        //TODO untested
        for(int i = 2; i < length - 4; i+= 6){
            quad(
            vertices[0], vertices[1],
            vertices[i], vertices[i + 1],
            vertices[i + 2], vertices[i + 3],
            vertices[i + 4], vertices[i + 5]
            );
        }
    }

    public static void poly(float x, float y, int sides, float radius){
        poly(x, y, sides, radius, 0f);
    }

    public static void poly(float x, float y, int sides, float radius, float rotation){
        if(sides == 3){
            float space = 360f / 3;

            float px = Angles.trnsx(rotation, radius);
            float py = Angles.trnsy(rotation, radius);
            float px2 = Angles.trnsx(space + rotation, radius);
            float py2 = Angles.trnsy(space + rotation, radius);
            float px3 = Angles.trnsx(space * (2) + rotation, radius);
            float py3 = Angles.trnsy(space * (2) + rotation, radius);
            tri(x + px, y + py, x + px2, y + py2, x + px3, y + py3);
        }else{

            float space = 360f / sides;

            for(int i = 0; i < sides - 1; i += 2){
                float px = Angles.trnsx(space * i + rotation, radius);
                float py = Angles.trnsy(space * i + rotation, radius);
                float px2 = Angles.trnsx(space * (i + 1) + rotation, radius);
                float py2 = Angles.trnsy(space * (i + 1) + rotation, radius);
                float px3 = Angles.trnsx(space * (i + 2) + rotation, radius);
                float py3 = Angles.trnsy(space * (i + 2) + rotation, radius);
                quad(x, y, x + px, y + py, x + px2, y + py2, x + px3, y + py3);
            }

            int mod = sides % 2;

            if(mod == 0 || sides < 4) return;

            int i = sides - 1;

            float px = Angles.trnsx(space * i + rotation, radius);
            float py = Angles.trnsy(space * i + rotation, radius);
            float px2 = Angles.trnsx(space * (i + 1) + rotation, radius);
            float py2 = Angles.trnsy(space * (i + 1) + rotation, radius);
            tri(x, y, x + px, y + py, x + px2, y + py2);
        }
    }

    public static void circle(Circle c){
        circle(c.x, c.y, c.radius);
    }

    public static void circle(float x, float y, float radius){
        if(circleRegion == null || circleRegion.texture.isDisposed()){
            circleRegion = atlas.find("circle");
        }

        Draw.rect(circleRegion, x, y, radius * 2, radius * 2);
    }

    public static void rect(Rect r){
        rect(r.x, r.y, r.width, r.height);
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

    public static void rects(float x, float y, float w, float h, float skew){
        quad(x, y, x + w, y, x + w + skew, y + h, x + skew, y + h);
    }

    public static void square(float x, float y, float radius){
        rect(x, y, radius*2, radius*2);
    }

    public static void square(float x, float y, float radius, float rotation){
        Draw.rect(atlas.white(), x, y, radius*2, radius*2, rotation);
    }
}
