package io.anuke.arc.postprocessing.effects;

import io.anuke.arc.Core;
import io.anuke.arc.graphics.*;
import io.anuke.arc.graphics.glutils.FrameBuffer;
import io.anuke.arc.postprocessing.PostEffect;
import io.anuke.arc.postprocessing.PostProcessor;
import io.anuke.arc.postprocessing.filters.*;
import io.anuke.arc.postprocessing.filters.Blur.BlurType;
import io.anuke.arc.postprocessing.utils.PingPongBuffer;

public final class Bloom extends PostEffect{
    private PingPongBuffer pingPongBuffer;

    public Blur blur;
    public Threshold threshold;
    public Combine combine;
    public Blending blending = Blending.disabled;

    public Bloom(int fboWidth, int fboHeight){
        pingPongBuffer = PostProcessor.newPingPongBuffer(fboWidth, fboHeight);

        blur = new Blur(fboWidth, fboHeight);
        threshold = new Threshold();
        combine = new Combine();

        blur.type = BlurType.Gaussian5x5b;
        blur.passes = 2;
        threshold.threshold = 0.277f;
        combine.baseIntensity = 1f;
        combine.baseSaturation = 0.85f;
        combine.effectIntensity = 1.1f;
        combine.effectSaturation = 0.85f;
    }

    @Override
    public void dispose(){
        combine.dispose();
        threshold.dispose();
        blur.dispose();
        pingPongBuffer.dispose();
    }

    @Override
    public void render(final FrameBuffer src, final FrameBuffer dest){
        Texture texsrc = src.getTexture();

        Core.gl.glDisable(GL20.GL_BLEND);

        pingPongBuffer.begin();
        {
            // threshold / high-pass filter
            // only areas with pixels >= threshold are blit to smaller fbo
            threshold.setInput(texsrc).setOutput(pingPongBuffer.getSourceBuffer()).render();

            // blur pass
            blur.render(pingPongBuffer);
        }
        pingPongBuffer.end();

        if(blending != Blending.disabled){
            Core.gl.glEnable(GL20.GL_BLEND);
            Core.gl.glBlendFunc(blending.src, blending.dst);
        }

        // mix original scene and blurred threshold, modulate via
        // set(Base|Bloom)(Saturation|Intensity)
        combine.setOutput(dest);
        combine.setInput(texsrc, pingPongBuffer.getResultTexture()).render();
    }

    @Override
    public void rebind(){
        blur.rebind();
        threshold.rebind();
        combine.rebind();
        pingPongBuffer.rebind();
    }
}
