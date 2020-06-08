package arc.graphics.vector;

import arc.graphics.vector.GlCall.*;
import arc.math.*;
import arc.struct.*;
import arc.util.pooling.Pool.*;
import arc.util.pooling.*;

public class PathMesh implements PathConstants, Poolable{
    private final Seq<PathComponent> components = new Seq<>();
    private final Seq<PathComponent> dashedStrokeComponents = new Seq<>();
    private final float[] bounds = new float[]{1e6f, 1e6f, -1e6f, -1e6f};
    Canvas canvas;
    private Seq<PathComponent> strokeComponents;
    private PathComponent lastComponent;
    private Point lastPoint;

    public static PathMesh obtain(Canvas canvas, Path path){
        PathMesh pathMesh = Pools.obtain(PathMesh.class, PathMesh::new);
        pathMesh.canvas = canvas;
        pathMesh.tesselatePath(path.getTansformedCommands(canvas.currentState.xform));
        return pathMesh;
    }

    public static boolean pointEquals(float x1, float y1, float x2, float y2, float tol){
        float dx = x2 - x1;
        float dy = y2 - y1;
        return dx * dx + dy * dy < tol * tol;
    }

    private void tesselatePath(FloatSeq commands){
        boolean firstComponent = true;
        addComponent();

        int i = 0;
        while(i < commands.size){
            int cmd = (int)commands.get(i);
            switch(cmd){
                case moveTo:
                    if(!firstComponent){
                        addComponent();
                    }else{
                        firstComponent = false;
                    }
                    addPoint(commands.get(i + 1), commands.get(i + 2), PT_CORNER);
                    i += 3;
                    break;
                case lineTo:
                    if(firstComponent){
                        addPoint(0, 0, PT_CORNER);
                        firstComponent = false;
                    }
                    addPoint(commands.get(i + 1), commands.get(i + 2), PT_CORNER);
                    i += 3;
                    break;
                case cubicTo:
                    if(firstComponent){
                        addPoint(0, 0, PT_CORNER);
                        firstComponent = false;
                    }
                    tesselateBezier(lastPoint.x, lastPoint.y, commands.get(i + 1), commands.get(i + 2), commands.get(i + 3),
                    commands.get(i + 4), commands.get(i + 5), commands.get(i + 6), 0, PT_CORNER);
                    i += 7;
                    break;
                case close:
                    closePath();
                    i++;
                    break;
                case winding:
                    pathWinding(Winding.values()[(int)commands.get(i + 1)]);
                    i += 2;
                    break;
                default:
                    i++;
            }
        }

        refineLastComponent();
        lastComponent = null;
        lastPoint = null;
    }

    private void addComponent(){
        refineLastComponent();
        lastComponent = PathComponent.obtain(this);
        components.add(lastComponent);
        lastPoint = null;
    }

    private void refineLastComponent(){
        if(lastComponent != null){
            lastComponent.refine();
            float[] componentBounds = lastComponent.bounds;
            bounds[0] = Math.min(bounds[0], componentBounds[0]);
            bounds[1] = Math.min(bounds[1], componentBounds[1]);
            bounds[2] = Math.max(bounds[2], componentBounds[2]);
            bounds[3] = Math.max(bounds[3], componentBounds[3]);
        }
    }

    private void addPoint(float x, float y, int flags){
        if(lastComponent == null){
            return;
        }

        Seq<Point> points = lastComponent.points;
        if(points.size > 0 && lastPoint != null && lastPoint.pointEquals(x, y, Canvas.distanceTolerance)){
            lastPoint.flags |= flags;
        }else{
            lastPoint = Point.obtain();
            points.add(lastPoint);
            lastPoint.x = x;
            lastPoint.y = y;
            lastPoint.flags = flags;
        }
    }

    private void closePath(){
        if(lastComponent != null){
            lastComponent.closed = true;
        }
    }

    private void pathWinding(Winding winding){
        if(lastComponent != null){
            lastComponent.winding = winding;
        }
    }

    private void tesselateBezier(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4,
                                 int level, int type){
        if(level > 10){
            return;
        }else if(level == 10 || checkTesselationTolerance(x1, y1, x2, y2, x3, y3, x4, y4)){
            addPoint(x4, y4, type);
            return;
        }

        float x12 = (x1 + x2) * 0.5f;
        float y12 = (y1 + y2) * 0.5f;
        float x23 = (x2 + x3) * 0.5f;
        float y23 = (y2 + y3) * 0.5f;
        float x34 = (x3 + x4) * 0.5f;
        float y34 = (y3 + y4) * 0.5f;
        float x123 = (x12 + x23) * 0.5f;
        float y123 = (y12 + y23) * 0.5f;
        float x234 = (x23 + x34) * 0.5f;
        float y234 = (y23 + y34) * 0.5f;
        float x1234 = (x123 + x234) * 0.5f;
        float y1234 = (y123 + y234) * 0.5f;

        tesselateBezier(x1, y1, x12, y12, x123, y123, x1234, y1234, level + 1, 0);
        tesselateBezier(x1234, y1234, x234, y234, x34, y34, x4, y4, level + 1, type);
    }

