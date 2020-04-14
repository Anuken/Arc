package arc.graphics.vector;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.vector.CanvasFlags.*;
import arc.graphics.vector.Font.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.struct.IntMap.*;
import arc.util.*;
import arc.util.pooling.Pool.*;
import arc.util.pooling.*;

public class Canvas implements Disposable, Poolable{
    public static final float distanceTolerance = 0.1f;
    final Dasher dasher = new Dasher();
    private final Array<CanvasState> states = new Array<>(32);
    private final Array<GlCall> calls = new Array<>(128);
    private final Array<Clip> clips = new Array<>();
    private final IntMap<Image> images = new IntMap<>();
    private final IntMap<Gradient> gradients = new IntMap<>();
    private final GradientBuilder gradientBuilder = new GradientBuilder();
    private final Path path = Path.obtain();
    private final PathBuilder pathBuilder = new PathBuilder();
    int width;
    int height;
    // For example, GLFW returns two dimension for an opened window: window size and
    // frame buffer size. In that case you would set windowWidth/Height to the window size
    // devicePixelRatio to: frameBufferWidth / windowWidth.
    float devicePixelRatio;
    float tesselationTolerance;
    float fringeWidth;
    CanvasFlags flags = new CanvasFlags();
    GlContext glContext = new GlContext();
    CanvasState currentState;
    private RenderGraph renderGraph = new RenderGraph();
    private int imageIndex;
    private int gradientIndex;

    public static Canvas obtain(CanvasFlag... flags){
        return obtain(Core.graphics.getWidth(), Core.graphics.getHeight(), flags);
    }

    public static Canvas obtain(int width, int height, CanvasFlag... flags){
        Canvas canvas = Pools.obtain(Canvas.class, Canvas::new);
        canvas.init(width, height, flags);
        return canvas;
    }

    public static Canvas obtain(Texture texture, CanvasFlag... flags){
        Canvas canvas = Pools.obtain(Canvas.class, Canvas::new);
        canvas.init(texture, flags);
        return canvas;
    }

    private void init(int width, int height, CanvasFlag... flags){
        this.width = width;
        this.height = height;
        this.flags.and(flags);

        devicePixelRatio = Core.graphics.getBackBufferWidth() / width;
        tesselationTolerance = 0.25f / devicePixelRatio;
        fringeWidth = 1.0f / devicePixelRatio;

        glContext.init(width, height, isAntiAlias(), isStencilStrokes(), isDebug());
        renderGraph.init(glContext, 0);

        saveState();
    }

