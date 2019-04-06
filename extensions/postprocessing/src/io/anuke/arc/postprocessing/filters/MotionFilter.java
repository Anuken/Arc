package io.anuke.arc.postprocessing.filters;

import io.anuke.arc.graphics.Pixmap.Format;
import io.anuke.arc.graphics.glutils.FrameBuffer;
import io.anuke.arc.postprocessing.PostFilter;

public class MotionFilter extends PostFilter{
    public float blurOpacity = 0.5f;

    private Copy copyFilter = new Copy();
    private FrameBuffer fbo;

    public MotionFilter(){
        super("motionblur");
    }

    @Override
    public void resize(int width, int height){
        if(fbo != null){
            fbo.dispose();
            fbo = null;
        }
    }

    @Override
    protected void update(){
        if(fbo != null) shader.setUniformi("u_texture1", u_texture1);
        shader.setUniformf("u_blurOpacity", this.blurOpacity);
    }

    @Override
    protected void onBeforeRender(){
        super.onBeforeRender();
        if(fbo != null) fbo.getTexture().bind(u_texture1);
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
            }
            setInput(src).setOutput(fbo).render();

            // Copy fbo to screen
            copyFilter.setInput(fbo).setOutput(dest).render();
        }
    }

}
