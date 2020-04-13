package arc.graphics.vector;

import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;

class StrokePathTriangulator extends PathTriangulator{
    private final Vec2 tempPoint = new Vec2();

    StrokePathTriangulator(PathComponent component){
        super(component, PT_BEVEL, PT_INNERBEVEL);
    }

    @Override
    void triangulate(){
        float width = component.getStrokeWidth();
        int ncap = calculateNumberOfCapFragments(width);
        Array<Point> points = component.points;
        float aa = canvas.fringeWidth;
        Point p0;
        Point p1;
        int start, end;

        int ptsSize = points.size;
        int ptsSizeMinusOne = ptsSize - 1;

        if(component.closed){
            // Looping
            p0 = points.get(ptsSizeMinusOne);
            p1 = points.get(0);
            start = 0;
            end = ptsSize;
        }else{
            // Add cap
            p0 = points.get(0);
            p1 = points.get(1);
            start = 1;
            end = ptsSizeMinusOne;
            addStartCap(width, ncap, aa, p0, p1);
        }

        LineJoin lineJoin = canvas.getStrokeLineJoin();
        for(int j = start; j < end && j < ptsSize; ++j){
            addStrokeVertices(width, ncap, p0, p1, lineJoin);
            p0 = p1;
            p1 = j < ptsSizeMinusOne ? points.get(j + 1) : points.get(0);
        }

        endStroke(width, ncap, aa, p0, p1);
    }

    private int calculateNumberOfCapFragments(float strokeWidth){
        LineCap lineCap = canvas.getStrokeLineCap();
        LineJoin lineJoin = canvas.getStrokeLineJoin();
        if(lineCap != LineCap.round && lineJoin != LineJoin.round){
            return 0;
        }
        float da = (float)(Math.acos(strokeWidth / (strokeWidth + canvas.tesselationTolerance)) * 2.0f);
        return Math.max(2, (int)Math.ceil(Mathf.PI / da));
    }

    private void addStartCap(float width, int ncap, float aa, Point p0, Point p1){
        CanvasUtils.normalize(tempPoint.set(p1.x - p0.x, p1.y - p0.y));
        float dx = tempPoint.x;
        float dy = tempPoint.y;

        LineCap lineCap = canvas.getStrokeLineCap();
        switch(lineCap){
            case butt:
                buttCapStart(p0, dx, dy, width, -aa * 0.5f, aa);
                break;
            case square:
                buttCapStart(p0, dx, dy, width, width - aa, aa);
                break;
            case round:
                roundCapStart(p0, dx, dy, width, ncap);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void addStrokeVertices(float width, int ncap, Point p0, Point p1, LineJoin lineJoin){
        if((p1.flags & (ptBevelFlag | prInnerBevelFlag)) != 0){
            if(lineJoin == LineJoin.round){
                roundJoin(triangleStripVertices, p0, p1, width, width, 0, 1, ncap);
            }else{
                bevelJoin(triangleStripVertices, p0, p1, width, width, 0, 1);
            }
        }else{
            newTriangleStripVertex(p1.x + (p1.dmx * width), p1.y + (p1.dmy * width), 0, 1);
            newTriangleStripVertex(p1.x - (p1.dmx * width), p1.y - (p1.dmy * width), 1, 1);
        }
    }

    private void endStroke(float w, int ncap, float aa, Point p0, Point p1){
        if(component.closed){
            loopStrokeEnd();
        }else{
            addEndCap(w, ncap, aa, p0, p1);
        }
    }

    private void loopStrokeEnd(){
        Vertex dst0 = triangleStripVertices.get(0);
        Vertex dst1 = triangleStripVertices.get(1);

        newTriangleStripVertex(dst0.x, dst0.y, 0, 1);
        newTriangleStripVertex(dst1.x, dst1.y, 1, 1);
    }

    private void addEndCap(float w, int ncap, float aa, Point p0, Point p1){
        CanvasUtils.normalize(tempPoint.set(p1.x - p0.x, p1.y - p0.y));
        float dx = tempPoint.x;
        float dy = tempPoint.y;

        LineCap lineCap = canvas.getStrokeLineCap();
        switch(lineCap){
            case butt:
                buttCapEnd(p1, dx, dy, w, -aa * 0.5f, aa);
                break;
            case square:
                buttCapEnd(p1, dx, dy, w, w - aa, aa);
                break;
            case round:
                roundCapEnd(p1, dx, dy, w, ncap);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void buttCapStart(Point p, float dx, float dy, float w, float d, float aa){
        float px = p.x - dx * d;
        float py = p.y - dy * d;
        float dly = -dx;

        newTriangleStripVertex(px + dy * w - dx * aa, py + dly * w - dy * aa, 0, 0);
        newTriangleStripVertex(px - dy * w - dx * aa, py - dly * w - dy * aa, 1, 0);
        newTriangleStripVertex(px + dy * w, py + dly * w, 0, 1);
        newTriangleStripVertex(px - dy * w, py - dly * w, 1, 1);
    }

    private void buttCapEnd(Point p, float dx, float dy, float w, float d, float aa){
        float px = p.x + dx * d;
        float py = p.y + dy * d;
        float dly = -dx;

        newTriangleStripVertex(px + dy * w, py + dly * w, 0, 1);
        newTriangleStripVertex(px - dy * w, py - dly * w, 1, 1);
        newTriangleStripVertex(px + dy * w + dx * aa, py + dly * w + dy * aa, 0, 0);
        newTriangleStripVertex(px - dy * w + dx * aa, py - dly * w + dy * aa, 1, 0);
    }

    private void roundCapStart(Point p, float dx, float dy, float w, int ncap){
        float px = p.x;
        float py = p.y;
        float dly = -dx;

        for(int i = 0; i < ncap; i++){
            float a = i / (float)(ncap - 1) * Mathf.PI;
            float ax = Mathf.cos(a) * w, ay = Mathf.sin(a) * w;

            newTriangleStripVertex(px - dy * ax - dx * ay, py - dly * ax - dy * ay, 0, 1);
            newTriangleStripVertex(px, py, 0.5f, 1);
        }

        newTriangleStripVertex(px + dy * w, py + dly * w, 0, 1);
        newTriangleStripVertex(px - dy * w, py - dly * w, 1, 1);
    }

    private void roundCapEnd(Point p, float dx, float dy, float w, int ncap){
        float px = p.x;
        float py = p.y;
        float dly = -dx;

        newTriangleStripVertex(px + dy * w, py + dly * w, 0, 1);
        newTriangleStripVertex(px - dy * w, py - dly * w, 1, 1);

        for(int i = 0; i < ncap; i++){
            float a = i / (float)(ncap - 1) * Mathf.PI;
            float ax = Mathf.cos(a) * w, ay = Mathf.sin(a) * w;

            newTriangleStripVertex(px, py, 0.5f, 1);
            newTriangleStripVertex(px - dy * ax + dx * ay, py - dly * ax + dy * ay, 0, 1);
        }
    }
}
