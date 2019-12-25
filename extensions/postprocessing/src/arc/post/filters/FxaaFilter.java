package arc.post.filters;

import arc.math.geom.Vec2;
import arc.post.PostFilter;

/**
 * Fast approximate anti-aliasing filter.
 * @author Toni Sagrista
 */
public final class FxaaFilter extends PostFilter{
    public Vec2 viewportInverse;
    public float reduceMin;
    public float reduceMul;
    public float spanMax;

    public FxaaFilter(int viewportWidth, int viewportHeight){
        this(new Vec2(viewportWidth, viewportHeight), 1f / 128f, 1f / 8f, 8f);
    }

    public FxaaFilter(int viewportWidth, int viewportHeight, float fxaa_reduce_min, float fxaa_reduce_mul, float fxaa_span_max){
        this(new Vec2(viewportWidth, viewportHeight), fxaa_reduce_min, fxaa_reduce_mul, fxaa_span_max);
    }

    public FxaaFilter(Vec2 viewportSize, float fxaa_reduce_min, float fxaa_reduce_mul, float fxaa_span_max){
        super("fxaa");
        this.viewportInverse = viewportSize;
        this.viewportInverse.x = 1f / this.viewportInverse.x;
        this.viewportInverse.y = 1f / this.viewportInverse.y;

        this.reduceMin = fxaa_reduce_min;
        this.reduceMul = fxaa_reduce_mul;
        this.spanMax = fxaa_span_max;
    }

    public void setViewportSize(float width, float height){
        this.viewportInverse.set(1f / width, 1f / height);
    }

    @Override
    protected void update(){
        shader.setUniformf("u_viewportInverse", viewportInverse);
        shader.setUniformf("FXAA_REDUCE_MIN", reduceMin);
        shader.setUniformf("FXAA_REDUCE_MUL", reduceMul);
        shader.setUniformf("FXAA_SPAN_MAX", spanMax);
    }
}
