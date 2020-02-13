package arc.fx.filters;

import arc.fx.*;
import arc.fx.util.*;
import arc.graphics.*;

public class BlurFilter extends FxFilter{
    private final PingPongBuffer pingPongBuffer;
    private final CopyFilter copy;
    private final GaussianBlurFilter blur;

    private boolean blending = false;
    private int sfactor, dfactor;

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
    public void render(ScreenQuad mesh, FxBuffer src, FxBuffer dst){
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

        if(blending){
            Gl.enable(Gl.blend);
        }

        if(blending){
            // TODO support for Gl.BlendFuncSeparate(sfactor, dfactor, GL20.GL_ONE, GL20.GL_ONE );
            Gl.blendFunc(sfactor, dfactor);
        }

        copy.setInput(pingPongBuffer.getDstTexture())
        .setOutput(dst)
        .render(mesh);
    }

    public BlurFilter enableBlending(int sfactor, int dfactor){
        this.blending = true;
        this.sfactor = sfactor;
        this.dfactor = dfactor;
        return this;
    }

    public void disableBlending(){
        this.blending = false;
    }

    public BlurFilter setBlurPasses(int blurPasses){
        blur.setPasses(blurPasses);
        return this;
    }

    public int getBlurPasses(){
        return blur.getPasses();
    }
}
