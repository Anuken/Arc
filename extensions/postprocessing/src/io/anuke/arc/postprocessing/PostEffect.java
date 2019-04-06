package io.anuke.arc.postprocessing;

import io.anuke.arc.graphics.glutils.FrameBuffer;
import io.anuke.arc.util.Disposable;

/**
 * This interface defines the base class for the concrete implementation of post-processor effects. An effect is considered
 * enabled by default.
 * @author bmanuel
 */
public abstract class PostEffect implements Disposable{
    public boolean enabled = true;

    /**
     * Concrete objects shall be responsible to recreate or rebind its own resources whenever its needed, usually when the OpenGL
     * context is lost. Eg., framebuffer textures should be updated and shader parameters should be reuploaded/rebound.
     */
    public abstract void rebind();

    /** Concrete objects shall implements its own rendering, given the source and destination buffers. */
    public abstract void render(final FrameBuffer src, final FrameBuffer dest);

    public void resize(int width, int height){

    }
}
