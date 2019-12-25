package arc.post.filters;

import arc.*;
import arc.graphics.*;
import arc.graphics.gl.FrameBuffer;
import arc.post.PostEffect;
import arc.post.PostProcessor;
import arc.post.filters.Blur.BlurType;
import arc.post.PingPongBuffer;

/**
 * Pseudo lens flare implementation. This is a post-processing effect entirely, no need for light positions or anything. It
 * includes ghost generation, halos, chromatic distortion and blur.
 * @author Toni Sagrista
 */
public final class LensFlareEffect extends PostEffect{
    private PingPongBuffer pingPongBuffer;

    public Lens lens;
    public Blur blur;
    public Bias bias;
    public Combine combine;
    public Blending blending = Blending.disabled;

    public LensFlareEffect(int fboWidth, int fboHeight){
        pingPongBuffer = PostProcessor.newPingPongBuffer(fboWidth, fboHeight);

        lens = new Lens(fboWidth, fboHeight);
        blur = new Blur(fboWidth, fboHeight);
        bias = new Bias();
        combine = new Combine();

        blur.type = BlurType.Gaussian5x5b;
        blur.passes = 2;
        bias.bias = -0.9f;

        combine.baseIntensity = 1f;
        combine.baseSaturation = 1f;

        combine.effectIntensity = 0.7f;
        combine.effectSaturation = 1f;
        lens.ghosts = 8;
        lens.haloWidth = 0.5f;
    }

    @Override
    public void dispose(){
        combine.dispose();
        bias.dispose();
        blur.dispose();
        pingPongBuffer.dispose();
    }

    @Override
    public void render(final FrameBuffer src, final FrameBuffer dest){
        Texture texsrc = src.getTexture();
        Core.gl.glDisable(GL20.GL_BLEND);

        pingPongBuffer.begin();
        {
            // apply bias
            bias.setInput(texsrc).setOutput(pingPongBuffer.getSourceBuffer()).render();

            lens.setInput(pingPongBuffer.getSourceBuffer()).setOutput(pingPongBuffer.getResultBuffer()).render();

            pingPongBuffer.set(pingPongBuffer.getResultBuffer(), pingPongBuffer.getSourceBuffer());

            // blur pass
            blur.render(pingPongBuffer);
        }
        pingPongBuffer.end();

        if(blending != Blending.disabled){
            Core.gl.glEnable(GL20.GL_BLEND);
            Core.gl.glBlendFunc(blending.src, blending.dst);
        }

        // mix original scene and blurred threshold, modulate via
        combine.setOutput(dest);
        combine.setInput(texsrc, pingPongBuffer.getResultTexture()).render();
    }

    @Override
    public void rebind(){
        blur.rebind();
        bias.rebind();
        combine.rebind();
        pingPongBuffer.rebind();
    }
}
