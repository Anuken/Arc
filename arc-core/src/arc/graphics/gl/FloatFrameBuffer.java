package arc.graphics.gl;

import arc.*;
import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.util.*;

/** This is a {@link FrameBuffer} variant backed by a float texture. */
public class FloatFrameBuffer extends FrameBuffer{

    FloatFrameBuffer(){
    }

    /**
     * Creates a GLFrameBuffer from the specifications provided by bufferBuilder
     **/
    protected FloatFrameBuffer(GLFrameBufferBuilder<? extends GLFrameBuffer<Texture>> bufferBuilder){
        super(bufferBuilder);
    }

    /**
     * Creates a new FrameBuffer with a float backing texture, having the given dimensions and potentially a depth buffer attached.
     * @param width the width of the framebuffer in pixels
     * @param height the height of the framebuffer in pixels
     * @param hasDepth whether to attach a depth buffer
     * @throws ArcRuntimeException in case the FrameBuffer could not be created
     */
    public FloatFrameBuffer(int width, int height, boolean hasDepth){
        FloatFrameBufferBuilder bufferBuilder = new FloatFrameBufferBuilder(width, height);
        bufferBuilder.addFloatAttachment(GL30.GL_RGBA32F, GL30.GL_RGBA, GL30.GL_FLOAT, false);
        if(hasDepth) bufferBuilder.addBasicDepthRenderBuffer();
        this.bufferBuilder = bufferBuilder;

        build();
    }

    @Override
    protected void create(Pixmap.Format format, int width, int height, boolean hasDepth, boolean hasStencil){
        //does nothing
    }

    @Override
    public void resize(int width, int height){
        throw new IllegalArgumentException("resize() is currently unsupported here.");
    }

    @Override
    protected Texture createTexture(FrameBufferTextureAttachmentSpec attachmentSpec){
        FloatTextureData data = new FloatTextureData(
        bufferBuilder.width, bufferBuilder.height,
        attachmentSpec.internalFormat, attachmentSpec.format, attachmentSpec.type,
        attachmentSpec.isGpuOnly
        );
        Texture result = new Texture(data);
        if(Core.app.isDesktop())
            result.setFilter(TextureFilter.linear, TextureFilter.linear);
        else
            // no filtering for float textures in OpenGL ES
            result.setFilter(TextureFilter.nearest, TextureFilter.nearest);
        result.setWrap(TextureWrap.clampToEdge, TextureWrap.clampToEdge);
        return result;
    }

}