    private void init(Texture texture, CanvasFlag... flags){
        width = texture.getWidth();
        height = texture.getHeight();
        this.flags.and(flags);

        devicePixelRatio = 1;
        tesselationTolerance = 0.25f;
        fringeWidth = 1.0f;

        glContext.init(width, height, isAntiAlias(), isStencilStrokes(), isDebug());
        renderGraph.init(glContext, texture);

        saveState();
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

    public int createImage(Fi textureFile, boolean flipX, boolean flipY, boolean premultiplied){
        Texture texture = new Texture(textureFile);
        int imageId = ++imageIndex;
        images.put(imageId, Image.obtain(imageId, texture, flipX, flipY, premultiplied, true));
        return imageId;
    }

    public int createImage(Texture texture, boolean flipX, boolean flipY, boolean premultiplied){
        int imageId = ++imageIndex;
        images.put(imageId, Image.obtain(imageId, texture, flipX, flipY, premultiplied, false));
        return imageId;
    }

    public void deleteImages(){
        Values<Image> values = images.values();
        while(values.hasNext){
            Image image = values.next();
            image.free();
        }
        images.clear();
    }

    public void deleteImage(int imageId){
        Image image = images.remove(imageId);
        if(image != null){
            image.free();
        }
    }

    public int createGradient(FloatArray stops){
        int gradientId = ++gradientIndex;
        gradients.put(gradientId, Gradient.obtain(gradientId, stops));
        return gradientId;
    }

    public GradientBuilder newGradient(){
        return gradientBuilder.start();
    }

    public void deleteGradient(int gradientId){
        Gradient gradient = gradients.get(gradientId);
        if(gradient != null){
            gradient.free();
        }
    }

    public void deleteGradients(){
        Values<Gradient> values = gradients.values();
        while(values.hasNext){
            Gradient gradient = values.next();
            gradient.free();
        }
        images.clear();
    }

    public int getCurrentLayer(){
        return renderGraph.allLayers.size;
    }

    public int newLayer(Effect effect){
        return newLayer(effect, 0, 0, width, height, false);
    }

    public int newLayer(Effect effect, boolean cloneState){
        return newLayer(effect, 0, 0, width, height, cloneState);
    }

    public int newLayer(Effect effect, Rect bounds){
        return newLayer(effect, bounds.x, bounds.y, bounds.width, bounds.height, false);
    }

    public int newLayer(Effect effect, float left, float top, float width, float height){
        return newLayer(effect, left, top, width, height, false);
    }

    public int newLayer(Effect effect, Rect bounds, boolean cloneState){
        return newLayer(effect, bounds.x, bounds.y, bounds.width, bounds.height, cloneState);
    }

    public int newLayer(Effect effect, float left, float top, float width, float height, boolean cloneState){
        CanvasLayer newLayer = CanvasLayer.obtain();
        newLayer.effect = effect;
        newLayer.bounds.set(left, top, width, height);
        renderGraph.pushLayer(newLayer);
        saveState(cloneState);
        return renderGraph.layersStack.size;
    }

    public void restoreLayer(){
        renderGraph.popLayer();
    }

    public void restoreToLayer(int index){
        for(int i = 0; i < index; i++){
            restoreLayer();
        }
    }

    public int saveState(){
        return saveState(false);
    }

    public int saveState(boolean cloneState){
        CanvasState newState = new CanvasState();
        if(cloneState){
            newState.set(currentState);
        }
        states.add(newState);
        currentState = newState;
        return states.size;
    }

    CanvasState getCurrentState(){
        return currentState;
    }

    public boolean restoreState(){
        if(states.size > 1){
            states.remove(states.size - 1);
            currentState = states.peek();
            return true;
        }else{
            return false;
        }
    }

    public int restoreToState(int state){
        int stateToRestore = state < 1 ? 1 : state;
        int restored = 0;
        while(states.size > stateToRestore){
            states.remove(states.size - 1);
        }
        currentState = states.peek();
        return restored;
    }

    public float getGlobalAlpha(){
        return currentState.globalAlpha;
    }

    public void setGlobalAlpha(float globalAlpha){
        currentState.globalAlpha = globalAlpha;
    }

    public void mulGlobalAlpha(float alpha){
        currentState.mulGlobalAlpha(alpha);
    }

    public float getStrokeWidth(){
        return currentState.strokeWidth;
    }

    public void setStrokeWidth(float strokeWidth){
        currentState.strokeWidth = strokeWidth;
    }

    public float getStrokeMiterLimit(){
        return currentState.miterLimit;
    }

    public void setStrokeMiterLimit(float miterLimit){
        currentState.miterLimit = miterLimit;
    }

    public LineJoin getStrokeLineJoin(){
        return currentState.lineJoin;
    }

    public void setStrokeLineJoin(LineJoin lineJoin){
        currentState.lineJoin = lineJoin;
    }

    public LineCap getStrokeLineCap(){
        return currentState.lineCap;
    }

    public void setStrokeLineCap(LineCap lineCap){
        currentState.lineCap = lineCap;
    }

    public DrawingStyle getDrawingStyle(){
        return currentState.drawingStyle;
    }

    public void setDrawingStyle(DrawingStyle drawingStyle){
        currentState.drawingStyle = drawingStyle;
    }

    public BlendMode getBlendMode(){
        return currentState.blendMode;
    }

    public void setBlendMode(BlendMode blendMode){
        currentState.blendMode = blendMode;
    }

    public PointStyle getPointStyle(){
        return currentState.pointStyle;
    }

    public void setPointStyle(PointStyle pointStyle){
        currentState.pointStyle = pointStyle;
    }

    public Winding getWinding(){
        return currentState.winding;
    }

    public void setWinding(Winding winding){
        currentState.winding = winding == null ? Winding.none : winding;
    }

    public Paint getStrokePaint(){
        return currentState.strokePaint;
    }

    public void setStrokePaint(Paint paint){
        currentState.strokePaint.set(paint);
    }

    public void setStrokeToColor(int r, int g, int b, int a){
        currentState.strokePaint.setToColor(r, g, b, a);
    }

    public void setStrokeColor(int r, int g, int b, int a){
        currentState.strokePaint.setColor(r, g, b, a);
    }

    public void setStrokeToColor(float r, float g, float b, float a){
        currentState.strokePaint.setToColor(r, g, b, a);
    }

    public void setStrokeColor(float r, float g, float b, float a){
        currentState.strokePaint.setColor(r, g, b, a);
    }

    public void setStrokeToColor(int rgba){
        currentState.strokePaint.setToColor(rgba);
    }

    public void setStrokeColor(int rgba){
        currentState.strokePaint.setColor(rgba);
    }

    public void setStrokeToColor(Color color){
        currentState.strokePaint.setToColor(color);
    }

    public void setStrokeColor(Color color){
        currentState.strokePaint.setToColor(color);
    }

    public void setStrokeToImage(float left, float top, float width, float height, float angle, float alpha,
                                 Image image){
        currentState.strokePaint.setToImageRad(left, top, width, height, angle, alpha, image);
    }

    public void setStrokeToImage(float left, float top, float width, float height, float angle, float alpha,
                                 int imageId){
        Image image = getImage(imageId);
        currentState.strokePaint.setToImageRad(left, top, width, height, angle, alpha, image);
    }

    public void setStrokeImage(float left, float top, float width, float height, float angle, float alpha,
                               Image image){
        currentState.strokePaint.setImageRad(left, top, width, height, angle, alpha, image);
    }

    public void setStrokeImage(float left, float top, float width, float height, float angle, float alpha,
                               int imageId){
        Image image = getImage(imageId);
        currentState.strokePaint.setImageRad(left, top, width, height, angle, alpha, image);
    }

    public void setStrokeToLinearGradient(float startX, float startY, float endX, float endY, GradientSpread spread,
                                          int gradientId){
        currentState.strokePaint.setToLinearGradient(startX, startY, endX, endY, spread, getGradient(gradientId));
    }

    public void setStrokeToLinearGradient(float startX, float startY, float endX, float endY, GradientSpread spread,
                                          Gradient gradient){
        currentState.strokePaint.setToLinearGradient(startX, startY, endX, endY, spread, gradient);
    }

    public void setStrokeLinearGradient(float startX, float startY, float endX, float endY, GradientSpread spread,
                                        int gradientId){
        currentState.strokePaint.setLinearGradient(startX, startY, endX, endY, spread, getGradient(gradientId));
    }

    public void setStrokeLinearGradient(float startX, float startY, float endX, float endY, GradientSpread spread,
                                        Gradient gradient){
        currentState.strokePaint.setLinearGradient(startX, startY, endX, endY, spread, gradient);
    }

    public void setStrokeToLinearGradient(float startX, float startY, float endX, float endY, GradientSpread spread,
                                          AffineTransform xform, int gradientId){
        currentState.strokePaint.setToLinearGradient(startX, startY, endX, endY, spread, xform,
        getGradient(gradientId));
    }

    public void setStrokeToLinearGradient(float startX, float startY, float endX, float endY, GradientSpread spread,
                                          AffineTransform xform, Gradient gradient){
        currentState.strokePaint.setToLinearGradient(startX, startY, endX, endY, spread, xform, gradient);
    }

    public void setStrokeLinearGradient(float startX, float startY, float endX, float endY, GradientSpread spread,
                                        AffineTransform xform, int gradientId){
        currentState.strokePaint.setLinearGradient(startX, startY, endX, endY, spread, xform, getGradient(gradientId));
    }

    public void setStrokeLinearGradient(float startX, float startY, float endX, float endY, GradientSpread spread,
                                        AffineTransform xform, Gradient gradient){
        currentState.strokePaint.setLinearGradient(startX, startY, endX, endY, spread, xform, gradient);
    }

    public void setStrokeToRadialGradient(float centerX, float centerY, float focusX, float focusY,
                                          float gradientRadius, GradientSpread spread, int gradientId){
        currentState.strokePaint.setToRadialGradient(centerX, centerY, focusX, focusY, gradientRadius, spread,
        getGradient(gradientId));
    }

    public void setStrokeToRadialGradient(float centerX, float centerY, float focusX, float focusY,
                                          float gradientRadius, GradientSpread spread, Gradient gradient){
        currentState.strokePaint.setToRadialGradient(centerX, centerY, focusX, focusY, gradientRadius, spread,
        gradient);
    }

    public void setStokeRadialGradient(float centerX, float centerY, float focusX, float focusY, float gradientRadius,
                                       GradientSpread spread, int gradientId){
        currentState.strokePaint.setRadialGradient(centerX, centerY, focusX, focusY, gradientRadius, spread,
        getGradient(gradientId));
    }

    public void setStokeRadialGradient(float centerX, float centerY, float focusX, float focusY, float gradientRadius,
                                       GradientSpread spread, Gradient gradient){
        currentState.strokePaint.setRadialGradient(centerX, centerY, focusX, focusY, gradientRadius, spread, gradient);
    }

    public void setStrokeToRadialGradient(float centerX, float centerY, float focusX, float focusY,
                                          float gradientRadius, GradientSpread spread, AffineTransform xform, int gradientId){
        currentState.strokePaint.setToRadialGradient(centerX, centerY, focusX, focusY, gradientRadius, spread, xform,
        getGradient(gradientId));
    }

    public void setStrokeToRadialGradient(float centerX, float centerY, float focusX, float focusY,
                                          float gradientRadius, GradientSpread spread, AffineTransform xform, Gradient gradient){
        currentState.strokePaint.setToRadialGradient(centerX, centerY, focusX, focusY, gradientRadius, spread, xform,
        gradient);
    }

    public void setStokeRadialGradient(float centerX, float centerY, float focusX, float focusY, float gradientRadius,
                                       GradientSpread spread, AffineTransform xform, int gradientId){
        currentState.strokePaint.setRadialGradient(centerX, centerY, focusX, focusY, gradientRadius, spread, xform,
        getGradient(gradientId));
    }

    public void setStokeRadialGradient(float centerX, float centerY, float focusX, float focusY, float gradientRadius,
                                       GradientSpread spread, AffineTransform xform, Gradient gradient){
        currentState.strokePaint.setRadialGradient(centerX, centerY, focusX, focusY, gradientRadius, spread, xform,
        gradient);
    }

    public void setStrokeToBoxGradient(float x, float y, float width, float heigth, float gradientRadius,
                                       GradientSpread spread, int gradientId){
        currentState.strokePaint.setToBoxGradient(x, y, width, heigth, gradientRadius, spread, getGradient(gradientId));
    }

    public void setStrokeToBoxGradient(float x, float y, float width, float heigth, float gradientRadius,
                                       GradientSpread spread, Gradient gradient){
        currentState.strokePaint.setToBoxGradient(x, y, width, heigth, gradientRadius, spread, gradient);
    }

    public void setStrokeBoxGradient(float x, float y, float width, float heigth, float gradientRadius,
                                     GradientSpread spread, int gradientId){
        currentState.strokePaint.setBoxGradient(x, y, width, heigth, gradientRadius, spread, getGradient(gradientId));
    }

    public void setStrokeBoxGradient(float x, float y, float width, float heigth, float gradientRadius,
                                     GradientSpread spread, Gradient gradient){
        currentState.strokePaint.setBoxGradient(x, y, width, heigth, gradientRadius, spread, gradient);
    }

    public void setStrokeToBoxGradient(float x, float y, float width, float heigth, float gradientRadius,
                                       GradientSpread spread, AffineTransform xform, int gradientId){
        currentState.strokePaint.setToBoxGradient(x, y, width, heigth, gradientRadius, spread, xform,
        getGradient(gradientId));
    }

    public void setStrokeToBoxGradient(float x, float y, float width, float heigth, float gradientRadius,
                                       GradientSpread spread, AffineTransform xform, Gradient gradient){
        currentState.strokePaint.setToBoxGradient(x, y, width, heigth, gradientRadius, spread, xform, gradient);
    }

    public void setStrokeBoxGradient(float x, float y, float width, float heigth, float gradientRadius,
                                     GradientSpread spread, AffineTransform xform, int gradientId){
        currentState.strokePaint.setBoxGradient(x, y, width, heigth, gradientRadius, spread, xform,
        getGradient(gradientId));
    }

    public void setStrokeBoxGradient(float x, float y, float width, float heigth, float gradientRadius,
                                     GradientSpread spread, AffineTransform xform, Gradient gradient){
        currentState.strokePaint.setBoxGradient(x, y, width, heigth, gradientRadius, spread, xform, gradient);
    }

    public void setStrokeToConicalGradient(float centerX, float centerY, int gradientId){
        currentState.strokePaint.setToConicalGradient(centerX, centerY, getGradient(gradientId));
    }

    public void setStrokeToConicalGradient(float centerX, float centerY, Gradient gradient){
        currentState.strokePaint.setToConicalGradient(centerX, centerY, gradient);
    }

    public void setStrokeConicalGradient(float centerX, float centerY, int gradientId){
        currentState.strokePaint.setConicalGradient(centerX, centerY, getGradient(gradientId));
    }

    public void setStrokeConicalGradient(float centerX, float centerY, Gradient gradient){
        currentState.strokePaint.setConicalGradient(centerX, centerY, gradient);
    }

    public void setStrokeToConicalGradient(float centerX, float centerY, AffineTransform xform, int gradientId){
        currentState.strokePaint.setToConicalGradient(centerX, centerY, xform, getGradient(gradientId));
    }

    public void setStrokeToConicalGradient(float centerX, float centerY, AffineTransform xform, Gradient gradient){
        currentState.strokePaint.setToConicalGradient(centerX, centerY, xform, gradient);
    }

    public void setStrokeConicalGradient(float centerX, float centerY, AffineTransform xform, int gradientId){
        currentState.strokePaint.setConicalGradient(centerX, centerY, xform, getGradient(gradientId));
    }

    public void setStrokeConicalGradient(float centerX, float centerY, AffineTransform xform, Gradient gradient){
        currentState.strokePaint.setConicalGradient(centerX, centerY, xform, gradient);
    }

    public float getStrokeDashOffset(){
        return currentState.dashOffset;
    }

    public void setStrokeDashOffset(float dashOffset){
        currentState.dashOffset = dashOffset;
    }

    public FloatArray getStrokeDashArray(FloatArray out){
        out.addAll(currentState.dashArray);
        return out;
    }

    public void setStrokeDashArray(FloatArray dashArray){
        FloatArray dashes = currentState.dashArray;
        dashes.clear();
        dashes.addAll(dashArray);
    }

    public void setStrokeDashArray(float... dashArray){
        FloatArray currentDashArray = currentState.dashArray;
        currentDashArray.clear();
        currentDashArray.addAll(dashArray);
    }

    public Paint getFillPaint(){
        return currentState.fillPaint;
    }

    public void setFillPaint(Paint paint){
        currentState.fillPaint.set(paint);
    }

    public void setFillToColor(int r, int g, int b, int a){
        currentState.fillPaint.setToColor(r, g, b, a);
    }

    public void setFillColor(int r, int g, int b, int a){
        currentState.fillPaint.setColor(r, g, b, a);
    }

    public void setFillToColor(float r, float g, float b, float a){
        currentState.fillPaint.setToColor(r, g, b, a);
    }

    public void setFillColor(float r, float g, float b, float a){
        currentState.fillPaint.setColor(r, g, b, a);
    }

    public void setFillToColor(int rgba){
        currentState.fillPaint.setToColor(rgba);
    }

    public void setFillColor(int rgba){
        currentState.fillPaint.setColor(rgba);
    }

    public void setFillToColor(Color color){
        currentState.fillPaint.setToColor(color);
    }

    public void setFillColor(Color color){
        currentState.fillPaint.setToColor(color);
    }

    public void setFillToImage(float centerX, float centerY, float width, float height, float angle, float alpha,
                               Image image){
        currentState.fillPaint.setToImageRad(centerX, centerY, width, height, angle, alpha, image);
    }

    public void setFillToImage(float left, float top, float width, float height, float angle, float alpha,
                               int imageId){
        Image image = getImage(imageId);
        currentState.fillPaint.setToImageRad(left, top, width, height, angle, alpha, image);
    }

    private Image getImage(int imageId){
        Image image = images.get(imageId);
        if(image == null){
            throw new ArcRuntimeException("Can't find image with id: " + imageId);
        }
        return image;
    }

    public void setFillImage(float left, float top, float width, float height, float angle, float alpha, Image image){
        currentState.fillPaint.setImageRad(left, top, width, height, angle, alpha, image);
    }

    public void setFillImage(float left, float top, float width, float height, float angle, float alpha, int imageId){
        Image image = getImage(imageId);
        currentState.fillPaint.setImageRad(left, top, width, height, angle, alpha, image);
    }

    public void setFillToLinearGradient(float startX, float startY, float endX, float endY, GradientSpread spread,
                                        int gradientId){
        currentState.fillPaint.setToLinearGradient(startX, startY, endX, endY, spread, getGradient(gradientId));
    }

    private Gradient getGradient(int gradientId){
        Gradient gradient = gradients.get(gradientId);
        if(gradient == null){
            throw new ArcRuntimeException("Can't find gradient with id: " + gradientId);
        }
        return gradient;
    }

    public void setFillToLinearGradient(float startX, float startY, float endX, float endY, GradientSpread spread,
                                        Gradient gradient){
        currentState.fillPaint.setToLinearGradient(startX, startY, endX, endY, spread, gradient);
    }

    public void setFillLinearGradient(float startX, float startY, float endX, float endY, GradientSpread spread,
                                      int gradientId){
        currentState.fillPaint.setLinearGradient(startX, startY, endX, endY, spread, getGradient(gradientId));
    }

    public void setFillLinearGradient(float startX, float startY, float endX, float endY, GradientSpread spread,
                                      Gradient gradient){
        currentState.fillPaint.setLinearGradient(startX, startY, endX, endY, spread, gradient);
    }

    public void setFillToLinearGradient(float startX, float startY, float endX, float endY, GradientSpread spread,
                                        AffineTransform xform, int gradientId){
        currentState.fillPaint.setToLinearGradient(startX, startY, endX, endY, spread, xform, getGradient(gradientId));
    }

    public void setFillToLinearGradient(float startX, float startY, float endX, float endY, GradientSpread spread,
                                        AffineTransform xform, Gradient gradient){
        currentState.fillPaint.setToLinearGradient(startX, startY, endX, endY, spread, xform, gradient);
    }

    public void setFillLinearGradient(float startX, float startY, float endX, float endY, GradientSpread spread,
                                      AffineTransform xform, int gradientId){
        currentState.fillPaint.setLinearGradient(startX, startY, endX, endY, spread, xform, getGradient(gradientId));
    }

    public void setFillLinearGradient(float startX, float startY, float endX, float endY, GradientSpread spread,
                                      AffineTransform xform, Gradient gradient){
        currentState.fillPaint.setLinearGradient(startX, startY, endX, endY, spread, xform, gradient);
    }

    public void setFillToRadialGradient(float centerX, float centerY, float focusX, float focusY, float gradientRadius,
                                        GradientSpread spread, int gradientId){
        currentState.fillPaint.setToRadialGradient(centerX, centerY, focusX, focusY, gradientRadius, spread,
        getGradient(gradientId));
    }

    public void setFillToRadialGradient(float centerX, float centerY, float focusX, float focusY, float gradientRadius,
                                        GradientSpread spread, Gradient gradient){
        currentState.fillPaint.setToRadialGradient(centerX, centerY, focusX, focusY, gradientRadius, spread, gradient);
    }

    public void setFillRadialGradient(float centerX, float centerY, float focusX, float focusY, float gradientRadius,
                                      GradientSpread spread, int gradientId){
        currentState.fillPaint.setRadialGradient(centerX, centerY, focusX, focusY, gradientRadius, spread,
        getGradient(gradientId));
    }

    public void setFillRadialGradient(float centerX, float centerY, float focusX, float focusY, float gradientRadius,
                                      GradientSpread spread, Gradient gradient){
        currentState.fillPaint.setRadialGradient(centerX, centerY, focusX, focusY, gradientRadius, spread, gradient);
    }

    public void setFillToRadialGradient(float centerX, float centerY, float focusX, float focusY, float gradientRadius,
                                        GradientSpread spread, AffineTransform xform, int gradientId){
        currentState.fillPaint.setToRadialGradient(centerX, centerY, focusX, focusY, gradientRadius, spread, xform,
        getGradient(gradientId));
    }

    public void setFillToRadialGradient(float centerX, float centerY, float focusX, float focusY, float gradientRadius,
                                        GradientSpread spread, AffineTransform xform, Gradient gradient){
        currentState.fillPaint.setToRadialGradient(centerX, centerY, focusX, focusY, gradientRadius, spread, xform,
        gradient);
    }

    public void setFillRadialGradient(float centerX, float centerY, float focusX, float focusY, float gradientRadius,
                                      GradientSpread spread, AffineTransform xform, int gradientId){
        currentState.fillPaint.setRadialGradient(centerX, centerY, focusX, focusY, gradientRadius, spread, xform,
        getGradient(gradientId));
    }

    public void setFillRadialGradient(float centerX, float centerY, float focusX, float focusY, float gradientRadius,
                                      GradientSpread spread, AffineTransform xform, Gradient gradient){
        currentState.fillPaint.setRadialGradient(centerX, centerY, focusX, focusY, gradientRadius, spread, xform,
        gradient);
    }

    public void setFillToBoxGradient(float x, float y, float width, float heigth, float gradientRadius,
                                     GradientSpread spread, int gradientId){
        currentState.fillPaint.setToBoxGradient(x, y, width, heigth, gradientRadius, spread, getGradient(gradientId));
    }

    public void setFillToBoxGradient(float x, float y, float width, float heigth, float gradientRadius,
                                     GradientSpread spread, Gradient gradient){
        currentState.fillPaint.setToBoxGradient(x, y, width, heigth, gradientRadius, spread, gradient);
    }

    public void setFillBoxGradient(float x, float y, float width, float heigth, float gradientRadius,
                                   GradientSpread spread, int gradientId){
        currentState.fillPaint.setBoxGradient(x, y, width, heigth, gradientRadius, spread, getGradient(gradientId));
    }

    public void setFillBoxGradient(float x, float y, float width, float heigth, float gradientRadius,
                                   GradientSpread spread, Gradient gradient){
        currentState.fillPaint.setBoxGradient(x, y, width, heigth, gradientRadius, spread, gradient);
    }

    public void setFillToBoxGradient(float x, float y, float width, float heigth, float gradientRadius,
                                     GradientSpread spread, AffineTransform xform, int gradientId){
        currentState.fillPaint.setToBoxGradient(x, y, width, heigth, gradientRadius, spread, xform,
        getGradient(gradientId));
    }

    public void setFillToBoxGradient(float x, float y, float width, float heigth, float gradientRadius,
                                     GradientSpread spread, AffineTransform xform, Gradient gradient){
        currentState.fillPaint.setToBoxGradient(x, y, width, heigth, gradientRadius, spread, xform, gradient);
    }

    public void setFillBoxGradient(float x, float y, float width, float heigth, float gradientRadius,
                                   GradientSpread spread, AffineTransform xform, int gradientId){
        currentState.fillPaint.setBoxGradient(x, y, width, heigth, gradientRadius, spread, xform,
        getGradient(gradientId));
    }

    public void setFillBoxGradient(float x, float y, float width, float heigth, float gradientRadius,
                                   GradientSpread spread, AffineTransform xform, Gradient gradient){
        currentState.fillPaint.setBoxGradient(x, y, width, heigth, gradientRadius, spread, xform, gradient);
    }

    public void setFillToConicalGradient(float centerX, float centerY, int gradientId){
        currentState.fillPaint.setToConicalGradient(centerX, centerY, getGradient(gradientId));
    }

    public void setFillToConicalGradient(float centerX, float centerY, Gradient gradient){
        currentState.fillPaint.setToConicalGradient(centerX, centerY, gradient);
    }

    public void setFillConicalGradient(float centerX, float centerY, int gradientId){
        currentState.fillPaint.setConicalGradient(centerX, centerY, getGradient(gradientId));
    }

    public void setFillConicalGradient(float centerX, float centerY, Gradient gradient){
        currentState.fillPaint.setConicalGradient(centerX, centerY, gradient);
    }

    public void setFillToConicalGradient(float centerX, float centerY, AffineTransform xform, int gradientId){
        currentState.fillPaint.setToConicalGradient(centerX, centerY, xform, getGradient(gradientId));
    }

    public void setFillToConicalGradient(float centerX, float centerY, AffineTransform xform, Gradient gradient){
        currentState.fillPaint.setToConicalGradient(centerX, centerY, xform, gradient);
    }

    public void setFillConicalGradient(float centerX, float centerY, AffineTransform xform, int gradientId){
        currentState.fillPaint.setConicalGradient(centerX, centerY, xform, getGradient(gradientId));
    }

    public void setFillConicalGradient(float centerX, float centerY, AffineTransform xform, Gradient gradient){
        currentState.fillPaint.setConicalGradient(centerX, centerY, xform, gradient);
    }

    public Font getFont(){
        return currentState.font;
    }

    public void setFont(Font font){
        currentState.font = font;
    }

    public void rotate(float degrees){
        currentState.xform.rotate(degrees);
    }

    public void rotateRad(float radians){
        currentState.xform.rotateRad(radians);
    }

    public void rotateAround(float degrees, float x, float y){
        currentState.xform.preTranslate(x, y).preRotate(degrees).preTranslate(-x, -y);
    }

    public void rotateAroundRad(float radians, float x, float y){
        currentState.xform.preTranslate(x, y).preRotateRad(radians).preTranslate(-x, -y);
    }

    public void scale(float scaleX, float scaleY){
        currentState.xform.scale(scaleX, scaleY);
    }

    public void scale(float scaleX, float scaleY, float x, float y){
        currentState.xform.preTranslate(x, y).preScale(scaleX, scaleY).preTranslate(-x, -y);
    }

    public void skew(float degreesX, float degreesY){
        currentState.xform.skew(degreesX, degreesY);
    }

    public void skewX(float degrees){
        currentState.xform.skewX(degrees);
    }

    public void skewY(float degrees){
        currentState.xform.skewY(degrees);
    }

    public void skewRad(float radiansX, float radiansY){
        currentState.xform.skewRad(radiansX, radiansY);
    }

    public void skewXRad(float radians){
        currentState.xform.skewXRad(radians);
    }

    public void skewYRad(float radians){
        currentState.xform.skewYRad(radians);
    }

    public void translate(float tx, float ty){
        currentState.xform.translate(tx, ty);
    }

    public void setTransform(AffineTransform transform){
        currentState.xform.set(transform);
    }

    public void preMultiplyTransform(AffineTransform transform){
        currentState.xform.mulLeft(transform);
    }

    public void multiplyTransform(AffineTransform transform){
        currentState.xform.mul(transform);
    }

    public void multiplyTransformLeft(AffineTransform transform){
        currentState.xform.mulLeft(transform);
    }

    public void resetTransform(){
        currentState.xform.idt();
    }

    public void clip(Path path){
        appendClipPath(path, ClipOperation.union);
    }

    public void clip(Path path, ClipOperation clipOperation){
        appendClipPath(path, clipOperation);
    }

    public void clip(Shape clipShape){
        clip(clipShape, ClipOperation.union);
    }

    public void clip(Shape clipShape, ClipOperation clipOperation){
        Clip clip = Clip.obtain(clipShape.getFillCall(this), clipOperation);
        currentState.addClip(clip);
    }

    public void clipRectangle(Rect clipRectangle){
        clipRectangle(clipRectangle, ClipOperation.union);
    }

    public void clipRectangle(Rect clipRectangle, ClipOperation clipOperation){
        path.rect(clipRectangle.x, clipRectangle.y, clipRectangle.width, clipRectangle.height);
        appendClipPath(clipOperation);
    }

    public void clipRectangle(float left, float top, float width, float height){
        clipRectangle(left, top, width, height, ClipOperation.union);
    }

    public void clipRectangle(float left, float top, float width, float height, ClipOperation clipOperation){
        path.rect(left, top, width, height);
        appendClipPath(clipOperation);
    }

    public void clearClip(){
        currentState.clips.clear();
    }

    public void setScissor(float x, float y, float width, float height){
        currentState.scissor.set(x, y, width, height, currentState.scissor.xform);
    }

    public void intersectScissor(float x, float y, float width, float height){
        currentState.scissor.intersect(x, y, width, height, currentState.scissor.xform);
    }

    public void resetScissor(){
        currentState.scissor.reset();
    }

    public void fillBackground(int r, int g, int b, int a){
        saveState();
        setFillToColor(r, g, b, a);
        drawRectangle(0, 0, width, height);
        restoreState();
    }

    public void fillBackground(float r, float g, float b, float a){
        saveState();
        setFillToColor(r, g, b, a);
        drawRectangle(0, 0, width, height);
        restoreState();
    }

    public void fillBackground(int r, int g, int b){
        saveState();
        setFillToColor(r, g, b, 255);
        drawRectangle(0, 0, width, height);
        restoreState();
    }

    public void fillBackground(Color color){
        fillBackground(color.r, color.g, color.b, color.a);
    }

    public void fillBackground(float r, float g, float b){
        saveState();
        setFillToColor(r, g, b, 1);
        drawRectangle(0, 0, width, height);
        restoreState();
    }

    public void fillBackground(Paint paint){
        saveState();
        setFillPaint(paint);
        drawRectangle(0, 0, width, height);
        restoreState();
    }

    private void appendDrawPath(){
        appendDrawPath(path);
        path.reset();
    }

    private void appendDrawPath(Path path){
        PathMesh pathMesh = PathMesh.obtain(this, path);

        DrawingStyle drawingStyle = currentState.drawingStyle;
        if(drawingStyle.drawFill()){
            GlCall call = pathMesh.createFillCall();
            call.clips.clear();
            call.clips.addAll(currentState.clips);
            calls.add(call);
            renderGraph.addCall(call);
        }

        if(drawingStyle.drawStroke()){
            GlCall call = pathMesh.createStrokeCall();
            call.clips.clear();
            call.clips.addAll(currentState.clips);
            calls.add(call);
            renderGraph.addCall(call);
        }

        pathMesh.free();
    }

    private void appendClipPath(ClipOperation clipOperation){
        appendClipPath(path, clipOperation);
        path.reset();
    }

    private void appendClipPath(Path path, ClipOperation clipOperation){
        PathMesh pathMesh = PathMesh.obtain(this, path);
        Clip clip = Clip.obtain(pathMesh.createFillCall(), clipOperation);
        clips.add(clip);
        currentState.addClip(clip);
        pathMesh.free();
    }

    public void drawRectangle(float left, float top, float width, float height){
        path.rect(left, top, width, height);
        appendDrawPath();
    }

    public void drawRoundRectangle(float left, float top, float width, float height, float radiusDegrees){
        path.roundedRect(left, top, width, height, Mathf.degreesToRadians * radiusDegrees);
        appendDrawPath();
    }

    public void drawRoundRectangleRad(float left, float top, float width, float height, float radiusRad){
        path.roundedRect(left, top, width, height, radiusRad);
        appendDrawPath();
    }

    public void drawRoundRectangle(float left, float top, float width, float height, float radiusXDegrees,
                                   float radiusYDegrees){
        path.roundedRect(left, top, width, height, Mathf.degreesToRadians * radiusXDegrees,
        Mathf.degreesToRadians * radiusYDegrees);
        appendDrawPath();
    }

    public void drawRoundRectangleRad(float left, float top, float width, float height, float radiusXRad,
                                      float radiusYRad){
        path.roundedRect(left, top, width, height, radiusXRad, radiusYRad);
        appendDrawPath();
    }

    public void drawEllipse(float centerX, float centerY, float radiusX, float radiusY){
        path.ellipse(centerX, centerY, radiusX, radiusY);
        appendDrawPath();
    }

    public void drawCircle(float centerX, float centerY, float radius){
        path.ellipse(centerX, centerY, radius, radius);
        appendDrawPath();
    }

    public void drawArc(float startX, float startY, float endX, float endY, float radiusDegrees){
        path.arcTo(startX, startY, endX, endY, Mathf.degreesToRadians * radiusDegrees);
        appendDrawPath();
    }

    public void drawArcRad(float startX, float startY, float endX, float endY, float radiusRad){
        path.arcTo(startX, startY, endX, endY, radiusRad);
        appendDrawPath();
    }

    public void drawArc(float radiusX, float radiusY, float angleDegrees, boolean largeArcFlag, boolean sweepFlag,
                        float x, float y){
        path.arcTo(radiusX, radiusY, angleDegrees, largeArcFlag, sweepFlag, x, y);
        appendDrawPath();
    }

    public void drawArcRad(float radiusX, float radiusY, float angleRad, boolean largeArcFlag, boolean sweepFlag,
                           float x, float y){
        path.arcToRad(radiusX, radiusY, angleRad, largeArcFlag, sweepFlag, x, y);
        appendDrawPath();
    }

    public void drawArc(float centerX, float centerY, float radius, float startAngleDegrees, float endAngleDegrees,
                        Direction direction){
        path.arc(centerX, centerY, radius, Mathf.degreesToRadians * startAngleDegrees,
        Mathf.degreesToRadians * endAngleDegrees, direction);
        appendDrawPath();
    }

    public void drawArc(float centerX, float centerY, float radiusX, float radiusY, float startAngleDegrees,
                        float endAngleDegrees, Direction direction){
        path.arc(centerX, centerY, radiusX, radiusY, Mathf.degreesToRadians * startAngleDegrees,
        Mathf.degreesToRadians * endAngleDegrees, direction);
        appendDrawPath();
    }

    public void drawArcRad(float centerX, float centerY, float radiusX, float radiusY, float startAngleRad,
                           float endAngleRad, Direction direction){
        path.arc(centerX, centerY, radiusX, radiusY, startAngleRad, endAngleRad, direction);
        appendDrawPath();
    }

    public void drawShape(Shape shape){
        DrawingStyle drawingStyle = currentState.drawingStyle;
        if(drawingStyle.drawFill()){
            renderGraph.addCall(shape.getFillCall(this));
        }

        if(drawingStyle.drawStroke()){
            renderGraph.addCall(shape.getStrokeCall(this));
        }
    }

    public void drawTexturedVertices(VertexMode mode, float... vertices){
        if(currentState.fillPaint.image == null){
            return;
        }
        TrianglesMesh trianglesMesh = TrianglesMesh.obtain(this);
        GlCall call = trianglesMesh.createTexturedCall(mode, vertices);
        calls.add(call);
        renderGraph.addCall(call);
        trianglesMesh.free();
    }

    public void drawTexturedVertices(Image image, VertexMode mode, float... vertices){
        saveState(false);
        setFillToImage(1, 1, 1, 1, 0, 1, image);
        TrianglesMesh trianglesMesh = TrianglesMesh.obtain(this);
        GlCall call = trianglesMesh.createTexturedCall(mode, vertices);
        calls.add(call);
        renderGraph.addCall(call);
        trianglesMesh.free();
        restoreState();
    }

    public void drawTexturedVertices(int image, VertexMode mode, float... vertices){
        saveState(false);
        setFillToImage(1, 1, 1, 1, 0, 1, image);
        TrianglesMesh trianglesMesh = TrianglesMesh.obtain(this);
        GlCall call = trianglesMesh.createTexturedCall(mode, vertices);
        calls.add(call);
        renderGraph.addCall(call);
        trianglesMesh.free();
        restoreState();
    }

    public void drawTexturedVertices(VertexMode mode, FloatArray vertices){
        if(currentState.fillPaint.image == null){
            return;
        }
        TrianglesMesh trianglesMesh = TrianglesMesh.obtain(this);
        GlCall call = trianglesMesh.createTexturedCall(mode, vertices);
        calls.add(call);
        renderGraph.addCall(call);
        trianglesMesh.free();
    }

    public void drawTexturedVertices(Image image, VertexMode mode, FloatArray vertices){
        saveState(false);
        setFillToImage(1, 1, 1, 1, 0, 1, image);
        TrianglesMesh trianglesMesh = TrianglesMesh.obtain(this);
        GlCall call = trianglesMesh.createTexturedCall(mode, vertices);
        calls.add(call);
        renderGraph.addCall(call);
        trianglesMesh.free();
        restoreState();
    }

    public void drawTexturedVertices(int image, VertexMode mode, FloatArray vertices){
        saveState(false);
        setFillToImage(1, 1, 1, 1, 0, 1, image);
        TrianglesMesh trianglesMesh = TrianglesMesh.obtain(this);
        GlCall call = trianglesMesh.createTexturedCall(mode, vertices);
        calls.add(call);
        renderGraph.addCall(call);
        trianglesMesh.free();
        restoreState();
    }

    public void drawTexturedVertices(VertexMode mode, Vec2... vertices){
        if(currentState.fillPaint.image == null){
            return;
        }
        TrianglesMesh trianglesMesh = TrianglesMesh.obtain(this);
        GlCall call = trianglesMesh.createTexturedCall(mode, vertices);
        calls.add(call);
        renderGraph.addCall(call);
        trianglesMesh.free();
    }

    public void drawTexturedVertices(Image image, VertexMode mode, Vec2... vertices){
        saveState(false);
        setFillToImage(1, 1, 1, 1, 0, 1, image);
        TrianglesMesh trianglesMesh = TrianglesMesh.obtain(this);
        GlCall call = trianglesMesh.createTexturedCall(mode, vertices);
        calls.add(call);
        renderGraph.addCall(call);
        trianglesMesh.free();
        restoreState();
    }

    public void drawTexturedVertices(int image, VertexMode mode, Vec2... vertices){
        saveState(false);
        setFillToImage(1, 1, 1, 1, 0, 1, image);
        TrianglesMesh trianglesMesh = TrianglesMesh.obtain(this);
        GlCall call = trianglesMesh.createTexturedCall(mode, vertices);
        calls.add(call);
        renderGraph.addCall(call);
        trianglesMesh.free();
        restoreState();
    }

    public void drawTexturedVertices(VertexMode mode, Vertex... vertices){
        if(currentState.fillPaint.image == null){
            return;
        }
        TrianglesMesh trianglesMesh = TrianglesMesh.obtain(this);
        GlCall call = trianglesMesh.createTexturedCall(mode, vertices);
        calls.add(call);
        renderGraph.addCall(call);
        trianglesMesh.free();
    }

    public void drawTexturedVertices(Image image, VertexMode mode, Vertex... vertices){
        saveState(false);
        setFillToImage(1, 1, 1, 1, 0, 1, image);
        TrianglesMesh trianglesMesh = TrianglesMesh.obtain(this);
        GlCall call = trianglesMesh.createTexturedCall(mode, vertices);
        calls.add(call);
        renderGraph.addCall(call);
        trianglesMesh.free();
        restoreState();
    }

    public void drawTexturedVertices(int image, VertexMode mode, Vertex... vertices){
        saveState(false);
        setFillToImage(1, 1, 1, 1, 0, 1, image);
        TrianglesMesh trianglesMesh = TrianglesMesh.obtain(this);
        GlCall call = trianglesMesh.createTexturedCall(mode, vertices);
        calls.add(call);
        renderGraph.addCall(call);
        trianglesMesh.free();
        restoreState();
    }

    public void drawVertices(VertexMode mode, float... vertices){
        TrianglesMesh trianglesMesh = TrianglesMesh.obtain(this);
        GlCall call = trianglesMesh.createCall(mode, vertices);
        calls.add(call);
        renderGraph.addCall(call);
        trianglesMesh.free();
    }

    public void drawVertices(VertexMode mode, FloatArray vertices){
        TrianglesMesh trianglesMesh = TrianglesMesh.obtain(this);
        GlCall call = trianglesMesh.createCall(mode, vertices);
        calls.add(call);
        renderGraph.addCall(call);
        trianglesMesh.free();
    }

    public void drawVertices(VertexMode mode, Vec2... vertices){
        TrianglesMesh trianglesMesh = TrianglesMesh.obtain(this);
        GlCall call = trianglesMesh.createCall(mode, vertices);
        calls.add(call);
        renderGraph.addCall(call);
        trianglesMesh.free();
    }

    public void drawLine(float startX, float startY, float stopX, float stopY){
        path.moveTo(startX, startY).lineTo(stopX, stopY);
        appendDrawPath();
    }

    public void drawLines(float... points){
        drawLines(points, 0, points.length / 4);
    }

    public void drawLines(float[] points, int offset, int count){
        int i = offset;
        while(i <= offset + (count * 4) - 1){
            path.moveTo(points[i++], points[i++]).lineTo(points[i++], points[i++]);
        }
        appendDrawPath();
    }

    public void drawLines(FloatArray points){
        drawLines(points, 0, points.size / 4);
    }

    public void drawLines(FloatArray points, int offset, int count){
        int i = offset;
        while(i <= offset + (count * 4) - 1){
            path.moveTo(points.get(i++), points.get(i++)).lineTo(points.get(i++), points.get(i++));
        }
        appendDrawPath();
    }

    public void drawLines(Vec2... points){
        drawLines(points, 0, points.length / 2);
    }

    public void drawLines(Vec2[] points, int offset, int count){
        int i = offset;
        while(i <= offset + (count * 2) - 1){
            Vec2 start = points[i++];
            Vec2 end = points[i++];
            path.moveTo(start.x, start.y).lineTo(end.x, end.y);
        }
        appendDrawPath();
    }

    public void drawPolyline(float... points){
        if(points.length < 4){
            return;
        }

        path.moveTo(points[0], points[1]);
        for(int i = 2; i < points.length; i += 2){
            path.lineTo(points[i], points[i + 1]);
        }
        appendDrawPath();
    }

    public void drawPolyline(FloatArray points){
        if(points.size < 4){
            return;
        }

        path.moveTo(points.get(0), points.get(1));
        for(int i = 2; i < points.size; i += 2){
            path.lineTo(points.get(i), points.get(i + 1));
        }
        appendDrawPath();
    }

    public void drawPolyline(Vec2... points){
        if(points.length < 2){
            return;
        }

        path.moveTo(points[0].x, points[0].y);
        for(int i = 1; i < points.length; i++){
            path.lineTo(points[i].x, points[i].y);
        }
        appendDrawPath();
    }

    public void drawPolygon(float... points){
        if(points.length < 4){
            return;
        }

        int i = 0;
        path.moveTo(points[i++], points[i++]);
        while(i < points.length){
            path.lineTo(points[i++], points[i++]);
        }
        path.close();
        appendDrawPath();
    }

    public void drawPolygon(FloatArray points){
        if(points.size < 4){
            return;
        }

        int i = 0;
        path.moveTo(points.get(i++), points.get(i++));
        while(i < points.size){
            path.lineTo(points.get(i++), points.get(i++));
        }
        path.close();
        appendDrawPath();
    }

    public void drawPolygon(Vec2... points){
        if(points.length < 2){
            return;
        }

        int i = 0;
        path.moveTo(points[i].x, points[i].y);
        i++;

        while(i < points.length){
            path.lineTo(points[i].x, points[i].y);
            i++;
        }
        path.close();
        appendDrawPath();
    }

    public void drawPoint(float centerX, float centerY){
        saveState(true);
        setFillPaint(getStrokePaint());
        setDrawingStyle(DrawingStyle.fill);
        switch(getPointStyle()){
            case circle:
                drawCircle(centerX, centerY, currentState.strokeWidth);
                break;
            case square:
                float strokeWidth = currentState.strokeWidth;
                float halfStrokeWidth = strokeWidth * 0.5f;
                drawRectangle(centerX - halfStrokeWidth, centerY - halfStrokeWidth, strokeWidth, strokeWidth);
                break;
        }
        restoreState();
    }

    public void drawPoints(float... pts){
        drawPoints(pts, 0, pts.length / 2);
    }

    public void drawPoints(float[] pts, int offset, int count){
        saveState(true);
        setFillPaint(getStrokePaint());
        setDrawingStyle(DrawingStyle.fill);

        float strokeWidth = currentState.strokeWidth;
        int i = offset;

        switch(getPointStyle()){
            case circle:
                while(i <= offset + (count * 2) - 2){
                    float x = pts[i++];
                    float y = pts[i++];
                    path.circle(x, y, strokeWidth);
                }
                break;
            case square:
                float halfStrokeWidth = strokeWidth * 0.5f;
                while(i <= offset + (count * 2) - 2){
                    float x = pts[i++];
                    float y = pts[i++];
                    path.rect(x - halfStrokeWidth, y - halfStrokeWidth, strokeWidth, strokeWidth);
                }
                break;
        }
        appendDrawPath();
        restoreState();
    }

    public void drawPoints(FloatArray pts){
        drawPoints(pts, 0, pts.size / 2);
    }

    public void drawPoints(FloatArray pts, int offset, int count){
        saveState(true);
        setFillPaint(getStrokePaint());
        setDrawingStyle(DrawingStyle.fill);

        float strokeWidth = currentState.strokeWidth;
        int i = offset;

        switch(getPointStyle()){
            case circle:
                while(i <= offset + (count * 2) - 2){
                    float x = pts.get(i++);
                    float y = pts.get(i++);
                    path.circle(x, y, strokeWidth);
                }
                break;
            case square:
                float halfStrokeWidth = strokeWidth * 0.5f;
                while(i <= offset + (count * 2) - 2){
                    float x = pts.get(i++);
                    float y = pts.get(i++);
                    path.rect(x - halfStrokeWidth, y - halfStrokeWidth, strokeWidth, strokeWidth);
                }
                break;
        }
        appendDrawPath();
        restoreState();
    }

    public void drawPoints(Vec2... pts){
        drawPoints(pts, 0, pts.length);
    }

    public void drawPoints(Vec2[] pts, int offset, int count){
        saveState(true);
        setFillPaint(getStrokePaint());
        setDrawingStyle(DrawingStyle.fill);

        float strokeWidth = currentState.strokeWidth;
        int i = offset;

        switch(getPointStyle()){
            case circle:
                while(i <= offset + count - 1){
                    float x = pts[i].x;
                    float y = pts[i].y;
                    path.circle(x, y, strokeWidth);
                    i++;
                }
                break;
            case square:
                float halfStrokeWidth = strokeWidth * 0.5f;
                while(i <= offset + count - 1){
                    float x = pts[i].x;
                    float y = pts[i].y;
                    path.rect(x - halfStrokeWidth, y - halfStrokeWidth, strokeWidth, strokeWidth);
                    i++;
                }
                break;
        }
        appendDrawPath();
        restoreState();
    }

    public void drawImage(int imageId){
        drawImage(getImage(imageId));
    }

    public void drawImage(Image image){
        saveState();
        int imageWidth = image.texture.getWidth();
        int imageHeight = image.texture.getHeight();
        setFillToImage(0, 0, imageWidth, imageHeight, 0, 1, image);
        drawRectangle(0, 0, imageWidth, imageHeight);
        restoreState();
    }

    public void drawImage(int imageId, float left, float top){
        drawImage(getImage(imageId), left, top);
    }

    public void drawImage(Image image, Vec2 leftTop){
        drawImage(image, leftTop.x, leftTop.y);
    }

    public void drawImage(int imageId, Vec2 leftTop){
        drawImage(getImage(imageId), leftTop.x, leftTop.y);
    }

    public void drawImage(Image image, float left, float top){
        saveState();
        int imageWidth = image.texture.getWidth();
        int imageHeight = image.texture.getHeight();
        setFillToImage(left, top, imageWidth, imageHeight, 0, 1, image);
        drawRectangle(left, top, imageWidth, imageHeight);
        restoreState();
    }

    public void drawImage(int imageId, Rect rectangle){
        drawImage(getImage(imageId), rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    public void drawImage(Image image, Rect rectangle){
        drawImage(image, rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    public void drawImage(int imageId, float left, float top, float width, float height){
        drawImage(getImage(imageId), left, top, width, height);
    }

    public void drawImage(Image image, float left, float top, float width, float height){
        saveState();
        setFillToImage(left, top, width, height, 0, 1, image);
        drawRectangle(left, top, width, height);
        restoreState();
    }

    public void drawImagePart(int imageId, Rect part){
        drawImagePart(getImage(imageId), part, part);
    }

    public void drawImagePart(Image image, Rect part){
        drawImagePart(image, part, part);
    }

    public void drawImagePart(int imageId, Rect part, Rect destination){
        drawImagePart(getImage(imageId), part, destination);
    }

    public void drawImagePart(Image image, Rect part, Rect destination){
        saveState();
        int imageWidth = image.texture.getWidth();
        int imageHeight = image.texture.getHeight();

        float widthRatio = destination.width / part.width;
        float finalWidth = imageWidth * widthRatio;

        float heightRatio = destination.height / part.height;
        float finalHeight = imageHeight * heightRatio;

        float translationX = (destination.x - part.x * widthRatio);
        float translationY = (destination.y - part.y * heightRatio);

        setFillToImage(translationX, translationY, finalWidth, finalHeight, 0, 1, image);
        drawRectangle(destination.x, destination.y, destination.width, destination.height);
        restoreState();
    }

    public void drawPath(Path path){
        appendDrawPath(path);
    }

    public PathBuilder newPath(){
        return pathBuilder.start();
    }

    public void drawText(float x, float y, CharSequence text){
        AffineTransform tempXform = AffineTransform.obtain();
        tempXform.set(currentState.xform);
        saveState(true);
        float scale = 0.0285f;
        float weight = 1f;
        tempXform.scale(scale * weight, scale);
        // tempXform.skewX(-25); //(italics)
        tempXform.translate(x, y);
        Font font = currentState.font;
        float advance = 0;
        Glyph lastGlyph = null;
        int codePointCount = Character.codePointCount(text, 0, text.length());
        for(int i = 0; i < codePointCount; i++){
            Glyph glyph = font.getGlyph(Character.codePointAt(text, i));
            if(lastGlyph != null){
                advance += font.getHorisontalKerning(lastGlyph, glyph) * scale * weight;
            }

            tempXform.translate(advance, 0);
            Path outline = glyph.getOutline();
            path.append(outline, tempXform);
            advance = glyph.getAdvanceWidth() * scale * weight;
            lastGlyph = glyph;
        }
        appendDrawPath();
        restoreState();
        tempXform.free();
    }

    public void drawHorizontalText(float x, float y, String text){
        AffineTransform tempXform = AffineTransform.obtain();
        tempXform.set(currentState.xform);
        saveState(true);
        float scale = 0.0065f;
        float weight = 1f;
        tempXform.scale(scale * weight, scale);
        // tempXform.skewX(-25);
        tempXform.translate(x, y);
        Font font = currentState.font;
        float advance = 0;
        Glyph lastGlyph = null;
        int codePointCount = text.codePointCount(0, text.length());
        for(int i = 0; i < codePointCount; i++){
            Glyph glyph = font.getGlyph(text.codePointAt(i));
            if(lastGlyph != null){
                advance += font.getHorisontalKerning(lastGlyph, glyph) * scale * weight;
            }

            tempXform.translate(advance, 0);
            path.append(glyph.getOutline(), tempXform);
            advance = glyph.getAdvanceWidth() * scale * weight;
            lastGlyph = glyph;
        }
        appendDrawPath();
        restoreState();
        tempXform.free();
    }

    public void render(){
        renderGraph.render();
    }

    public float getDevicePixelRatio(){
        return devicePixelRatio;
    }

    public boolean isAntiAlias(){
        return flags.isAntiAlias();
    }

    public boolean isStencilStrokes(){
        return flags.isStencilStrokes();
    }

    // TODO handling flags changes
    /*
     * public void setFlags(CanvasFlag... flags) { this.flags.set(flags); }
     *
     * public void clearFlags() { flags.clear(); }
     */

    public boolean isDebug(){
        return flags.isDebug();
    }

    /*
     * public void setAntiAlias(boolean antialias) { flags.setAntiAlias(antialias); }
     */

    public void clear(){
        CanvasUtils.resetArray(calls);
        CanvasUtils.resetArray(clips);
        CanvasUtils.resetArray(states);
        renderGraph.clear();
        saveState();
    }

    /*
     * public void setStencilStrokes(boolean stencilStrokes) { flags.setStencilStrokes(stencilStrokes); }
     */

    @Override
    public void reset(){
        width = 0;
        height = 0;
        devicePixelRatio = 0;
        tesselationTolerance = 0;
        fringeWidth = 0;

        imageIndex = 0;
        deleteImages();

        gradientIndex = 0;
        deleteGradients();

        path.reset();
        pathBuilder.builderPath.reset();

        flags.clear();

        currentState = null;
        CanvasUtils.resetArray(states);
        CanvasUtils.resetArray(calls);
        CanvasUtils.resetArray(clips);

        renderGraph.reset();
        glContext.reset();
    }

    /*
     * public void setDebug(boolean debug) { flags.setDebug(debug); }
     */

    @Override
    public void dispose(){
        reset();
    }

    public void free(){
        Pools.free(this);
    }

    public interface TextLayoutContext{

        // TODO
    }

    public static abstract class TextLayout{
        private final Vec2 penPosition = new Vec2();

        public abstract void appendPath(String text, Path path);
    }

    public class GradientBuilder{
        final FloatArray stops = new FloatArray();

        GradientBuilder start(){
            stops.clear();
            return this;
        }

        public GradientBuilder add(float offset, int rgba){
            stops.add(offset);
            stops.add(((rgba & 0xff000000) >>> 24) / 255f);
            stops.add(((rgba & 0x00ff0000) >>> 16) / 255f);
            stops.add(((rgba & 0x0000ff00) >>> 8) / 255f);
            stops.add(((rgba & 0x000000ff)) / 255f);
            return this;
        }

        public GradientBuilder add(float offset, Color color){
            stops.add(offset);
            stops.add(color.r);
            stops.add(color.g);
            stops.add(color.b);
            stops.add(color.a);
            return this;
        }

        public GradientBuilder add(float offset, int r, int g, int b, int a){
            stops.add(offset);
            stops.add(r / 255f);
            stops.add(g / 255f);
            stops.add(b / 255f);
            stops.add(a / 255f);
            return this;
        }

        public GradientBuilder add(float offset, float r, float g, float b, float a){
            stops.add(offset);
            stops.add(r);
            stops.add(g);
            stops.add(b);
            stops.add(a);
            return this;
        }

        public int build(){
            return createGradient(stops);
        }
    }

    public class PathBuilder{
        private final Path builderPath = new Path();

        PathBuilder start(){
            builderPath.reset();
            return this;
        }

        public PathBuilder moveToRel(float x, float y){
            builderPath.moveToRel(x, y);
            return this;
        }

        public PathBuilder moveTo(float x, float y){
            builderPath.moveTo(x, y);
            return this;
        }

        public PathBuilder verticalMoveTo(float y){
            builderPath.verticalMoveTo(y);
            return this;
        }

        public PathBuilder verticalMoveToRel(float y){
            builderPath.verticalMoveToRel(y);
            return this;
        }

        public PathBuilder horizontalMoveTo(float x){
            builderPath.horizontalMoveTo(x);
            return this;
        }

        public PathBuilder horizontalMoveToRel(float x){
            builderPath.horizontalMoveToRel(x);
            return this;
        }

        public PathBuilder lineToRel(float x, float y){
            builderPath.lineToRel(x, y);
            return this;
        }

        public PathBuilder lineTo(float x, float y){
            builderPath.lineTo(x, y);
            return this;
        }

        public PathBuilder verticalLineTo(float y){
            builderPath.verticalLineTo(y);
            return this;
        }

        public PathBuilder verticalLineToRel(float y){
            builderPath.verticalLineToRel(y);
            return this;
        }

        public PathBuilder horizontalLineTo(float x){
            builderPath.horizontalLineTo(x);
            return this;
        }

        public PathBuilder horizontalLineToRel(float x){
            builderPath.horizontalLineToRel(x);
            return this;
        }

        public PathBuilder cubicToRel(float controlX1, float controlY1, float controlX2, float controlY2, float x,
                                      float y){
            builderPath.cubicToRel(controlX1, controlY1, controlX2, controlY2, x, y);
            return this;
        }

        public PathBuilder cubicTo(float controlX1, float controlY1, float controlX2, float controlY2, float x,
                                   float y){
            builderPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y);
            return this;
        }

        public PathBuilder cubicSmoothTo(float controlX2, float controlY2, float x, float y){
            builderPath.cubicSmoothTo(controlX2, controlY2, x, y);
            return this;
        }

        public PathBuilder cubicSmoothToRel(float controlX2, float controlY2, float x, float y){
            builderPath.cubicSmoothToRel(controlX2, controlY2, x, y);
            return this;
        }

        public PathBuilder quadToRel(float controlX, float controlY, float x, float y){
            builderPath.quadToRel(controlX, controlY, x, y);
            return this;
        }

        public PathBuilder quadTo(float controlX, float controlY, float x, float y){
            builderPath.quadTo(controlX, controlY, x, y);
            return this;
        }

        public PathBuilder quadSmoothTo(float x, float y){
            builderPath.quadSmoothTo(x, y);
            return this;
        }

        public PathBuilder quadSmoothToRel(float x, float y){
            builderPath.quadSmoothToRel(x, y);
            return this;
        }

        public PathBuilder arcToRad(float radiusX, float radiusY, float angleRadians, boolean largeArcFlag,
                                    boolean sweepFlag, float x, float y){
            builderPath.arcToRad(radiusX, radiusY, angleRadians, largeArcFlag, sweepFlag, x, y);
            return this;
        }

        public PathBuilder arcToRadRel(float radiusX, float radiusY, float angleRadians, boolean largeArcFlag,
                                       boolean sweepFlag, float x, float y){
            builderPath.arcToRadRel(radiusX, radiusY, angleRadians, largeArcFlag, sweepFlag, x, y);
            return this;
        }

        public PathBuilder arcToRel(float radiusX, float radiusY, float angleDegrees, boolean largeArcFlag,
                                    boolean sweepFlag, float x, float y){
            builderPath.arcToRel(radiusX, radiusY, angleDegrees, largeArcFlag, sweepFlag, x, y);
            return this;
        }

        public PathBuilder arcTo(float radiusX, float radiusY, float angleDegrees, boolean largeArcFlag,
                                 boolean sweepFlag, float x, float y){
            builderPath.arcTo(radiusX, radiusY, angleDegrees, largeArcFlag, sweepFlag, x, y);
            return this;
        }

        public PathBuilder arcToRel(float startX, float startY, float endX, float endY, float radius){
            builderPath.arcToRel(startX, startY, endX, endY, radius);
            return this;
        }

        public PathBuilder arcTo(float startX, float startY, float endX, float endY, float radius){
            builderPath.arcTo(startX, startY, endX, endY, radius);
            return this;
        }

        public PathBuilder close(){
            builderPath.close();
            return this;
        }

        public void draw(){
            appendDrawPath(builderPath);
            builderPath.reset();
        }

        public void clip(ClipOperation clipOperation){
            appendClipPath(builderPath, clipOperation);
            builderPath.reset();
        }
    }
}
