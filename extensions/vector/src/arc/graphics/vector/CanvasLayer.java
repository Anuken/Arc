package arc.graphics.vector;

import arc.math.geom.*;
import arc.util.pooling.Pool.*;
import arc.util.pooling.*;

class CanvasLayer implements Poolable{
    final Rect bounds = new Rect();
    Effect effect;//TODO apply effect

    public static CanvasLayer obtain(){
        return Pools.obtain(CanvasLayer.class, CanvasLayer::new);
    }

    @Override
    public void reset(){
        effect = null;
        bounds.set(0, 0, 0, 0);
    }

    public void free(){
        Pools.free(this);
    }
}
