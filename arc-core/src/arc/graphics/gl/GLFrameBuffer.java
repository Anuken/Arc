package arc.graphics.gl;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.struct.*;
import arc.util.*;

import java.nio.*;

/**
 * <p>
 * Encapsulates OpenGL ES 2.0 frame buffer objects. This is a simple helper class which should cover most FBO uses. It will
 * automatically create a gltexture for the color attachment and a renderbuffer for the depth buffer. You can get a hold of the
 * gltexture by {@link GLFrameBuffer#getTexture()}. This class will only work with OpenGL ES 2.0.
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
public abstract class GLFrameBuffer<T extends GLTexture> implements Disposable{
    protected final static int GL_DEPTH24_STENCIL8_OES = 0x88F0;
    /** the currently bound framebuffer; null for the default one. */
    protected static GLFrameBuffer currentBoundFramebuffer;
    /** the default framebuffer handle, a.k.a screen. */
    protected static int defaultFramebufferHandle;
    /** # of nested buffers right now */
    protected static int bufferNesting;
    /** true if we have polled for the default handle already. */
    protected static boolean defaultFramebufferHandleInitialized = false;
    /** the color buffer texture **/
    protected Seq<T> textureAttachments = new Seq<>();
    /** the framebuffer that was bound before this one began (null to indicate that nothing was bound) **/
    protected GLFrameBuffer lastBoundFramebuffer = null;
    /** the framebuffer handle **/
    protected int framebufferHandle;
    /** the depthbuffer render object handle **/
    protected int depthbufferHandle;
    /** the stencilbuffer render object handle **/
    protected int stencilbufferHandle;
    /** the depth stencil packed render buffer object handle **/
    protected int depthStencilPackedBufferHandle;
    /** if has depth stencil packed buffer **/
    protected boolean hasDepthStencilPackedBuffer;

    /** if multiple texture attachments are present **/
    protected boolean isMRT;

    protected GLFrameBufferBuilder<? extends GLFrameBuffer<T>> bufferBuilder;

    GLFrameBuffer(){
    }

    /** Creates a GLFrameBuffer from the specifications provided by bufferBuilder **/
    protected GLFrameBuffer(GLFrameBufferBuilder<? extends GLFrameBuffer<T>> bufferBuilder){
        this.bufferBuilder = bufferBuilder;
        build();
    }

    public static int getBufferNesting(){
        return bufferNesting;
    }

    /** Unbinds the framebuffer, all drawing will be performed to the normal framebuffer from here on. */
    public static void unbind(){
        Gl.bindFramebuffer(Gl.framebuffer, defaultFramebufferHandle);
    }

    /** Convenience method to return the first Texture attachment present in the fbo **/
    public T getTexture(){
        return textureAttachments.first();
    }

    /** Return the Texture attachments attached to the fbo **/
    public Seq<T> getTextureAttachments(){
        return textureAttachments;
    }

    /** Override this method in a derived class to set up the backing texture as you like. */
    protected abstract T createTexture(FrameBufferTextureAttachmentSpec attachmentSpec);

    /** Override this method in a derived class to dispose the backing texture as you like. */
    protected abstract void disposeColorTexture(T colorTexture);

    /** Override this method in a derived class to attach the backing texture to the GL framebuffer object. */
    protected abstract void attachFrameBufferColorTexture(T texture);

    protected void build(){

        checkValidBuilder();

        // iOS uses a different framebuffer handle! (not necessarily 0)
        if(!defaultFramebufferHandleInitialized){
            defaultFramebufferHandleInitialized = true;
            if(Core.app.isIOS()){
                IntBuffer intbuf = ByteBuffer.allocateDirect(16 * Integer.SIZE / 8).order(ByteOrder.nativeOrder()).asIntBuffer();
                Gl.getIntegerv(Gl.framebufferBinding, intbuf);
                defaultFramebufferHandle = intbuf.get(0);
            }else{
                defaultFramebufferHandle = 0;
            }
        }

        //save last buffer's handle
        int lastHandle = currentBoundFramebuffer == null ? defaultFramebufferHandle : currentBoundFramebuffer.framebufferHandle;

        framebufferHandle = Gl.genFramebuffer();
        Gl.bindFramebuffer(Gl.framebuffer, framebufferHandle);

        int width = bufferBuilder.width;
        int height = bufferBuilder.height;

        if(bufferBuilder.hasDepthRenderBuffer){
            depthbufferHandle = Gl.genRenderbuffer();
            Gl.bindRenderbuffer(Gl.renderbuffer, depthbufferHandle);
            Gl.renderbufferStorage(Gl.renderbuffer, bufferBuilder.depthRenderBufferSpec.internalFormat, width, height);
        }

        if(bufferBuilder.hasStencilRenderBuffer){
            stencilbufferHandle = Gl.genRenderbuffer();
            Gl.bindRenderbuffer(Gl.renderbuffer, stencilbufferHandle);
            Gl.renderbufferStorage(Gl.renderbuffer, bufferBuilder.stencilRenderBufferSpec.internalFormat, width, height);
        }

        if(bufferBuilder.hasPackedStencilDepthRenderBuffer){
            depthStencilPackedBufferHandle = Gl.genRenderbuffer();
            Gl.bindRenderbuffer(Gl.renderbuffer, depthStencilPackedBufferHandle);
            Gl.renderbufferStorage(Gl.renderbuffer, bufferBuilder.packedStencilDepthRenderBufferSpec.internalFormat, width,
            height);
        }

        isMRT = bufferBuilder.textureAttachmentSpecs.size > 1;
        int colorTextureCounter = 0;
        if(isMRT){
            for(FrameBufferTextureAttachmentSpec attachmentSpec : bufferBuilder.textureAttachmentSpecs){
                T texture = createTexture(attachmentSpec);
                textureAttachments.add(texture);
                if(attachmentSpec.isColorTexture()){
                    Gl.framebufferTexture2D(Gl.framebuffer, GL30.GL_COLOR_ATTACHMENT0 + colorTextureCounter, GL30.GL_TEXTURE_2D,
                    texture.getTextureObjectHandle(), 0);
                    colorTextureCounter++;
                }else if(attachmentSpec.isDepth){
                    Gl.framebufferTexture2D(Gl.framebuffer, GL20.GL_DEPTH_ATTACHMENT, GL20.GL_TEXTURE_2D,
                    texture.getTextureObjectHandle(), 0);
                }else if(attachmentSpec.isStencil){
                    Gl.framebufferTexture2D(Gl.framebuffer, GL20.GL_STENCIL_ATTACHMENT, GL20.GL_TEXTURE_2D,
                    texture.getTextureObjectHandle(), 0);
                }
            }
        }else{
            T texture = createTexture(bufferBuilder.textureAttachmentSpecs.first());
            textureAttachments.add(texture);
            Gl.bindTexture(texture.glTarget, texture.getTextureObjectHandle());
        }

        if(isMRT){
            IntBuffer buffer = Buffers.newIntBuffer(colorTextureCounter);
            for(int i = 0; i < colorTextureCounter; i++){
                buffer.put(GL30.GL_COLOR_ATTACHMENT0 + i);
            }
            buffer.position(0);
            Core.gl30.glDrawBuffers(colorTextureCounter, buffer);
        }else{
            attachFrameBufferColorTexture(textureAttachments.first());
        }

        if(bufferBuilder.hasDepthRenderBuffer){
            Gl.framebufferRenderbuffer(Gl.framebuffer, GL20.GL_DEPTH_ATTACHMENT, Gl.renderbuffer, depthbufferHandle);
        }

        if(bufferBuilder.hasStencilRenderBuffer){
            Gl.framebufferRenderbuffer(Gl.framebuffer, GL20.GL_STENCIL_ATTACHMENT, Gl.renderbuffer, stencilbufferHandle);
        }

        if(bufferBuilder.hasPackedStencilDepthRenderBuffer){
            Gl.framebufferRenderbuffer(Gl.framebuffer, GL30.GL_DEPTH_STENCIL_ATTACHMENT, Gl.renderbuffer,
            depthStencilPackedBufferHandle);
        }

        Gl.bindRenderbuffer(Gl.renderbuffer, 0);
        for(T texture : textureAttachments){
            Gl.bindTexture(texture.glTarget, 0);
        }

        int result = Gl.checkFramebufferStatus(Gl.framebuffer);

        if(result == Gl.framebufferUnsupported && bufferBuilder.hasDepthRenderBuffer && bufferBuilder.hasStencilRenderBuffer
        && (Core.graphics.supportsExtension("GL_OES_packed_depth_stencil")
        || Core.graphics.supportsExtension("GL_EXT_packed_depth_stencil"))){
            if(bufferBuilder.hasDepthRenderBuffer){
                Gl.deleteRenderbuffer(depthbufferHandle);
                depthbufferHandle = 0;
            }
            if(bufferBuilder.hasStencilRenderBuffer){
                Gl.deleteRenderbuffer(stencilbufferHandle);
                stencilbufferHandle = 0;
            }
            if(bufferBuilder.hasPackedStencilDepthRenderBuffer){
                Gl.deleteRenderbuffer(depthStencilPackedBufferHandle);
                depthStencilPackedBufferHandle = 0;
            }

            depthStencilPackedBufferHandle = Gl.genRenderbuffer();
            hasDepthStencilPackedBuffer = true;
            Gl.bindRenderbuffer(Gl.renderbuffer, depthStencilPackedBufferHandle);
            Gl.renderbufferStorage(Gl.renderbuffer, GL_DEPTH24_STENCIL8_OES, width, height);
            Gl.bindRenderbuffer(Gl.renderbuffer, 0);

            Gl.framebufferRenderbuffer(Gl.framebuffer, GL20.GL_DEPTH_ATTACHMENT, Gl.renderbuffer,
            depthStencilPackedBufferHandle);
            Gl.framebufferRenderbuffer(Gl.framebuffer, GL20.GL_STENCIL_ATTACHMENT, Gl.renderbuffer,
            depthStencilPackedBufferHandle);
            result = Gl.checkFramebufferStatus(Gl.framebuffer);
        }

        //restore old bound buffer
        Gl.bindFramebuffer(Gl.framebuffer, lastHandle);

        if(result != Gl.framebufferComplete){
            for(T texture : textureAttachments){
                disposeColorTexture(texture);
            }

            if(hasDepthStencilPackedBuffer){
                Gl.deleteBuffer(depthStencilPackedBufferHandle);
            }else{
                if(bufferBuilder.hasDepthRenderBuffer) Gl.deleteRenderbuffer(depthbufferHandle);
                if(bufferBuilder.hasStencilRenderBuffer) Gl.deleteRenderbuffer(stencilbufferHandle);
            }

            Gl.deleteFramebuffer(framebufferHandle);

            if(result == Gl.framebufferIncompleteAttachment)
                throw new IllegalStateException("Frame buffer couldn't be constructed: incomplete attachment");
            if(result == Gl.framebufferIncompleteDimensions)
                throw new IllegalStateException("Frame buffer couldn't be constructed: incomplete dimensions");
            if(result == Gl.framebufferIncompleteMissingAttachment)
                throw new IllegalStateException("Frame buffer couldn't be constructed: missing attachment");
            if(result == Gl.framebufferUnsupported)
                throw new IllegalStateException("Frame buffer couldn't be constructed: unsupported combination of formats");
            throw new IllegalStateException("Frame buffer couldn't be constructed: unknown error " + result);
        }
    }

    private void checkValidBuilder(){
        boolean runningGL30 = Core.graphics.isGL30Available();

        if(!runningGL30){
            if(bufferBuilder.hasPackedStencilDepthRenderBuffer){
                throw new ArcRuntimeException("Packed Stencil/Render render buffers are not available on GLES 2.0");
            }
            if(bufferBuilder.textureAttachmentSpecs.size > 1){
                throw new ArcRuntimeException("Multiple render targets not available on GLES 2.0");
            }
            for(FrameBufferTextureAttachmentSpec spec : bufferBuilder.textureAttachmentSpecs){
                if(spec.isDepth)
                    throw new ArcRuntimeException("Depth texture FrameBuffer Attachment not available on GLES 2.0");
                if(spec.isStencil)
                    throw new ArcRuntimeException("Stencil texture FrameBuffer Attachment not available on GLES 2.0");
                if(spec.isFloat){
                    if(!Core.graphics.supportsExtension("OES_texture_float")){
                        throw new ArcRuntimeException("Float texture FrameBuffer Attachment not available on GLES 2.0");
                    }
                }
            }
        }
    }

    /** Releases all resources associated with the FrameBuffer. */
    @Override
    public void dispose(){
        for(T texture : textureAttachments){
            disposeColorTexture(texture);
        }

        if(hasDepthStencilPackedBuffer){
            Gl.deleteRenderbuffer(depthStencilPackedBufferHandle);
        }else{
            if(bufferBuilder.hasDepthRenderBuffer) Gl.deleteRenderbuffer(depthbufferHandle);
            if(bufferBuilder.hasStencilRenderBuffer) Gl.deleteRenderbuffer(stencilbufferHandle);
        }

        Gl.deleteFramebuffer(framebufferHandle);
    }

    /** Makes the frame buffer current so everything gets drawn to it. */
    public void bind(){
        Gl.bindFramebuffer(Gl.framebuffer, framebufferHandle);
    }

    public boolean isBound(){
        return currentBoundFramebuffer == this;
    }

    /** Flushes the batch, begins this buffer and clears the screen.*/
    public void begin(Color clearColor){
        begin();
        Gl.clearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
        Gl.clear(depthbufferHandle != 0 ? Gl.colorBufferBit | Gl.depthBufferBit : Gl.colorBufferBit);
    }

    /** Binds the frame buffer and sets the viewport accordingly, so everything gets drawn to it. */
    public void begin(){
        Draw.flush();
        //save last buffer
        beginBind();
        setFrameBufferViewport();
    }

    /** Begins the buffer without setting the viewport or flushing the batch. */
    public void beginBind(){
        if(currentBoundFramebuffer == this) throw new IllegalArgumentException("Do not begin() twice.");
        //save last buffer
        lastBoundFramebuffer = currentBoundFramebuffer;
        currentBoundFramebuffer = this;
        bufferNesting ++;
        bind();
    }

    /** Sets viewport to the dimensions of framebuffer. Called by {@link #begin()}. */
    protected void setFrameBufferViewport(){
        Gl.viewport(0, 0, bufferBuilder.width, bufferBuilder.height);
    }

    /** Unbinds the framebuffer, all drawing will be performed to the normal framebuffer from here on. */
    public void end(){
        Draw.flush();
        //there was a buffer before this one
        if(lastBoundFramebuffer != null){
            //rebind the last framebuffer and set its viewport
            lastBoundFramebuffer.bind();
            lastBoundFramebuffer.setFrameBufferViewport();
        }else{
            //bind to default buffer and viewport
            unbind();
            Gl.viewport(0, 0, Core.graphics.getBackBufferWidth(), Core.graphics.getBackBufferHeight());
        }

        bufferNesting --;

        //set last bound framebuffer as current
        currentBoundFramebuffer = lastBoundFramebuffer;
        //no longer bound, so nothing came last
        lastBoundFramebuffer = null;
    }

    /** Stop binding. This does not flush the batch or change the viewport. */
    public void endBind(){
        //there was a buffer before this one
        if(lastBoundFramebuffer != null){
            //rebind the last framebuffer and set its viewport
            lastBoundFramebuffer.bind();
        }else{
            //bind to default buffer and viewport
            unbind();
        }

        bufferNesting --;

        //set last bound framebuffer as current
        currentBoundFramebuffer = lastBoundFramebuffer;
        //no longer bound, so nothing came last
        lastBoundFramebuffer = null;
    }

    /** @return The OpenGL handle of the framebuffer (see {@link GL20#glGenFramebuffer()}) */
    public int getFramebufferHandle(){
        return framebufferHandle;
    }

    /**
     * @return The OpenGL handle of the (optional) depth buffer (see {@link GL20#glGenRenderbuffer()}). May return 0 even if depth
     * buffer enabled
     */
    public int getDepthBufferHandle(){
        return depthbufferHandle;
    }

    /**
     * @return The OpenGL handle of the (optional) stencil buffer (see {@link GL20#glGenRenderbuffer()}). May return 0 even if
     * stencil buffer enabled
     */
    public int getStencilBufferHandle(){
        return stencilbufferHandle;
    }

    /** @return The OpenGL handle of the packed depth & stencil buffer (GL_DEPTH24_STENCIL8_OES) or 0 if not used. **/
    protected int getDepthStencilPackedBuffer(){
        return depthStencilPackedBufferHandle;
    }

    /** @return the height of the framebuffer in pixels */
    public int getHeight(){
        return bufferBuilder.height;
    }

    /** @return the width of the framebuffer in pixels */
    public int getWidth(){
        return bufferBuilder.width;
    }

    protected static class FrameBufferTextureAttachmentSpec{
        int internalFormat, format, type;
        boolean isFloat, isGpuOnly;
        boolean isDepth;
        boolean isStencil;

        public FrameBufferTextureAttachmentSpec(int internalformat, int format, int type){
            this.internalFormat = internalformat;
            this.format = format;
            this.type = type;
        }

        public boolean isColorTexture(){
            return !isDepth && !isStencil;
        }
    }

    protected static class FrameBufferRenderBufferAttachmentSpec{
        int internalFormat;

        public FrameBufferRenderBufferAttachmentSpec(int internalFormat){
            this.internalFormat = internalFormat;
        }
    }

    protected static abstract class GLFrameBufferBuilder<U extends GLFrameBuffer<? extends GLTexture>>{
        protected int width, height;

        protected Seq<FrameBufferTextureAttachmentSpec> textureAttachmentSpecs = new Seq<>();

        protected FrameBufferRenderBufferAttachmentSpec stencilRenderBufferSpec;
        protected FrameBufferRenderBufferAttachmentSpec depthRenderBufferSpec;
        protected FrameBufferRenderBufferAttachmentSpec packedStencilDepthRenderBufferSpec;

        protected boolean hasStencilRenderBuffer;
        protected boolean hasDepthRenderBuffer;
        protected boolean hasPackedStencilDepthRenderBuffer;

        public GLFrameBufferBuilder(int width, int height){
            this.width = width;
            this.height = height;
        }

        public GLFrameBufferBuilder<U> addColorTextureAttachment(int internalFormat, int format, int type){
            textureAttachmentSpecs.add(new FrameBufferTextureAttachmentSpec(internalFormat, format, type));
            return this;
        }

        public GLFrameBufferBuilder<U> addBasicColorTextureAttachment(Pixmap.Format format){
            int glFormat = format.glFormat;
            int glType = format.glType;
            return addColorTextureAttachment(glFormat, glFormat, glType);
        }

        public GLFrameBufferBuilder<U> addFloatAttachment(int internalFormat, int format, int type, boolean gpuOnly){
            FrameBufferTextureAttachmentSpec spec = new FrameBufferTextureAttachmentSpec(internalFormat, format, type);
            spec.isFloat = true;
            spec.isGpuOnly = gpuOnly;
            textureAttachmentSpecs.add(spec);
            return this;
        }

        public GLFrameBufferBuilder<U> addDepthTextureAttachment(int internalFormat, int type){
            FrameBufferTextureAttachmentSpec spec = new FrameBufferTextureAttachmentSpec(internalFormat, GL30.GL_DEPTH_COMPONENT,
            type);
            spec.isDepth = true;
            textureAttachmentSpecs.add(spec);
            return this;
        }

        public GLFrameBufferBuilder<U> addStencilTextureAttachment(int internalFormat, int type){
            FrameBufferTextureAttachmentSpec spec = new FrameBufferTextureAttachmentSpec(internalFormat, GL30.GL_STENCIL_ATTACHMENT,
            type);
            spec.isStencil = true;
            textureAttachmentSpecs.add(spec);
            return this;
        }

        public GLFrameBufferBuilder<U> addDepthRenderBuffer(int internalFormat){
            depthRenderBufferSpec = new FrameBufferRenderBufferAttachmentSpec(internalFormat);
            hasDepthRenderBuffer = true;
            return this;
        }

        public GLFrameBufferBuilder<U> addStencilRenderBuffer(int internalFormat){
            stencilRenderBufferSpec = new FrameBufferRenderBufferAttachmentSpec(internalFormat);
            hasStencilRenderBuffer = true;
            return this;
        }

        public GLFrameBufferBuilder<U> addStencilDepthPackedRenderBuffer(int internalFormat){
            packedStencilDepthRenderBufferSpec = new FrameBufferRenderBufferAttachmentSpec(internalFormat);
            hasPackedStencilDepthRenderBuffer = true;
            return this;
        }

        public GLFrameBufferBuilder<U> addBasicDepthRenderBuffer(){
            return addDepthRenderBuffer(GL20.GL_DEPTH_COMPONENT16);
        }

        public GLFrameBufferBuilder<U> addBasicStencilRenderBuffer(){
            return addStencilRenderBuffer(GL20.GL_STENCIL_INDEX8);
        }

        public GLFrameBufferBuilder<U> addBasicStencilDepthPackedRenderBuffer(){
            return addStencilDepthPackedRenderBuffer(GL30.GL_DEPTH24_STENCIL8);
        }

        public abstract U build();
    }

    public static class FrameBufferBuilder extends GLFrameBufferBuilder<FrameBuffer>{
        public FrameBufferBuilder(int width, int height){
            super(width, height);
        }

        @Override
        public FrameBuffer build(){
            return new FrameBuffer(this);
        }
    }

    public static class FloatFrameBufferBuilder extends GLFrameBufferBuilder<FloatFrameBuffer>{
        public FloatFrameBufferBuilder(int width, int height){
            super(width, height);
        }

        @Override
        public FloatFrameBuffer build(){
            return new FloatFrameBuffer(this);
        }
    }

    public static class FrameBufferCubemapBuilder extends GLFrameBufferBuilder<FrameBufferCubemap>{
        public FrameBufferCubemapBuilder(int width, int height){
            super(width, height);
        }

        @Override
        public FrameBufferCubemap build(){
            return new FrameBufferCubemap(this);
        }
    }
}
