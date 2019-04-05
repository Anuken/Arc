package io.anuke.arc.postprocessing.filters;

import io.anuke.arc.graphics.Pixmap.Format;
import io.anuke.arc.graphics.Texture;
import io.anuke.arc.graphics.glutils.FrameBuffer;
import io.anuke.arc.postprocessing.PostFilter;

public class MotionFilter extends PostFilter{
    public float blurOpacity = 0.5f;
    public Texture lastFrameTex;

    private Copy copyFilter = new Copy();
    private FrameBuffer fbo;

    public MotionFilter(){
        super("motionblur");
    }

    @Override
    protected void update(){
        if(lastFrameTex != null) shader.setUniformi("u_texture1", u_texture1);
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
            }
            setInput(src).setOutput(fbo).render();

            // Copy fbo to screen
            copyFilter.setInput(fbo).setOutput(dest).render();
        }

        // Set last frame
        lastFrameTex = fbo.getTexture();
    }

}
