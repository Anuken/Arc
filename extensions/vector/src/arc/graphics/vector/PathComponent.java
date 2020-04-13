package arc.graphics.vector;

import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.pooling.Pool.*;
import arc.util.pooling.*;

public class PathComponent implements PathConstants, Poolable{
    private static final float fillMiterLimit = 2.4f;
    final Array<Point> points = new Array<>();
    final FillPathTriangulator fillTriangulator = new FillPathTriangulator(this);
    final StrokePathTriangulator strokeTriangulator = new StrokePathTriangulator(this);
    private final Vec2 tempPoint = new Vec2();
    Canvas canvas;
    PathMesh pathMesh;
    boolean closed;
    boolean convex;
    Winding winding = Winding.none;
    float[] bounds = new float[4];

    public PathComponent(){
    }

    static PathComponent obtain(PathMesh pathMesh){
        PathComponent pathComponent = Pools.obtain(PathComponent.class, PathComponent::new);
        pathComponent.canvas = pathMesh.canvas;
        pathComponent.pathMesh = pathMesh;
        pathComponent.winding = pathMesh.canvas.getWinding();
        pathComponent.fillTriangulator.canvas = pathMesh.canvas;//TODO ugly
        pathComponent.fillTriangulator.pathMesh = pathMesh;
        pathComponent.strokeTriangulator.canvas = pathMesh.canvas;
        pathComponent.strokeTriangulator.pathMesh = pathMesh;
        return pathComponent;
    }

    private static float getTriangleArea2(float ax, float ay, float bx, float by, float cx, float cy){
        float abx = bx - ax;
        float aby = by - ay;
        float acx = cx - ax;
        float acy = cy - ay;
        return acx * aby - abx * acy;
    }

    private static float getInverseWidth(float width){
        return width > 0.0f ? 1.0f / width : 0.0f;
    }

    private static float calculateExtrusions(Point p0, Point p1){
        float dlx0 = p0.dy;
        float dly0 = -p0.dx;
        float dlx1 = p1.dy;
        float dly1 = -p1.dx;

        p1.dmx = (dlx0 + dlx1) * 0.5f;
        p1.dmy = (dly0 + dly1) * 0.5f;
        float dmr2 = p1.dmx * p1.dmx + p1.dmy * p1.dmy;

        if(dmr2 > 0.000001f){
            float scale = 1.0f / dmr2;
            scale = scale > 600.0f ? 600.0f : scale;
            p1.dmx *= scale;
            p1.dmy *= scale;
        }

        return dmr2;
    }

    private static void calculateInnerJoinProperties(float inverseFillWidth, float inverseStrokeWidth, Point p0, Point p1, float dmr2){
        float minPointsLength = Math.min(p0.length, p1.length);

        float limit = Math.max(1.01f, minPointsLength * inverseStrokeWidth);
        if((dmr2 * limit * limit) < 1.0f){
            p1.flags |= PT_INNERBEVEL;
        }

        limit = Math.max(1.01f, minPointsLength * inverseFillWidth);
        if((dmr2 * limit * limit) < 1.0f){
            p1.flags |= PT_FILL_INNERBEVEL;
        }
    }

    GlPathComponent createFillComponent(){
        return fillTriangulator.createGlPathComponent();
    }

    GlPathComponent createStrokeComponent(){
        return strokeTriangulator.createGlPathComponent();
    }

    void refine(){
        enforceWinding();
        tryClosingPath();
        calculatePointProperties();
    }

    private void enforceWinding(){
        if(points.size > 2 && mustReversePolygon()){
            points.reverse();
        }
    }

    private boolean mustReversePolygon(){
        if(winding == Winding.none){
            return false;
        }else{
            float area = getPolygonArea();
            return winding == Winding.counterClockwise && area < 0.0f || winding == Winding.clockwise && area > 0.0f;
        }
    }

    private float getPolygonArea(){
        float area = 0;

        for(int i = 2; i < points.size; i++){
            Point a = points.get(0);
            Point b = points.get(i - 1);
            Point c = points.get(i);
            area += getTriangleArea2(a.x, a.y, b.x, b.y, c.x, c.y);
        }

        return area * 0.5f;
    }

