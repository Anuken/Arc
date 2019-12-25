package arc.graphics;

/** Specific blending modes. */
public enum Blending{
    normal(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA),
    additive(GL20.GL_SRC_ALPHA, GL20.GL_ONE),
    disabled(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

    public final int src, dst;

    Blending(int src, int dst){
        this.src = src;
        this.dst = dst;
    }
}
