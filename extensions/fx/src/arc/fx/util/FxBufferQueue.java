package arc.fx.util;

import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.graphics.gl.*;
import arc.struct.*;
import arc.util.*;

/**
 * Provides looped access to an array of {@link FxBuffer}.
 */
public class FxBufferQueue implements Disposable{
    private final Array<FrameBuffer> buffers;
    private int currentIdx = 0;

    private TextureWrap wrapU = TextureWrap.ClampToEdge;
    private TextureWrap wrapV = TextureWrap.ClampToEdge;
    private TextureFilter filterMin = TextureFilter.Nearest;
    private TextureFilter filterMag = TextureFilter.Nearest;

    public FxBufferQueue(Pixmap.Format pixelFormat, int fboAmount){
        if(fboAmount < 1){
            throw new IllegalArgumentException("FBO amount should be a positive number.");
        }
        buffers = new Array<>(true, fboAmount);
        for(int i = 0; i < fboAmount; i++){
            buffers.add(new FrameBuffer(pixelFormat, 4, 4));
        }
    }

    @Override
    public void dispose(){
        for(int i = 0; i < buffers.size; i++){
            buffers.get(i).dispose();
        }
    }

    public void resize(int width, int height){
        for(int i = 0; i < buffers.size; i++){
            buffers.get(i).resize(width, height);
        }
    }

    /**
     * Restores buffer OpenGL parameters. Could be useful in case of OpenGL context loss.
     */
    public void rebind(){
        for(int i = 0; i < buffers.size; i++){
            FrameBuffer wrapper = buffers.get(i);
            // FBOs might be null if the instance wasn't initialized with #resize(int, int) yet.
            if(wrapper == null) continue;

            Texture texture = wrapper.getTexture();
            texture.setWrap(wrapU, wrapV);
            texture.setFilter(filterMin, filterMag);
        }
    }

    public FrameBuffer getCurrent(){
        return buffers.get(currentIdx);
    }

    public FrameBuffer changeToNext(){
        currentIdx = (currentIdx + 1) % buffers.size;
        return getCurrent();
    }

    public void setTextureParams(TextureWrap u, TextureWrap v, TextureFilter min, TextureFilter mag){
        wrapU = u;
        wrapV = v;
        filterMin = min;
        filterMag = mag;
        rebind();
    }
}
