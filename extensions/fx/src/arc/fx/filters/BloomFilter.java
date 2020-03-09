package arc.fx.filters;

import arc.fx.*;
import arc.fx.util.*;
import arc.graphics.Blending;
import arc.graphics.*;
import arc.graphics.Pixmap.*;
import arc.graphics.gl.*;

public class BloomFilter extends FxFilter{
    public final PingPongBuffer buffer;

    public final GaussianBlurFilter blur;
    public final ThresholdFilter threshold;
    public final CombineFilter combine;

    public Blending blending = Blending.normal;
    public int scaling = 4;

    public BloomFilter(){
        buffer = new PingPongBuffer(Format.RGBA8888);

        blur = new GaussianBlurFilter();
        threshold = new ThresholdFilter();
        combine = new CombineFilter();

        blur.setPasses(2);
        blur.setAmount(10f);
        threshold.gamma = 0f;
        combine.src1int = 1f;
        combine.src1sat = 1f;
        combine.src2int = 2f;
        combine.src2sat = 2f;
        rebind();
    }

    @Override
    public void rebind(){
        threshold.rebind();
        combine.rebind();
        buffer.rebind();
    }

    @Override
    public void resize(int width, int height){
        width /= scaling;
        height /= scaling;

        buffer.resize(width, height);
        blur.resize(width, height);
        threshold.resize(width, height);
        combine.resize(width, height);
    }

    @Override
    public void dispose(){
        combine.dispose();
        threshold.dispose();
        blur.dispose();
        buffer.dispose();
    }

    @Override
    public void render(ScreenQuad mesh, final FrameBuffer src, final FrameBuffer dst){
        Texture texSrc = src.getTexture();

        Gl.disable(Gl.blend);

        buffer.begin();

        // Threshold / high-pass filter
        // Only areas with pixels >= threshold are blit to smaller FBO
        threshold.setInput(texSrc).setOutput(buffer.getDstBuffer()).render(mesh);
        buffer.swap();

        // Blur pass
        blur.render(mesh, buffer);

        buffer.end();

        if(blending != Blending.disabled){
            Gl.enable(Gl.blend);
            Gl.blendFunc(blending.src, blending.dst);
        }

        // Mix original scene and blurred threshold, modulate via set(Base|BloomEffect)(Saturation|Intensity)
        combine.setInput(texSrc, buffer.getDstTexture())
        .setOutput(dst)
        .render(mesh);
    }
}
