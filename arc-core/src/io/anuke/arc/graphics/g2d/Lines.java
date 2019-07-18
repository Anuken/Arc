package io.anuke.arc.graphics.g2d;

import io.anuke.arc.Core;
import io.anuke.arc.collection.FloatArray;
import io.anuke.arc.graphics.Color;
import io.anuke.arc.math.Angles;
import io.anuke.arc.math.Mathf;
import io.anuke.arc.math.geom.Rectangle;
import io.anuke.arc.math.geom.Vector2;

public class Lines{
    private static float stroke = 1f;
    private static Vector2 vector = new Vector2();
    private static FloatArray floats = new FloatArray(20);
    private static FloatArray floatBuilder = new FloatArray(20);
    private static float[] points = new float[8];
    private static boolean building;
    private static int circleVertices = 30;

    /** Set the vertices used for drawing a line circle. */
    public static void setCircleVertices(int amount){
        circleVertices = amount;
    }

    public static void lineAngle(float x, float y, float angle, float length, CapStyle style){
        vector.set(1, 1).setLength(length).setAngle(angle);

        line(x, y, x + vector.x, y + vector.y, style);
    }

    public static void lineAngle(float x, float y, float angle, float length){
        vector.set(1, 1).setLength(length).setAngle(angle);

        line(x, y, x + vector.x, y + vector.y);
    }

    public static void lineAngle(float x, float y, float offset, float angle, float length){
        vector.set(1, 1).setLength(length + offset).setAngle(angle);

        line(x, y, x + vector.x, y + vector.y);
    }

    public static void lineAngleCenter(float x, float y, float angle, float length){
        vector.set(1, 1).setLength(length).setAngle(angle);

        line(x - vector.x / 2, y - vector.y / 2, x + vector.x / 2, y + vector.y / 2);
    }

    public static void line(float x, float y, float x2, float y2){
        line(x, y, x2, y2, CapStyle.square, 0f);
    }

    public static void line(float x, float y, float x2, float y2, CapStyle cap){
        line(x, y, x2, y2, cap, 0f);
    }

    public static void line(float x, float y, float x2, float y2, CapStyle cap, float padding){
        line(Core.atlas.white(), x, y, x2, y2, cap, padding);
    }

    public static void line(TextureRegion region, float x, float y, float x2, float y2, CapStyle cap, float padding){
        float length = Mathf.dst(x, y, x2, y2) + (cap == CapStyle.none || cap == CapStyle.round ? padding * 2f : stroke / 2 + (cap == CapStyle.round ? 0 : padding * 2));
        float angle = Mathf.atan2(x2 - x, y2 - y) * Mathf.radDeg;

        if(cap == CapStyle.square){
            Draw.rect(region, x - stroke / 2 - padding + length/2f, y, length, stroke, stroke / 2 + padding, stroke / 2, angle);
        }else if(cap == CapStyle.none){
            Draw.rect(region, x - padding + length/2f, y, length, stroke, padding, stroke / 2, angle);
        }else if(cap == CapStyle.round){ //TODO remove or fix
            TextureRegion cir = Core.atlas.has("hcircle") ? Core.atlas.find("hcircle") : Core.atlas.find("circle");
            Draw.rect(region, x - padding + length/2f, y, length, stroke, padding, stroke / 2, angle);
            Draw.rect(cir, x, y, stroke, stroke, angle + 180f);
            Draw.rect(cir, x2, y2, stroke, stroke, angle);
        }
    }

    public static void linePoint(float x, float y){
        if(!building) throw new IllegalStateException("Not building");
        floatBuilder.add(x, y);
    }

    public static void beginLine(){
        if(building) throw new IllegalStateException("Already building");
        floatBuilder.clear();
        building = true;
    }

    public static void endLine(){
        if(!building) throw new IllegalStateException("Not building");
        polyline(floatBuilder, false);
        building = false;
    }

    public static void polyline(FloatArray points, boolean wrap){
        polyline(points.items, points.size, wrap);
    }

