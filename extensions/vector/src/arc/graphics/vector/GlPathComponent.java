package arc.graphics.vector;

import arc.struct.*;
import arc.util.pooling.Pool.*;
import arc.util.pooling.*;

public class GlPathComponent implements Poolable{
    final Seq<Vertex> triangleFanVertices = new Seq<>();
    final Seq<Vertex> triangleStripVertices = new Seq<>();
    int triangleFanVerticesOffset = -1;
    int triangleStripVerticesOffset = -1;

    static GlPathComponent obtain(){
        return Pools.obtain(GlPathComponent.class, GlPathComponent::new);
    }

    @Override
    public void reset(){
        triangleFanVerticesOffset = -1;
        CanvasUtils.resetArray(triangleFanVertices);
        triangleStripVerticesOffset = -1;
        CanvasUtils.resetArray(triangleStripVertices);
    }
}
