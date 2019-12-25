package arc.post.filters;

import arc.graphics.Pixmap.Format;
import arc.graphics.Texture;
import arc.graphics.gl.FrameBuffer;
import arc.post.PostFilter;

public class MotionFilter extends PostFilter{
    public float blurOpacity = 0.5f;

    private Copy copyFilter = new Copy();
    private Texture lastFrameTex;
    private FrameBuffer fbo;

    public MotionFilter(){
        super("motionblur");
    }

    @Override
    public void resize(int width, int height){
        lastFrameTex = null;
    }

    @Override
    protected void update(){
        shader.setUniformi("u_texture1", u_texture1);
        shader.setUniformf("u_blurOpacity", this.blurOpacity);
    }

    @Override
    protected void onBeforeRender(){
        super.onBeforeRender();
        if(lastFrameTex != null) lastFrameTex.bind(u_texture1);
    }

    @Override
    public void render(FrameBuffer src, FrameBuffer dest){

        if(dest != null){
            setInput(src).setOutput(dest).render();
            fbo = dest;
        }else{
            if(fbo == null){
                // Init frame buffer
                fbo = new FrameBuffer(Format.RGBA8888, src.getWidth(), src.getHeight(), false);
                fbo.getTexture().setFilter(src.getTexture().getMinFilter());
            }
            setInput(src).setOutput(fbo).render();

            // Copy fbo to screen
            copyFilter.setInput(fbo).setOutput(dest).render();
        }

        lastFrameTex = fbo.getTexture();
    }

}
