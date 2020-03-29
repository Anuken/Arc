package arc.graphics;

/** Specific blending modes. */
public enum Blending{
    normal(Gl.srcAlpha, Gl.oneMinusSrcAlpha),
    additive(Gl.srcAlpha, Gl.one),
    disabled(Gl.srcAlpha, Gl.oneMinusSrcAlpha);

    public final int src, dst;

    Blending(int src, int dst){
        this.src = src;
        this.dst = dst;
    }

    public void apply(){
        Gl.blendFunc(src, dst);
    }
}
