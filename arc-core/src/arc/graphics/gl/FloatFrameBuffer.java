package arc.graphics.gl;

import arc.*;
import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.graphics.gl.GLVersion.*;
import arc.util.*;

import java.nio.*;

/** This is a {@link FrameBuffer} variant backed by a float texture. */
public class FloatFrameBuffer extends FrameBuffer{
    FloatBuffer buffer;

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
        Texture result = new Texture();
        result.width = bufferBuilder.width;
        result.height = bufferBuilder.height;
        result.bind();

        if(!attachmentSpec.isGpuOnly){
            int amountOfFloats = 4;
            if(Core.graphics.getGLVersion().type.equals(GlType.OpenGL)){
                if(attachmentSpec.internalFormat == GL30.GL_RGBA16F || attachmentSpec.internalFormat == GL30.GL_RGBA32F) amountOfFloats = 4;
                if(attachmentSpec.internalFormat == GL30.GL_RGB16F || attachmentSpec.internalFormat == GL30.GL_RGB32F) amountOfFloats = 3;
                if(attachmentSpec.internalFormat == GL30.GL_RG16F || attachmentSpec.internalFormat == GL30.GL_RG32F) amountOfFloats = 2;
                if(attachmentSpec.internalFormat == GL30.GL_R16F || attachmentSpec.internalFormat == GL30.GL_R32F) amountOfFloats = 1;
            }
            this.buffer = Buffers.newFloatBuffer(bufferBuilder.width * bufferBuilder.height * amountOfFloats);
        }

        if(Core.app.isAndroid() || Core.app.isIOS() || Core.app.isWeb()){

            if(!Core.graphics.supportsExtension("OES_texture_float"))
                throw new ArcRuntimeException("Extension OES_texture_float not supported!");

            // GLES and WebGL defines texture format by 3rd and 8th argument,
            // so to get a float texture one needs to supply GL_RGBA and GL_FLOAT there.
            Gl.texImage2D(Gl.texture2d, 0, GL20.GL_RGBA, bufferBuilder.width, bufferBuilder.height, 0, GL20.GL_RGBA, GL20.GL_FLOAT, buffer);

        }else{
            if(!Core.graphics.isGL30Available()){
                if(!Core.graphics.supportsExtension("GL_ARB_texture_float"))
                    throw new ArcRuntimeException("Extension GL_ARB_texture_float not supported!");
            }
            // in desktop OpenGL the texture format is defined only by the third argument,
            // hence we need to use GL_RGBA32F there (this constant is unavailable in GLES/WebGL)
            Gl.texImage2D(Gl.texture2d, 0, attachmentSpec.internalFormat, bufferBuilder.width, bufferBuilder.height, 0, attachmentSpec.format, GL20.GL_FLOAT, buffer);
        }

        if(Core.app.isDesktop())
            result.setFilter(TextureFilter.linear, TextureFilter.linear);
        else
            // no filtering for float textures in OpenGL ES
            result.setFilter(TextureFilter.nearest, TextureFilter.nearest);
        result.setWrap(TextureWrap.clampToEdge, TextureWrap.clampToEdge);
        return result;
    }

}
