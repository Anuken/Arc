package arc.graphics.g2d;

import arc.*;
import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;

public class Lines{
    public static boolean useLegacyLine = false;

    private static float stroke = 1f;
    private static Vec2 vector = new Vec2(), u = new Vec2(), v = new Vec2(), inner = new Vec2(), outer = new Vec2();
    private static FloatSeq floats = new FloatSeq(20);
    private static FloatSeq floatBuilder = new FloatSeq(20);
    private static boolean building;
    private static float circlePrecision = 0.4f;

    /** Set the vertices used for drawing a line circle. */
    public static void setCirclePrecision(float amount){
        circlePrecision = amount;
    }

    public static int circleVertices(float rad){
        return 11 + (int)(rad * circlePrecision);
    }

    public static void lineAngle(float x, float y, float angle, float length, boolean cap){
        vector.set(1, 1).setLength(length).setAngle(angle);

        line(x, y, x + vector.x, y + vector.y, cap);
    }

    public static void lineAngle(float x, float y, float angle, float length){
        vector.set(1, 1).setLength(length).setAngle(angle);

        line(x, y, x + vector.x, y + vector.y);
    }

    public static void lineAngle(float x, float y, float offset, float angle, float length){
        vector.set(1, 1).setLength(length + offset).setAngle(angle);

        line(x, y, x + vector.x, y + vector.y);
    }

    public static void lineAngleCenter(float x, float y, float angle, float length, boolean cap){
        vector.trns(angle, length);

        line(x - vector.x / 2, y - vector.y / 2, x + vector.x / 2, y + vector.y / 2, cap);
    }

    public static void lineAngleCenter(float x, float y, float angle, float length){
        vector.trns(angle, length);

        line(x - vector.x / 2, y - vector.y / 2, x + vector.x / 2, y + vector.y / 2);
    }

    public static void line(float x, float y, float x2, float y2){
        line(x, y, x2, y2, true);
    }

    public static void line(float x, float y, float x2, float y2, boolean cap){
        line(Core.atlas.white(), x, y, x2, y2, cap);
    }

    public static void line(TextureRegion region, float x, float y, float x2, float y2, boolean cap){
        if(useLegacyLine){
            float length = Mathf.dst(x, y, x2, y2) + (!cap ? 0 : stroke);
            float angle = (Mathf.atan2(x2 - x, y2 - y)) * Mathf.radDeg;

            if(cap){
                Draw.rect(region, x - stroke / 2 + length/2f, y, length, stroke, stroke / 2, stroke / 2, angle);
            }else{
                Draw.rect(region, x + length/2f, y, length, stroke, 0, stroke / 2, angle);
            }
        }else{
            float hstroke = stroke/2f;
            float len = Mathf.len(x2 - x, y2 - y);
            float diffx = (x2 - x) / len * hstroke, diffy = (y2 - y) / len * hstroke;

            if(cap){
                Fill.quad(
                region,

                x - diffx - diffy,
                y - diffy + diffx,

                x - diffx + diffy,
                y - diffy - diffx,

                x2 + diffx + diffy,
                y2 + diffy - diffx,

                x2 + diffx - diffy,
                y2 + diffy + diffx

                );
            }else{
                Fill.quad(
                region,

                x - diffy,
                y + diffx,

                x + diffy,
                y - diffx,

                x2 + diffy,
                y2 - diffx,

                x2 - diffy,
                y2 + diffx

                );
            }
        }
    }

    public static void linePoint(Position p){
        linePoint(p.getX(), p.getY());
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
        endLine(false);
    }

    public static void endLine(boolean wrap){
        if(!building) throw new IllegalStateException("Not building");
        polyline(floatBuilder, wrap);
        building = false;
    }

    public static void polyline(FloatSeq points, boolean wrap){
        polyline(points.items, points.size, wrap);
    }

