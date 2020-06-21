package arc.graphics.gl;

import arc.graphics.*;
import arc.graphics.Pixmap.*;
import arc.graphics.Texture.*;
import arc.graphics.g2d.*;
import arc.util.*;

/**
 * <p>
 * Encapsulates OpenGL ES 2.0 frame buffer objects. This is a simple helper class which should cover most FBO uses. It will
 * automatically create a texture for the color attachment and a renderbuffer for the depth buffer. You can get a hold of the
 * texture by {@link FrameBuffer#getTexture()}. This class will only work with OpenGL ES 2.0.
 * </p>
 *
 * <p>
 * FrameBuffers are managed. In case of an OpenGL context loss, which only happens on Android when a user switches to another
 * application or receives an incoming call, the framebuffer will be automatically recreated.
 * </p>
 *
 * <p>
 * A FrameBuffer must be disposed if it is no longer needed
 * </p>
 * @author mzechner, realitix
 */
public class FrameBuffer extends GLFrameBuffer<Texture>{
    private Format format;

    /**
     * Creates a GLFrameBuffer from the specifications provided by bufferBuilder
     **/
    protected FrameBuffer(GLFrameBufferBuilder<? extends GLFrameBuffer<Texture>> bufferBuilder){
        super(bufferBuilder);
    }

    /** Creates a new 2x2 buffer. Resize before use. */
    public FrameBuffer(){
        this(2, 2);
    }

    /** Creates a new FrameBuffer having the given dimensions in the format RGBA8888 and no depth buffer.*/
    public FrameBuffer(int width, int height){
        this(Format.rgba8888, width, height, false, false);
    }

    /** Creates a new FrameBuffer having the given dimensions and no depth buffer. */
    public FrameBuffer(Pixmap.Format format, int width, int height){
        this(format, width, height, false, false);
    }

    /** Creates a new FrameBuffer having the given dimensions and potentially a depth buffer attached. */
    public FrameBuffer(Pixmap.Format format, int width, int height, boolean hasDepth){
        this(format, width, height, hasDepth, false);
    }

    /** Creates a new FrameBuffer having the given dimensions and potentially a depth buffer attached. */
    public FrameBuffer(int width, int height, boolean hasDepth){
        this(Format.rgba8888, width, height, hasDepth, false);
    }

    /**
     * Creates a new FrameBuffer having the given dimensions and potentially a depth and a stencil buffer attached.
     * @param format the format of the color buffer; according to the OpenGL ES 2.0 spec, only RGB565, RGBA4444 and RGB5_A1 are
     * color-renderable
     * @param width the width of the framebuffer in pixels
     * @param height the height of the framebuffer in pixels
     * @param hasDepth whether to attach a depth buffer
     * @throws ArcRuntimeException in case the FrameBuffer could not be created
     */
    public FrameBuffer(Pixmap.Format format, int width, int height, boolean hasDepth, boolean hasStencil){
        create(format, width, height, hasDepth, hasStencil);
    }

    protected void create(Pixmap.Format format, int width, int height, boolean hasDepth, boolean hasStencil){
        width = Math.max(width, 2);
        height = Math.max(height, 2);
        this.format = format;
        FrameBufferBuilder frameBufferBuilder = new FrameBufferBuilder(width, height);
        frameBufferBuilder.addBasicColorTextureAttachment(format);
        if(hasDepth) frameBufferBuilder.addBasicDepthRenderBuffer();
        if(hasStencil) frameBufferBuilder.addBasicStencilRenderBuffer();
        this.bufferBuilder = frameBufferBuilder;
        build();
    }

    /** Blits this buffer onto the screen using the specified shader. */
    public void blit(Shader shader){
        Draw.blit(this, shader);
    }

    /** Note that this does nothing if the width and height are correct. */
    public void resize(int width, int height){
        //prevent incomplete attachment issues.
        width = Math.max(width, 2);
        height = Math.max(height, 2);

        //ignore pointless resizing
        if(width == getWidth() && height == getHeight()) return;

        TextureFilter min = getTexture().getMinFilter(), mag = getTexture().getMagFilter();
        boolean hasDepth = depthbufferHandle != 0, hasStencil = stencilbufferHandle != 0;
        dispose();

        FrameBufferBuilder frameBufferBuilder = new FrameBufferBuilder(width, height);
        frameBufferBuilder.addBasicColorTextureAttachment(format);
        if(hasDepth) frameBufferBuilder.addBasicDepthRenderBuffer();
        if(hasStencil) frameBufferBuilder.addBasicStencilRenderBuffer();
        this.bufferBuilder = frameBufferBuilder;
        this.textureAttachments.clear();
        this.framebufferHandle = 0;
        this.depthbufferHandle = 0;
        this.stencilbufferHandle = 0;
        this.depthStencilPackedBufferHandle = 0;
        this.hasDepthStencilPackedBuffer = this.isMRT = false;
        build();
        getTexture().setFilter(min, mag);
    }

    /** See {@link GLFrameBuffer#unbind()} */
    public static void unbind(){
        GLFrameBuffer.unbind();
    }

    @Override
    protected Texture createTexture(FrameBufferTextureAttachmentSpec attachmentSpec){
        GLOnlyTextureData data = new GLOnlyTextureData(bufferBuilder.width, bufferBuilder.height, 0, attachmentSpec.internalFormat, attachmentSpec.format, attachmentSpec.type);
        Texture result = new Texture(data);
        result.setFilter(TextureFilter.linear, TextureFilter.linear);
        result.setWrap(TextureWrap.clampToEdge, TextureWrap.clampToEdge);
        return result;
    }

    @Override
    protected void disposeColorTexture(Texture colorTexture){
        colorTexture.dispose();
    }

    @Override
    protected void attachFrameBufferColorTexture(Texture texture){
        Gl.framebufferTexture2D(Gl.framebuffer, GL20.GL_COLOR_ATTACHMENT0, GL20.GL_TEXTURE_2D, texture.getTextureObjectHandle(), 0);
    }
}
