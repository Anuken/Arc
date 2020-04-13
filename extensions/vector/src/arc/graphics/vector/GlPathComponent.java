package arc.graphics.vector;

import arc.struct.*;
import arc.util.pooling.Pool.*;
import arc.util.pooling.*;

public class GlPathComponent implements Poolable{
    final Array<Vertex> triangleFanVertices = new Array<>();
    final Array<Vertex> triangleStripVertices = new Array<>();
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
