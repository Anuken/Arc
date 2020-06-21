package arc.graphics.vector;

import arc.graphics.*;
import arc.graphics.Pixmap.*;
import arc.graphics.Texture.*;
import arc.util.*;
import arc.util.pooling.Pool.*;
import arc.util.pooling.*;

import java.nio.*;

import static arc.graphics.GL20.*;

class VFrameBuffer implements Poolable{
    private static int defaultFrameBufferObject = -1;

    int fbo;
    int rbo;
    Texture texture;
    private boolean ownsTexture;

    private IntBuffer handle = Buffers.newIntBuffer(16);

    private VFrameBuffer(){
    }

    static VFrameBuffer obtain(int width, int height, Format format){
        VFrameBuffer fb = Pools.obtain(VFrameBuffer.class, VFrameBuffer::new);
        if(fb.init(width, height, format)){
            fb.ownsTexture = true;
            return fb;
        }else{
            fb.free();
            throw new IllegalStateException("frame buffer couldn't be constructed.");
        }
    }

    static VFrameBuffer obtain(Texture texture){
        VFrameBuffer fb = Pools.obtain(VFrameBuffer.class, VFrameBuffer::new);
        if(fb.init(texture)){
            return fb;
        }else{
            fb.free();
            throw new IllegalStateException("frame buffer couldn't be constructed.");
        }
    }

    private boolean init(int width, int height, Format format){
        Texture texture = new Texture(width, height, format);
        texture.setFilter(TextureFilter.linear, TextureFilter.linear);
        texture.setWrap(TextureWrap.clampToEdge, TextureWrap.clampToEdge);
        ownsTexture = true;
        return init(texture);
    }

    private boolean init(Texture texture){
        this.texture = texture;

        handle.clear();
        Gl.getIntegerv(GL_FRAMEBUFFER_BINDING, handle);
        int defaultFBO = handle.get(0);

        handle.clear();
        Gl.getIntegerv(GL_RENDERBUFFER_BINDING, handle);
        int defaultRBO = handle.get(0);

        // frame buffer object
        fbo = Gl.genFramebuffer();
        Gl.bindFramebuffer(GL_FRAMEBUFFER, fbo);

        // render buffer object
        rbo = Gl.genRenderbuffer();
        Gl.bindRenderbuffer(GL_RENDERBUFFER, rbo);
        Gl.renderbufferStorage(GL_RENDERBUFFER, GL_STENCIL_INDEX8, texture.getWidth(), texture.getHeight());

        // combine all
        Gl.framebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture.getTextureObjectHandle(), 0);
        Gl.framebufferRenderbuffer(GL_FRAMEBUFFER, GL_STENCIL_ATTACHMENT, GL_RENDERBUFFER, rbo);

        Gl.bindFramebuffer(GL_FRAMEBUFFER, defaultFBO);
        Gl.bindRenderbuffer(GL_RENDERBUFFER, defaultRBO);

        return Gl.checkFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE;
    }

    public void bind(){
        if(defaultFrameBufferObject == -1){
            handle.clear();
            Gl.getIntegerv(GL_FRAMEBUFFER_BINDING, handle);
            defaultFrameBufferObject = handle.get(0);
        }

        Gl.bindFramebuffer(GL_FRAMEBUFFER, fbo);
    }

    public void unbind(){
        Gl.bindFramebuffer(GL_FRAMEBUFFER, defaultFrameBufferObject);
    }

    public Texture getTexture(){
        return texture;
    }

    @Override
    public void reset(){
        if(fbo != 0){
            Gl.deleteFramebuffer(fbo);
            fbo = 0;
        }

        if(rbo != 0){
            Gl.deleteRenderbuffer(rbo);
            rbo = 0;
        }

        if(texture != null){
            if(ownsTexture){
                texture.dispose();
            }
            texture = null;
        }

        ownsTexture = false;
    }

    public void free(){
        Pools.free(this);
    }
}
