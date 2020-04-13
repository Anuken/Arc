package arc.graphics.vector;

import arc.graphics.vector.GlCall.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.pooling.Pool.*;
import arc.util.pooling.*;

class TrianglesMesh implements Poolable{
    final Array<Vertex> triangleVertices = new Array<>();
    Canvas canvas;

    static TrianglesMesh obtain(Canvas canvas){
        TrianglesMesh trianglesMesh = Pools.obtain(TrianglesMesh.class, TrianglesMesh::new);
        trianglesMesh.canvas = canvas;
        return trianglesMesh;
    }

    private void newVertex(float x, float y, float u, float v){
        triangleVertices.add(Vertex.obtain(x, y, u, v, canvas.currentState.xform));
    }

    public GlCall createTexturedCall(VertexMode mode, float... vertices){
        addTexturedVertices(vertices);
        return createCall(mode, true);
    }

    private void addTexturedVertices(float... vertices){
        if(vertices.length < 12){
            return;
        }

        int i = 0;
        while(i <= vertices.length - 5){
            newVertex(vertices[i++], vertices[i++], vertices[i++], vertices[i++]);
        }
    }

    public GlCall createTexturedCall(VertexMode mode, FloatArray vertices){
        addTexturedVertices(vertices);
        return createCall(mode, true);
    }

    private void addTexturedVertices(FloatArray vertices){
        if(vertices.size < 12){
            return;
        }

        int i = 0;
        while(i <= vertices.size - 5){
            newVertex(vertices.get(i++), vertices.get(i++), vertices.get(i++), vertices.get(i++));
        }
    }

    public GlCall createTexturedCall(VertexMode mode, Vec2... vertices){
        addTexturedVertices(vertices);
        return createCall(mode, true);
    }

    private void addTexturedVertices(Vec2... vertices){
        if(vertices.length < 6){
            return;
        }

        int i = 0;
        while(i <= vertices.length - 3){
            Vec2 xy = vertices[i++];
            Vec2 uv = vertices[i++];
            newVertex(xy.x, xy.y, uv.x, uv.y);
        }
    }

    public GlCall createTexturedCall(VertexMode mode, Vertex... vertices){
        addTexturedVertices(vertices);
        return createCall(mode, true);
    }

    private void addTexturedVertices(Vertex... vertices){
        if(vertices.length < 3){
            return;
        }

        for(int i = 0; i < vertices.length - 1; i++){
            Vertex vertex = vertices[i];
            newVertex(vertex.x, vertex.y, vertex.u, vertex.v);
        }
    }

    GlCall createCall(VertexMode mode, boolean textured, Array<Vertex> vertices){
        triangleVertices.addAll(vertices);
        return createCall(mode, textured);
    }

    public GlCall createCall(VertexMode mode, float... vertices){
        addVertices(vertices);
        return createCall(mode, false);
    }

    private void addVertices(float... vertices){
        if(vertices.length < 6){
            return;
        }

        int i = 0;
        while(i <= vertices.length - 3){
            newVertex(vertices[i++], vertices[i++], 0.5f, 1);
        }
    }

    public GlCall createCall(VertexMode mode, FloatArray vertices){
        addVertices(vertices);
        return createCall(mode, false);
    }

    private void addVertices(FloatArray vertices){
        if(vertices.size < 6){
            return;
        }

        int i = 0;
        while(i <= vertices.size - 3){
            newVertex(vertices.get(i++), vertices.get(i++), 0.5f, 1);
        }
    }

    public GlCall createCall(VertexMode mode, Vec2... vertices){
        addVertices(vertices);
        return createCall(mode, false);
    }

    private void addVertices(Vec2... vertices){
        if(vertices.length < 3){
            return;
        }

        for(Vec2 point : vertices){
            newVertex(point.x, point.y, 0.5f, 1);
        }
    }

    private GlCall createCall(VertexMode mode, boolean textured){
        CanvasState currentState = canvas.currentState;
        GlCall call = GlCall.obtain();
        call.callType = CallType.triangles;
        call.blendMode = currentState.blendMode;
        call.clips.addAll(currentState.clips);
        call.vertexMode = mode;
        call.initUniform(currentState.xform, currentState.globalAlpha, currentState.scissor, currentState.fillPaint, canvas.fringeWidth, canvas.fringeWidth, -1.0f, textured);
        call.triangleVertices.addAll(triangleVertices);
        triangleVertices.clear();
        return call;
    }

    @Override
    public void reset(){
        canvas = null;
    }

    public void free(){
        Pools.free(this);
    }
}
