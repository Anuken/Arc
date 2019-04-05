package io.anuke.arc.postprocessing.filters;

import io.anuke.arc.math.geom.Vector2;
import io.anuke.arc.postprocessing.PostFilter;

/**
 * Normal filtered anti-aliasing filter.
 * @author Toni Sagrista
 */
public final class NfaaFilter extends PostFilter{
    public Vector2 viewportInverse;

    public NfaaFilter(int viewportWidth, int viewportHeight){
        this(new Vector2(viewportWidth, viewportHeight));
    }

    public NfaaFilter(Vector2 viewportSize){
        super("nfaa");
        this.viewportInverse = viewportSize;
        this.viewportInverse.x = 1f / this.viewportInverse.x;
        this.viewportInverse.y = 1f / this.viewportInverse.y;
    }

    public void setViewportSize(float width, float height){
        this.viewportInverse.set(1f / width, 1f / height);
    }

    @Override
    protected void update(){
        shader.setUniformf("u_viewportInverse", viewportInverse);
    }
}
