package arc.graphics;

import arc.graphics.Pixmap.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.util.*;

/**
 * <p>
 * Encapsulates OpenGL ES 2.0 frame buffer objects. This is a simple helper class which should cover most FBO uses. It will
 * automatically create a texture for the color attachment and a renderbuffer for the depth buffer. You can get a hold of the
 * texture by {@link FrameBuffer#getTexture()}. This class will only work with OpenGL ES 2.0.
 * </p>
 *
 * <p>
 * A FrameBuffer must be disposed if it is no longer needed.
 * </p>
 * @author mzechner, realitix
 */
public class FrameBuffer extends GLFrameBuffer<Texture>{
    protected Format format;
    protected boolean hasDepth, hasStencil;
    protected @Nullable Texture depthTexture, stencilTexture;

    /**
     * Creates a GLFrameBuffer from the specifications provided by bufferBuilder
     **/
    public FrameBuffer(GLFrameBufferBuilder<? extends GLFrameBuffer<Texture>> bufferBuilder){
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

    public @Nullable Texture getDepthTexture(){
        return depthTexture;
    }

    public @Nullable Texture getStencilTexture(){
        return stencilTexture;
    }

    protected void create(Pixmap.Format format, int width, int height, boolean hasDepth, boolean hasStencil){
        this.hasDepth = hasDepth;
        this.hasStencil = hasStencil;
        width = Math.max(width, 2);
        height = Math.max(height, 2);
        this.format = format;
        FrameBufferBuilder frameBufferBuilder = new FrameBufferBuilder(width, height);
        frameBufferBuilder.addBasicColorTextureAttachment(format);
        if(hasDepth) frameBufferBuilder.addDepthTextureAttachment(Gl.depthComponent24, Gl.unsignedInt);
        if(hasStencil) frameBufferBuilder.addBasicStencilRenderBuffer();
        this.bufferBuilder = frameBufferBuilder;
        build();
    }

    /** Blits this buffer onto the screen using the specified shader. */
    public void blit(Shader shader){
        Draw.blit(this, shader);
    }

    public boolean resizeCheck(int width, int height){
        int lastWidth = getWidth(), lastHeight = getHeight();
        resize(width, height);
        return lastWidth != getWidth() || lastHeight != getHeight();
    }

    /**
     * Note that this does nothing if the width and height are the same.
     * */
    public void resize(int width, int height){
        //prevent incomplete attachment issues.
        width = Math.max(width, 2);
        height = Math.max(height, 2);

        //ignore pointless resizing
        if(width == getWidth() && height == getHeight()) return;

        TextureFilter min = getTexture().getMinFilter(), mag = getTexture().getMagFilter();
        dispose();

        FrameBufferBuilder frameBufferBuilder = new FrameBufferBuilder(width, height);
        frameBufferBuilder.addBasicColorTextureAttachment(format);
        if(hasDepth) frameBufferBuilder.addDepthTextureAttachment(Gl.depthComponent24, Gl.unsignedInt);
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
    public void begin(Color clearColor){
        begin();
        Gl.clearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
        Gl.clear(hasDepth ? Gl.colorBufferBit | Gl.depthBufferBit : Gl.colorBufferBit);
    }

    @Override
    protected Texture createTexture(FrameBufferTextureAttachmentSpec attachmentSpec){
        Texture result = new Texture();
        result.width = bufferBuilder.width;
        result.height = bufferBuilder.height;
        result.bind();
        Gl.texImage2D(Gl.texture2d, 0, attachmentSpec.internalFormat, bufferBuilder.width, bufferBuilder.height, 0, attachmentSpec.format, attachmentSpec.type, null);

        if(attachmentSpec.isDepth) depthTexture = result;
        if(attachmentSpec.isStencil) stencilTexture = result;

        result.setFilter(attachmentSpec.isColorTexture() ? TextureFilter.linear : TextureFilter.nearest);
        result.setWrap(TextureWrap.clampToEdge);
        return result;
    }

    @Override
    protected void disposeTexture(Texture colorTexture){
        colorTexture.dispose();
    }

    @Override
    protected void attachTexture(int attachment, Texture texture){
        Gl.framebufferTexture2D(Gl.framebuffer, attachment, Gl.texture2d, texture.getTextureObjectHandle(), 0);
    }
}
