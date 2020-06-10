package arc.graphics;

/** Specific blending modes. */
public enum Blending{
    normal(Gl.srcAlpha, Gl.oneMinusSrcAlpha),
    additive(Gl.srcAlpha, Gl.one),
    disabled(Gl.srcAlpha, Gl.oneMinusSrcAlpha){

        @Override
        public void apply(){
            Gl.disable(Gl.blend);
        }
    };

    public final int src, dst;

    Blending(int src, int dst){
        this.src = src;
        this.dst = dst;
    }

    /** Enables/disables blending and sets the correct GL blend function. */
    public void apply(){
        Gl.enable(Gl.blend);
        Gl.blendFunc(src, dst);
    }
}
