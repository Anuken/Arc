package arc.graphics.gl;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.Cubemap.*;
import arc.graphics.Texture.*;
import arc.util.*;

/**
 * <p>
 * Encapsulates OpenGL ES 2.0 frame buffer objects. This is a simple helper class which should cover most FBO uses. It will
 * automatically create a cubemap for the color attachment and a renderbuffer for the depth buffer. You can get a hold of the
 * cubemap by {@link FrameBufferCubemap#getTexture()}. This class will only work with OpenGL ES 2.0.
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
 *
 * <p>
 * Typical use: <br />
 * FrameBufferCubemap frameBuffer = new FrameBufferCubemap(Format.RGBA8888, fSize, fSize, true); <br />
 * frameBuffer.begin(); <br />
 * while( frameBuffer.nextSide() ) { <br />
 * frameBuffer.getSide().getUp(camera.up); <br />
 * frameBuffer.getSide().getDirection(camera.direction);<br />
 * camera.update(); <br />
 * <p>
 * Gl.clearColor(0, 0, 0, 1); <br />
 * Gl.clear(Gl.colorBufferBit | Gl.depthBufferBit); <br />
 * //render something <br />
 * } <br />
 * frameBuffer.end(); <br />
 * Cubemap cubemap = frameBuffer.getColorBufferCubemap();
 * </p>
 * @author realitix
 */
public class FrameBufferCubemap extends GLFrameBuffer<Cubemap>{
    /**
     * Creates a GLFrameBuffer from the specifications provided by bufferBuilder
     **/
    protected FrameBufferCubemap(GLFrameBufferBuilder<? extends GLFrameBuffer<Cubemap>> bufferBuilder){
        super(bufferBuilder);
    }

    /**
     * Creates a new FrameBuffer having the given dimensions and potentially a depth buffer attached.
     */
    public FrameBufferCubemap(Pixmap.Format format, int width, int height, boolean hasDepth){
        this(format, width, height, hasDepth, false);
    }

    /**
     * Creates a new FrameBuffer having the given dimensions and potentially a depth and a stencil buffer attached.
     * @param format the format of the color buffer; according to the OpenGL ES 2.0 spec, only RGB565, RGBA4444 and RGB5_A1 are
     * color-renderable
     * @param width the width of the cubemap in pixels
     * @param height the height of the cubemap in pixels
     * @param hasDepth whether to attach a depth buffer
     * @param hasStencil whether to attach a stencil buffer
     * @throws ArcRuntimeException in case the FrameBuffer could not be created
     */
    public FrameBufferCubemap(Pixmap.Format format, int width, int height, boolean hasDepth, boolean hasStencil){
        FrameBufferCubemapBuilder frameBufferBuilder = new FrameBufferCubemapBuilder(width, height);
        frameBufferBuilder.addBasicColorTextureAttachment(format);
        if(hasDepth) frameBufferBuilder.addBasicDepthRenderBuffer();
        if(hasStencil) frameBufferBuilder.addBasicStencilRenderBuffer();
        this.bufferBuilder = frameBufferBuilder;

        build();
    }


    @Override
    protected Cubemap createTexture(FrameBufferTextureAttachmentSpec attachmentSpec){
        GLOnlyTextureData data = new GLOnlyTextureData(bufferBuilder.width, bufferBuilder.height, 0, attachmentSpec.internalFormat, attachmentSpec.format, attachmentSpec.type);
        Cubemap result = new Cubemap(data, data, data, data, data, data);
        result.setFilter(TextureFilter.linear, TextureFilter.linear);
        result.setWrap(TextureWrap.clampToEdge, TextureWrap.clampToEdge);
        return result;
    }

    @Override
    protected void disposeTexture(Cubemap colorTexture){
        colorTexture.dispose();
    }

    @Override
    protected void attachTexture(int attachment, Cubemap texture){
        int glHandle = texture.getTextureObjectHandle();
        for(CubemapSide side : CubemapSide.all){
            Gl.framebufferTexture2D(Gl.framebuffer, attachment, side.glEnum, glHandle, 0);
        }
    }

    /** Should be called in between a call to {@link #begin()} and {@link #end()}. */
    public void eachSide(Cons<CubemapSide> cons){
        for(CubemapSide side : CubemapSide.all){
            cons.get(side);
        }
    }

    /**
     * Bind the side, making it active to render on. Should be called in between a call to {@link #begin()} and {@link #end()}.
     * @param side The side to bind
     */
    public void bindSide(CubemapSide side){
        Gl.framebufferTexture2D(Gl.framebuffer, Gl.colorAttachment0, side.glEnum, getTexture().getTextureObjectHandle(), 0);
    }
}