    private boolean checkTesselationTolerance(float x1, float y1, float x2, float y2, float x3, float y3, float x4,
                                              float y4){
        float dx = x4 - x1;
        float dy = y4 - y1;
        float d2 = Math.abs(((x2 - x4) * dy - (y2 - y4) * dx));
        float d3 = Math.abs(((x3 - x4) * dy - (y3 - y4) * dx));

        return ((d2 + d3) * (d2 + d3) < canvas.tesselationTolerance * (dx * dx + dy * dy));
    }

    GlCall createFillCall(){
        GlCall fillCall = GlCall.obtain();
        fillCall.callType = isConvex() ? CallType.convexFill : CallType.fill;
        fillCall.blendMode = canvas.getBlendMode();

        for(int i = 0; i < components.size; i++){
            PathComponent pathComponent = components.get(i);
            fillCall.components.add(pathComponent.createFillComponent());
        }

        //TODO this breaks things completely, drawing a filled quad over everything-- remove?
        addFillQuad(fillCall);
        setupFillUniforms(fillCall);

        return fillCall;
    }

    boolean isConvex(){
        return components.size == 1 && components.get(0).convex;
    }

    private void addFillQuad(GlCall fillCall){
        fillCall.newTriangleVertex(bounds[0], bounds[3], 0.5f, 1.0f);
        fillCall.newTriangleVertex(bounds[2], bounds[3], 0.5f, 1.0f);
        fillCall.newTriangleVertex(bounds[2], bounds[1], 0.5f, 1.0f);

        fillCall.newTriangleVertex(bounds[0], bounds[3], 0.5f, 1.0f);
        fillCall.newTriangleVertex(bounds[2], bounds[1], 0.5f, 1.0f);
        fillCall.newTriangleVertex(bounds[0], bounds[1], 0.5f, 1.0f);
    }

    private void setupFillUniforms(GlCall fillCall){
        CanvasState state = canvas.currentState;
        fillCall.initUniform(state.xform, state.globalAlpha, state.scissor, state.fillPaint, canvas.fringeWidth,
        canvas.fringeWidth, -1.0f);
    }

    GlCall createStrokeCall(){
        prepareStrokeComponents();

        GlCall strokeCall = GlCall.obtain();
        strokeCall.callType = CallType.stroke;
        strokeCall.blendMode = canvas.getBlendMode();

        for(int i = 0; i < strokeComponents.size; i++){
            PathComponent pathComponent = strokeComponents.get(i);
            strokeCall.components.add(pathComponent.createStrokeComponent());
        }

        setupStrokeUniforms(strokeCall);
        return strokeCall;
    }

    private void prepareStrokeComponents(){
        if(canvas.currentState.isDashedStroke()){
            createDashedStrokeComponents();
            strokeComponents = dashedStrokeComponents;
        }else{
            strokeComponents = components;
        }
    }

    private void createDashedStrokeComponents(){
        Dasher dasher = canvas.dasher;
        // TODO check if needs component reset
        CanvasUtils.resetArray(dashedStrokeComponents);
        CanvasState currentState = canvas.currentState;
        dasher.init(currentState.dashArray, currentState.dashOffset);

        for(int i = 0; i < components.size; i++){
            PathComponent pathComponent = components.get(i);
            dasher.appendDashedStrokeComponents(dashedStrokeComponents, pathComponent);
        }

        dasher.reset();
    }

    private void setupStrokeUniforms(GlCall strokeCall){
        CanvasState state = canvas.currentState;
        float scale = state.xform.getAverageScale();
        float strokeWidth = Mathf.clamp(state.strokeWidth * scale, 0.0f, 200.0f) * 0.5f;
        strokeWidth = canvas.isAntiAlias() ? strokeWidth + canvas.fringeWidth * 0.5f : strokeWidth;
        float globalAlpha = state.globalAlpha;

        float fringeWidth = canvas.fringeWidth;
        if(strokeWidth < fringeWidth){
            // If the stroke width is less than pixel size, use alpha to emulate coverage.
            // Since coverage is area, scale by alpha*alpha.
            float alpha = Mathf.clamp(strokeWidth / fringeWidth, 0.0f, 1.0f);
            globalAlpha *= alpha * alpha;
            strokeWidth = fringeWidth;
        }

        strokeCall.initUniform(state.xform, globalAlpha, state.scissor, state.strokePaint, strokeWidth, fringeWidth,
        -1.0f);
        if(canvas.isStencilStrokes()){
            strokeCall.initStrokesUniform(state.xform, globalAlpha, state.scissor, state.strokePaint, strokeWidth,
            fringeWidth, 1.0f - 0.5f / 255.0f);
        }
    }

    @Override
    public void reset(){
        canvas = null;
        bounds[0] = bounds[1] = 1e6f;
        bounds[2] = bounds[3] = -1e6f;
        CanvasUtils.resetArray(components);
        CanvasUtils.resetArray(dashedStrokeComponents);
        strokeComponents = null;
    }

    public void free(){
        Pools.free(this);
    }
}