    public static void polyline(float[] points, int length, boolean wrap){

        if(length < 4) return;

        float lasta;

        {
            float x1 = points[length - 2];
            float y1 = points[length - 1];
            float x2 = points[0];
            float y2 = points[1];
            float x3 = points[2];
            float y3 = points[3];

            if(wrap){
                lasta = Mathf.slerp(Angles.angle(x1, y1, x2, y2), Angles.angle(x2, y2, x3, y3), 0.5f);
            }else{
                lasta = Angles.angle(x1, y1, x2, y2) + 180f;
            }
        }

        for(int i = 0; i < (wrap ? length : length - 2); i += 2){
            float x1 = points[i];
            float y1 = points[i + 1];
            float x2 = points[(i + 2) % length];
            float y2 = points[(i + 3) % length];

            float avg;
            float ang1 = Angles.angle(x1, y1, x2, y2);

            if(wrap){
                float x3 = points[(i + 4) % length];
                float y3 = points[(i + 5) % length];
                float ang2 = Angles.angle(x2, y2, x3, y3);

                avg = Mathf.slerp(ang1, ang2, 0.5f);
            }else{
                avg = ang1;
            }

            float s = stroke / 2f;

            float cos1 = Mathf.cosDeg(lasta - 90) * s;
            float sin1 = Mathf.sinDeg(lasta - 90) * s;
            float cos2 = Mathf.cosDeg(avg - 90) * s;
            float sin2 = Mathf.sinDeg(avg - 90) * s;

            float qx1 = x1 + cos1;
            float qy1 = y1 + sin1;
            float qx4 = x1 - cos1;
            float qy4 = y1 - sin1;

            float qx2 = x2 + cos2;
            float qy2 = y2 + sin2;
            float qx3 = x2 - cos2;
            float qy3 = y2 - sin2;

            Fill.quad(qx1, qy1, qx2, qy2, qx3, qy3, qx4, qy4);

            lasta = avg;
        }

    }

    public static void dashLine(float x1, float y1, float x2, float y2, int divisions){
        float dx = x2 - x1, dy = y2 - y1;

        for(int i = 0; i < divisions; i++){
            if(i % 2 == 0){
                line(x1 + ((float)i / divisions) * dx, y1 + ((float)i / divisions) * dy,
                x1 + ((i + 1f) / divisions) * dx, y1 + ((i + 1f) / divisions) * dy);
            }
        }
    }

    public static void circle(float x, float y, float rad){
        poly(x, y, circleVertices, rad);
    }

    public static void dashCircle(float x, float y, float radius){
        float scaleFactor = 0.6f;
        int sides = 10 + (int)(radius * scaleFactor);
        if(sides % 2 == 1) sides++;

        vector.set(0, 0);

        for(int i = 0; i < sides; i++){
            if(i % 2 == 0) continue;
            vector.set(radius, 0).setAngle(360f / sides * i + 90);
            float x1 = vector.x;
            float y1 = vector.y;

            vector.set(radius, 0).setAngle(360f / sides * (i + 1) + 90);

            line(x1 + x, y1 + y, vector.x + x, vector.y + y);
        }
    }

    public static void spikes(float x, float y, float radius, float length, int spikes, float rot){
        vector.set(0, 1);
        float step = 360f / spikes;

        for(int i = 0; i < spikes; i++){
            vector.setAngle(i * step + rot);
            vector.setLength(radius);
            float x1 = vector.x, y1 = vector.y;
            vector.setLength(radius + length);

            line(x + x1, y + y1, x + vector.x, y + vector.y);
        }
    }

    public static void spikes(float x, float y, float rad, float length, int spikes){
        spikes(x, y, rad, length, spikes, 0);
    }

    public static void poly(float x, float y, int sides, float radius, float angle){
        vector.set(0, 0);

        for(int i = 0; i < sides; i++){
            vector.set(radius, 0).setAngle(360f / sides * i + angle + 90);
            float x1 = vector.x;
            float y1 = vector.y;

            vector.set(radius, 0).setAngle(360f / sides * (i + 1) + angle + 90);


            line(x1 + x, y1 + y, vector.x + x, vector.y + y);
        }
    }

    public static void polySeg(int sides, int from, int to, float x, float y, float radius, float angle){
        vector.set(0, 0);

        for(int i = from; i < to; i++){
            vector.set(radius, 0).setAngle(360f / sides * i + angle + 90);
            float x1 = vector.x;
            float y1 = vector.y;

            vector.set(radius, 0).setAngle(360f / sides * (i + 1) + angle + 90);

            line(x1 + x, y1 + y, vector.x + x, vector.y + y);
        }
    }

