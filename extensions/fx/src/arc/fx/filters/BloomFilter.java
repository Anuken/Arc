package arc.fx.filters;

import arc.fx.*;
import arc.fx.util.*;
import arc.graphics.*;
import arc.graphics.Pixmap.*;

public final class BloomFilter extends FxFilter{
    public final PingPongBuffer pingPongBuffer;

    public final GaussianBlurFilter blur;
    public final ThresholdFilter threshold;
    public final CombineFilter combine;

    public boolean blending = false;
    public int sfactor, dfactor;

    public BloomFilter(){
        pingPongBuffer = new PingPongBuffer(Format.RGBA8888);

        blur = new GaussianBlurFilter();
        threshold = new ThresholdFilter();
        combine = new CombineFilter();

        blur.setPasses(10);
        threshold.gamma = 0.85f;
        combine.src1int = 1f;
        combine.src1sat = 0.85f;
        combine.src2int = 1.f;
        combine.src2sat = 0.85f;
    }

    @Override
    public void rebind(){
        threshold.rebind();
        combine.rebind();
        pingPongBuffer.rebind();
    }

    @Override
    public void resize(int width, int height){
        pingPongBuffer.resize(width, height);

        blur.resize(width, height);
        threshold.resize(width, height);
        combine.resize(width, height);
    }

    @Override
    public void dispose(){
        combine.dispose();
        threshold.dispose();
        blur.dispose();
        pingPongBuffer.dispose();
    }

    @Override
    public void render(ScreenQuad mesh, final FxBuffer src, final FxBuffer dst){
        Texture texSrc = src.getFbo().getTexture();

        Gl.disable(Gl.blend);

        pingPongBuffer.begin();

        // Threshold / high-pass filter
        // Only areas with pixels >= threshold are blit to smaller FBO
        threshold.setInput(texSrc).setOutput(pingPongBuffer.getDstBuffer()).render(mesh);
        pingPongBuffer.swap();

        // Blur pass
        blur.render(mesh, pingPongBuffer);

        pingPongBuffer.end();

        if(blending){
            Gl.enable(Gl.blend);
        }

        if(blending){
            Gl.blendFunc(sfactor, dfactor);
        }

        // Mix original scene and blurred threshold, modulate via set(Base|BloomEffect)(Saturation|Intensity)
        combine.setInput(texSrc, pingPongBuffer.getDstTexture())
        .setOutput(dst)
        .render(mesh);
    }
}
