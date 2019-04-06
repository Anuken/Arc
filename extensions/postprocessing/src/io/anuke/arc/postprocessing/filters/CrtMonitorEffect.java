package io.anuke.arc.postprocessing.filters;

import io.anuke.arc.Core;
import io.anuke.arc.graphics.*;
import io.anuke.arc.graphics.glutils.FrameBuffer;
import io.anuke.arc.postprocessing.PostEffect;
import io.anuke.arc.postprocessing.PostProcessor;
import io.anuke.arc.postprocessing.filters.Blur.BlurType;
import io.anuke.arc.postprocessing.filters.CrtScreen.RgbMode;
import io.anuke.arc.postprocessing.utils.PingPongBuffer;

public final class CrtMonitorEffect extends PostEffect{
    private PingPongBuffer pingPongBuffer = null;
    private FrameBuffer buffer = null;
    private CrtScreen crt;
    private Blur blur;
    private Combine combine;
    private boolean doblur;

    public Blending blending = Blending.disabled;

    // the effect is designed to work on the whole screen area, no small/mid size tricks!
    public CrtMonitorEffect(int fboWidth, int fboHeight, boolean barrelDistortion, boolean performBlur, RgbMode mode, int effectsSupport){
        doblur = performBlur;

        if(doblur){
            pingPongBuffer = PostProcessor.newPingPongBuffer(fboWidth, fboHeight);
            blur = new Blur(fboWidth, fboHeight);
            blur.passes = 1;
            blur.amount = 2f;
            blur.type = BlurType.Gaussian3x3;
        }else{
            buffer = new FrameBuffer(fboWidth, fboHeight);
        }

        combine = new Combine();
        crt = new CrtScreen(barrelDistortion, mode, effectsSupport);
    }

    @Override
    public void dispose(){
        crt.dispose();
        combine.dispose();
        if(doblur){
            blur.dispose();
        }

        if(buffer != null){
            buffer.dispose();
        }

        if(pingPongBuffer != null){
            pingPongBuffer.dispose();
        }
    }

    @Override
    public void rebind(){
        crt.rebind();
    }

    @Override
    public void render(FrameBuffer src, FrameBuffer dest){
        // the original scene
        Texture in = src.getTexture();

        Core.gl.glDisable(GL20.GL_BLEND);

        Texture out;

        if(doblur){

            pingPongBuffer.begin();
            {
                // crt pass
                crt.setInput(in).setOutput(pingPongBuffer.getSourceBuffer()).render();

                // blur pass
                blur.render(pingPongBuffer);
            }
            pingPongBuffer.end();

            out = pingPongBuffer.getResultTexture();
        }else{
            // crt pass
            crt.setInput(in).setOutput(buffer).render();

            out = buffer.getTexture();
        }

        if(blending != Blending.disabled){
            Core.gl.glEnable(GL20.GL_BLEND);
            Core.gl.glBlendFunc(blending.src, blending.dst);
        }

        // do combine pass
        combine.setOutput(dest);
        combine.setInput(in, out).render();
    }

}