    public static void polyline(float[] points, int length, boolean wrap){
        if(length < 4) return;

        if(!wrap){
            for(int i = 0; i < length-2; i+= 2){
                float cx = points[i];
                float cy = points[i + 1];
                float cx2 = points[i + 2];
                float cy2 = points[i + 3];
                line(cx, cy, cx2, cy2);
            }
        }else{
            floats.clear();

            for(int i = 0; i < length; i += 2){
                float x0 = points[Mathf.mod(i - 2, length)];
                float y0 = points[Mathf.mod(i - 1, length)];
                float x1 = points[i];
                float y1 = points[i + 1];
                float x2 = points[(i + 2) % length];
                float y2 = points[(i + 3) % length];

                float ang0 = Angles.angle(x0, y0, x1, y1), ang1 = Angles.angle(x1, y1, x2, y2);
                float beta = Mathf.sinDeg(ang1 - ang0);

                u.set(x0, y0).sub(x1, y1).scl(1f / Mathf.dst(x0, y0, x1, y1)).scl(stroke / (2f*beta));
                v.set(x2, y2).sub(x1, y1).scl(1f / Mathf.dst(x2, y2, x1, y1)).scl(stroke / (2f*beta));

                inner.set(x1, y1).add(u).add(v);
                outer.set(x1, y1).sub(u).sub(v);

                floats.add(inner.x, inner.y, outer.x, outer.y);
            }

            for(int i = 0; i < floats.size; i += 4){
                float x1 = floats.items[i];
                float y1 = floats.items[i + 1];
                float x2 = floats.items[(i + 2) % floats.size];
                float y2 = floats.items[(i + 3) % floats.size];

                float x3 = floats.items[(i + 4) % floats.size];
                float y3 = floats.items[(i + 5) % floats.size];
                float x4 = floats.items[(i + 6) % floats.size];
                float y4 = floats.items[(i + 7) % floats.size];

                Fill.quad(x1, y1, x3, y3, x4, y4, x2, y2);
            }
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
        poly(x, y, circleVertices(rad), rad);
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

    public static void quad(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4){
        floatBuilder.clear();

        floatBuilder.add(x1, y1, x2, y2);
        floatBuilder.add(x3, y3, x4, y4);

        polyline(floatBuilder, true);
    }

    public static void poly(float x, float y, int sides, float radius, float angle){
        float space = 360f / sides;
        float hstep = stroke / 2f / Mathf.cosDeg(space/2f);
        float r1 = radius - hstep, r2 = radius + hstep;

        for(int i = 0; i < sides; i++){
            float a = space * i + angle, cos = Mathf.cosDeg(a), sin = Mathf.sinDeg(a), cos2 = Mathf.cosDeg(a + space), sin2 = Mathf.sinDeg(a + space);
            Fill.quad(
            x + r1*cos, y + r1*sin,
            x + r1*cos2, y + r1*sin2,
            x + r2*cos2, y + r2*sin2,
            x + r2*cos, y + r2*sin
            );
        }
    }

    public static void poly(float x, float y, int sides, float radius){
        poly(x, y, sides, radius, 0);
    }

    public static void poly(Vec2[] vertices, float offsetx, float offsety, float scl){
        for(int i = 0; i < vertices.length; i++){
            Vec2 current = vertices[i];
            Vec2 next = i == vertices.length - 1 ? vertices[0] : vertices[i + 1];
            line(current.x * scl + offsetx, current.y * scl + offsety, next.x * scl + offsetx, next.y * scl + offsety);
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
        floats.clear();

        for(int i = 0; i < max; i++){
            vector.set(radius, 0).setAngle(360f / sides * i + angle);
            float x1 = vector.x;
            float y1 = vector.y;

            vector.set(radius, 0).setAngle(360f / sides * (i + 1) + angle);

            floats.add(x1 + x, y1 + y);
        }

        polyline(floats, false);
    }

    public static void square(float x, float y, float rad){
        rect(x - rad, y - rad, rad * 2, rad * 2);
    }

    public static void square(float x, float y, float rad, float rot){
        poly(x, y, 4, rad, rot - 45);
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

    public static void rect(Rect rect){
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
