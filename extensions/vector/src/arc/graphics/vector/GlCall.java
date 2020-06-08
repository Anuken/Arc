package arc.graphics.vector;

import arc.struct.*;
import arc.util.pooling.Pool.*;
import arc.util.pooling.*;

public class GlCall implements Poolable{
    final GlUniforms uniform = new GlUniforms();
    final GlUniforms stencilStrokesUniform = new GlUniforms();
    final Seq<GlPathComponent> components = new Seq<>();
    final Seq<Vertex> triangleVertices = new Seq<>();
    final Seq<Clip> clips = new Seq<>();
    CallType callType = CallType.none;
    BlendMode blendMode = BlendMode.over;
    int triangleVerticesOffset = -1;
    VertexMode vertexMode = VertexMode.triangles;

    public GlCall(){
    }

    static GlCall obtain(){
        return Pools.obtain(GlCall.class, GlCall::new);
    }

    static GlCall obtainFrameBufferRenderCall(){
        GlCall call = Pools.obtain(GlCall.class, GlCall::new);
        call.callType = CallType.triangles;
        call.uniform.initForFrameBuffer();
        return call;
    }

    void newTriangleVertex(float x, float y, float u, float v){
        triangleVertices.add(Vertex.obtain(x, y, u, v));
    }

    void initUniform(AffineTransform globalXform, float globalAlpha, Scissor scissor, Paint paint, float width,
                     float fringe, float strokeThr){
        uniform.init(globalXform, globalAlpha, paint, scissor, width, fringe, strokeThr, false);
    }

    void initUniform(AffineTransform globalXform, float globalAlpha, Scissor scissor, Paint paint, float width,
                     float fringe, float strokeThr, boolean texturedVertices){
        uniform.init(globalXform, globalAlpha, paint, scissor, width, fringe, strokeThr, texturedVertices);
    }

    void initStrokesUniform(AffineTransform globalXform, float globalAlpha, Scissor scissor, Paint paint, float width,
                            float fringe, float strokeThr){
        stencilStrokesUniform.init(globalXform, globalAlpha, paint, scissor, width, fringe, strokeThr, false);
    }

    @Override
    public void reset(){
        callType = CallType.none;
        blendMode = BlendMode.over;
        uniform.reset();
        stencilStrokesUniform.reset();
        triangleVerticesOffset = -1;
        CanvasUtils.resetArray(triangleVertices);
        vertexMode = VertexMode.triangles;
        CanvasUtils.resetArray(components);
        clips.clear();
    }

    void free(){
        Pools.free(this);
    }

    enum CallType{
        none, fill, convexFill, stroke, triangles
    }
}