    public static void curve(float x1, float y1, float cx1, float cy1, float cx2, float cy2, float x2, float y2, int segments){

        // Algorithm shamelessly stolen from shaperenderer class
        float subdiv_step = 1f / segments;
        float subdiv_step2 = subdiv_step * subdiv_step;
        float subdiv_step3 = subdiv_step * subdiv_step * subdiv_step;

        float pre1 = 3 * subdiv_step;
        float pre2 = 3 * subdiv_step2;
        float pre4 = 6 * subdiv_step2;
        float pre5 = 6 * subdiv_step3;

        float tmp1x = x1 - cx1 * 2 + cx2;
        float tmp1y = y1 - cy1 * 2 + cy2;

        float tmp2x = (cx1 - cx2) * 3 - x1 + x2;
        float tmp2y = (cy1 - cy2) * 3 - y1 + y2;

        float fx = x1;
        float fy = y1;

        float dfx = (cx1 - x1) * pre1 + tmp1x * pre2 + tmp2x * subdiv_step3;
        float dfy = (cy1 - y1) * pre1 + tmp1y * pre2 + tmp2y * subdiv_step3;

        float ddfx = tmp1x * pre4 + tmp2x * pre5;
        float ddfy = tmp1y * pre4 + tmp2y * pre5;

        float dddfx = tmp2x * pre5;
        float dddfy = tmp2y * pre5;

        while(segments-- > 0){
            float fxold = fx, fyold = fy;
            fx += dfx;
            fy += dfy;
            dfx += ddfx;
            dfy += ddfy;
            ddfx += dddfx;
            ddfy += dddfy;
            line(fxold, fyold, fx, fy);
        }

        line(fx, fy, x2, y2);
    }

    public static void swirl(float x, float y, float radius, float finion){
        swirl(x, y, radius, finion, 0f);
    }

    public static void swirl(float x, float y, float radius, float finion, float angle){
        int sides = 50;
        int max = (int)(sides * (finion + 0.001f));
        vector.set(0, 0);

        for(int i = 0; i < max; i++){
            vector.set(radius, 0).setAngle(360f / sides * i + angle);
            float x1 = vector.x;
            float y1 = vector.y;

            vector.set(radius, 0).setAngle(360f / sides * (i + 1) + angle);

            line(x1 + x, y1 + y, vector.x + x, vector.y + y);
        }
    }

    public static void poly(float x, float y, int sides, float radius){
        floats.clear();
        for(int i = 0; i < sides; i++){
            float rad = (float)i / sides * Mathf.PI2;
            floats.add(Mathf.cos(rad) * radius + x, Mathf.sin(rad) * radius + y);
        }

        polyline(floats, true);
    }

    public static void poly(Vector2[] vertices, float offsetx, float offsety, float scl){
        for(int i = 0; i < vertices.length; i++){
            Vector2 current = vertices[i];
            Vector2 next = i == vertices.length - 1 ? vertices[0] : vertices[i + 1];
            line(current.x * scl + offsetx, current.y * scl + offsety, next.x * scl + offsetx, next.y * scl + offsety);
        }
    }

    public static void square(float x, float y, float rad){
        rect(x - rad, y - rad, rad * 2, rad * 2);
    }

    public static void rect(float x, float y, float width, float height, float xspace, float yspace){
        x -= xspace;
        y -= yspace;
        width += xspace * 2;
        height += yspace * 2;

        Fill.crect(x, y, width, stroke);
        Fill.crect(x, y + height, width, -stroke);

        Fill.crect(x + width, y, -stroke, height);
        Fill.crect(x, y, stroke, height);
    }

    public static void rect(float x, float y, float width, float height){
        rect(x, y, width, height, 0);
    }

    public static void rect(Rectangle rect){
        rect(rect.x, rect.y, rect.width, rect.height, 0);
    }

    public static void rect(float x, float y, float width, float height, int space){
        rect(x, y, width, height, space, space);
    }

    public static void stroke(float thick){
        stroke = thick;
    }

    public static void stroke(float thick, Color color){
        stroke = thick;
        Draw.color(color);
    }

    public static float getStroke(){
        return stroke;
    }
}
