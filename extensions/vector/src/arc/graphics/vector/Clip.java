package arc.graphics.vector;

import arc.util.pooling.Pool.*;
import arc.util.pooling.*;

class Clip implements Poolable{
    GlCall call;
    ClipOperation clipOperation = ClipOperation.union;

    static Clip obtain(GlCall call, ClipOperation clipOperation){
        Clip clip = Pools.obtain(Clip.class, Clip::new);
        clip.call = call;
        clip.clipOperation = clipOperation;
        return clip;
    }

    @Override
    public void reset(){
        call = null;
        clipOperation = ClipOperation.union;
    }

    void free(){
        Pools.free(this);
    }
}
