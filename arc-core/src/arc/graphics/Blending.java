package arc.graphics;

/** Blending modes, can be instantiated to make custom blending. */
public class Blending{
    public static final Blending
    normal = new Blending(Gl.srcAlpha, Gl.oneMinusSrcAlpha),
    additive = new Blending(Gl.srcAlpha, Gl.one),
    disabled = new Blending(Gl.srcAlpha, Gl.oneMinusSrcAlpha){
        @Override
        public void apply(){
            Gl.disable(Gl.blend);
        }
    };

    public final int src, dst;

    public Blending(int src, int dst){
        this.src = src;
        this.dst = dst;
    }

    /** Enables/disables blending and sets the correct GL blend function. */
    public void apply(){
        Gl.enable(Gl.blend);
        Gl.blendFunc(src, dst);
    }
}
