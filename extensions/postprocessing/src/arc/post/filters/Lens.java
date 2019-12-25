package arc.post.filters;

import arc.graphics.Texture;
import arc.math.geom.Vector2;
import arc.post.PostFilter;

public final class Lens extends PostFilter{
    public Vector2 viewportInverse;
    public int ghosts;
    public float haloWidth;
    public Texture lensColorTexture;

    public Lens(int width, int height){
        super("lensflare2");
        viewportInverse = new Vector2(1f / width, 1f / height);
    }

    public void setViewportSize(float width, float height){
        this.viewportInverse.set(1f / width, 1f / height);
    }

    @Override
    protected void update(){
        shader.setUniformf("u_texture1", u_texture1);
        shader.setUniformf("u_viewportInverse", viewportInverse);
        shader.setUniformf("u_ghosts", ghosts);
        shader.setUniformf("u_haloWidth", haloWidth);
    }

    @Override
    protected void onBeforeRender(){
        inputTexture.bind(u_texture0);
        lensColorTexture.bind(u_texture1);
    }
}
