package io.anuke.arc.postprocessing;

import io.anuke.arc.Core;
import io.anuke.arc.collection.Array;
import io.anuke.arc.graphics.Color;
import io.anuke.arc.graphics.GL20;
import io.anuke.arc.graphics.Pixmap.Format;
import io.anuke.arc.graphics.glutils.FrameBuffer;
import io.anuke.arc.util.Disposable;

/**
 * Provides a way to capture the rendered scene to an off-screen buffer and to apply a chain of effects on it before rendering to
 * screen.
 * <p>
 * Effects can be added or removed via {@link #addEffect(PostEffect)} and {@link #removeEffect(PostEffect)}.
 * @author bmanuel
 */
public final class PostProcessor implements Disposable{
    private static final Format format = Format.RGBA8888;
    private static final Array<PingPongBuffer> buffers = new Array<>();

    public final PingPongBuffer composite;
    public final Color clearColor = Color.clear;
    public boolean enabled = true;

    private final Array<PostEffect> effects = new Array<>();
    private final Array<PostEffect> items = new Array<>();
    private boolean capturing, hasCaptured;

    public PostProcessor(){
        composite = newPingPongBuffer(Core.graphics.getWidth(), Core.graphics.getHeight());
    }

    public void resize(int width, int height){
        composite.resize(width, height);

        for(PostEffect effect : effects){
            effect.resize(width, height);
        }
    }

    public void add(PostEffect effect){
        effect.rebind();
        effects.add(effect);
    }

    public void remove(PostEffect effect){
        effects.remove(effect);
    }

    /** If called before capturing it will indicate if the next capture call will succeeds or not. */
    public boolean isReady(){
        return enabled && !capturing && effects.contains(e -> e.enabled);
    }

    /**
     * Starts capturing the scene, clears the buffer with the clear color specified by {@link #setClearColor(Color)} or
     * {@link #setClearColor(float r, float g, float b, float a)}.
     * @return true or false, whether or not capturing has been initiated. Capturing will fail in case there are no enabled effects
     * in the chain or this instance is not enabled or capturing is already started.
     */
    public boolean capture(){
        hasCaptured = false;

        if(enabled && !capturing){
            if(!effects.contains(e -> e.enabled)){
                return false;
            }

            capturing = true;
            composite.begin();
            composite.capture();

            Core.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
            Core.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            return true;
        }

        return false;
    }

    /**
     * Starts capturing the scene as {@link #capture()}, but <strong>without</strong> clearing the screen.
     * @return true or false, whether or not capturing has been initiated.
     */
    public boolean captureNoClear(){
        hasCaptured = false;

        if(enabled && !capturing){
            if(!effects.contains(e -> e.enabled)){
                return false;
            }

            capturing = true;
            composite.begin();
            composite.capture();
            return true;
        }

        return false;
    }

    /** Stops capturing the scene and returns the result, or null if nothing was captured. */
    public FrameBuffer captureEnd(){
        if(enabled && capturing){
            capturing = false;
            hasCaptured = true;
            composite.end();
            return composite.getResultBuffer();
        }

        return null;
    }

    /** After a capture/captureEnd action, returns the just captured buffer */
    public FrameBuffer captured(){
        if(enabled && hasCaptured){
            return composite.getResultBuffer();
        }

        return null;
    }

    /** Regenerates and/or rebinds owned resources when needed, eg. when the OpenGL context is lost. */
    public void rebind(){

        for(int i = 0; i < buffers.size; i++){
            buffers.get(i).rebind();
        }

        for(PostEffect e : effects){
            e.rebind();
        }
    }

    /**
     * Stops capturing the scene and apply the effect chain, if there is one. If the specified output framebuffer is NULL, then the
     * rendering will be performed to screen.
     */
    public void render(FrameBuffer dest){
        captureEnd();

        if(!hasCaptured){
            return;
        }

        items.selectFrom(effects, e -> e.enabled);

        int count = items.size;
        if(count > 0){

            //render effects chain, [0,n-1]
            if(count > 1){
                for(int i = 0; i < count - 1; i++){
                    PostEffect e = items.get(i);

                    composite.capture();
                    e.render(composite.getSourceBuffer(), composite.getResultBuffer());
                }

                //complete
                composite.end();
            }

            //render with null dest (to screen)
            items.get(count - 1).render(composite.getResultBuffer(), dest);

            //ensure default texture unit #0 is active
            Core.gl.glActiveTexture(GL20.GL_TEXTURE0);
        }
    }

    /** Convenience method to render to screen. */
    public void render(){
        render(null);
    }

    /** Frees owned resources. */
    @Override
    public void dispose(){
        for(PostEffect effect : effects){
            effect.dispose();
        }
        effects.clear();

        // cleanup managed buffers, if any
        for(int i = 0; i < buffers.size; i++){
            buffers.get(i).dispose();
        }

        buffers.clear();
    }

    /**
     * Creates and returns a managed PingPongBuffer buffer, just create and forget. If rebind() is called on context loss, managed
     * PingPongBuffers will be rebound for you.
     * <p>
     * This is a drop-in replacement for the same-signature PingPongBuffer's constructor.
     */
    public static PingPongBuffer newPingPongBuffer(int width, int height){
        PingPongBuffer buffer = new PingPongBuffer(width, height, format, false);
        buffers.add(buffer);
        return buffer;
    }
}