    private void tryClosingPath(){
        // If the first and last points are the same, remove the last, mark as closed path.
        Point p0 = points.get(points.size - 1);
        Point p1 = points.get(0);

        if(points.size > 1 && p0.pointEquals(p1.x, p1.y, Canvas.distanceTolerance)){
            points.remove(points.size - 1);
            p0 = points.get(points.size - 1);
            closed = true;
        }
    }

    float getFillWidth(){
        return canvas.isAntiAlias() ? canvas.fringeWidth : 0;
    }

    float getStrokeWidth(){
        CanvasState state = canvas.currentState;
        float scale = state.xform.getAverageScale();
        float strokeWidth = Mathf.clamp(state.strokeWidth * scale, 0.0f, 200.0f) * 0.5f;
        return canvas.isAntiAlias() ? strokeWidth + canvas.fringeWidth * 0.5f : strokeWidth;
    }

    private void calculatePointProperties(){
        bounds[0] = bounds[1] = 1e6f;
        bounds[2] = bounds[3] = -1e6f;
        float inverseFillWidth = getInverseWidth(getFillWidth());
        float inverseStrokeWidth = getInverseWidth(getStrokeWidth());

        int ptsSize = points.size;
        Point p = null;
        Point p0 = points.get(ptsSize - 1);
        Point p1 = points.get(0);
        int numberOfLeftTurns = 0;

        for(int i = 0; i < ptsSize; i++){
            calculateDirectionAndLength(p0, p1);
            updateBounds(p0);
            numberOfLeftTurns += calculatePointProperties(inverseFillWidth, inverseStrokeWidth, p, p0);
            p = p0;
            p0 = p1;
            p1 = i < ptsSize - 1 ? points.get(i + 1) : points.get(0);
        }

        numberOfLeftTurns += calculatePointProperties(inverseFillWidth, inverseStrokeWidth, p, p0);

        convex = numberOfLeftTurns == points.size;
    }

    private void updateBounds(Point p0){
        bounds[0] = Math.min(bounds[0], p0.x);
        bounds[1] = Math.min(bounds[1], p0.y);
        bounds[2] = Math.max(bounds[2], p0.x);
        bounds[3] = Math.max(bounds[3], p0.y);
    }

    private void calculateDirectionAndLength(Point p0, Point p1){
        p0.length = CanvasUtils.normalize(tempPoint.set(p1.x - p0.x, p1.y - p0.y));
        p0.dx = tempPoint.x;
        p0.dy = tempPoint.y;
    }

    private int calculatePointProperties(float inverseFillWidth, float inverseStrokeWidth, Point p, Point p0){
        if(p == null){
            return 0;
        }

        float dmr2 = calculateExtrusions(p, p0);
        calculateInnerJoinProperties(inverseFillWidth, inverseStrokeWidth, p, p0, dmr2);
        calculateCornerBevelProperties(p0, dmr2);

        float cross = p0.dx * p.dy - p.dx * p0.dy;
        if(cross > 0.0f){
            p0.flags |= PT_LEFT;
            return 1;
        }else{
            return 0;
        }
    }

    private void calculateCornerBevelProperties(Point p1, float dmr2){
        if((p1.flags & PT_CORNER) != 0){
            LineJoin lineJoin = canvas.getStrokeLineJoin();
            float strokeMiterLimit = canvas.getStrokeMiterLimit();

            if(lineJoin == LineJoin.bevel || lineJoin == LineJoin.round){
                p1.flags |= PT_BEVEL | PT_FILL_BEVEL;
            }else if((dmr2 * strokeMiterLimit * strokeMiterLimit) < 1.0f){
                p1.flags |= PT_BEVEL;
            }else if((dmr2 * fillMiterLimit * fillMiterLimit) < 1.0f){
                p1.flags |= PT_FILL_BEVEL;
            }
        }
    }

    @Override
    public void reset(){
        canvas = null;

        closed = false;
        convex = false;
        winding = Winding.none;

        bounds[0] = bounds[1] = 1e6f;
        bounds[2] = bounds[3] = -1e6f;

        CanvasUtils.resetArray(points);
    }
}
