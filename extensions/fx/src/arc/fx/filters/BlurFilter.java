package arc.fx.filters;

import arc.fx.*;
import arc.fx.util.*;
import arc.graphics.*;
import arc.graphics.gl.*;

public class BlurFilter extends FxFilter{
    public final GaussianBlurFilter blur;
    private final PingPongBuffer pingPongBuffer;
    private final CopyFilter copy;

    public Blending blending = Blending.disabled;

    // To keep track of the first render call.
    private boolean firstRender = true;

    public BlurFilter(){
        this(8, GaussianBlurFilter.BlurType.Gaussian5x5);
    }

    public BlurFilter(int blurPasses, GaussianBlurFilter.BlurType blurType){
        pingPongBuffer = new PingPongBuffer(Pixmap.Format.RGBA8888);

        copy = new CopyFilter();

        blur = new GaussianBlurFilter();
        blur.setPasses(blurPasses);
        blur.setType(blurType);
    }

    @Override
    public void dispose(){
        pingPongBuffer.dispose();
        blur.dispose();
        copy.dispose();
    }

    @Override
    public void resize(int width, int height){
        pingPongBuffer.resize(width, height);
        blur.resize(width, height);
        copy.resize(width, height);
    }

    @Override
    public void rebind(){
        pingPongBuffer.rebind();
        blur.setParams();
        copy.rebind();
    }

    @Override
    public void render(ScreenQuad mesh, FrameBuffer src, FrameBuffer dst){
        if(blur.getPasses() < 1){
            // Do not apply blur filter.
            copy.setInput(src).setOutput(dst).render(mesh);
            return;
        }

        Gl.disable(Gl.blend);

        pingPongBuffer.begin();
        copy.setInput(src).setOutput(pingPongBuffer.getDstBuffer()).render(mesh);
        pingPongBuffer.swap();
        // Blur filter performs multiple passes of mixing ping-pong buffers and expects src and dst to have valid data.
        // So for the first run we just make both src and dst buffers identical.
        if(firstRender){
            firstRender = false;
            copy.setInput(src).setOutput(pingPongBuffer.getDstBuffer()).render(mesh);
            pingPongBuffer.swap();
        }
        blur.render(mesh, pingPongBuffer);
        pingPongBuffer.end();

        if(blending != Blending.disabled){
            Gl.enable(Gl.blend);
            Gl.blendFunc(blending.src, blending.dst);
        }

        copy.setInput(pingPongBuffer.getDstTexture())
        .setOutput(dst)
        .render(mesh);
    }

}
