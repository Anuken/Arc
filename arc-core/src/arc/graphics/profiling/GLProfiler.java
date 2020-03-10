package arc.graphics.profiling;

import arc.Graphics;
import arc.graphics.GL30;
import arc.math.FloatCounter;

/**
 * When enabled, collects statistics about GL calls and checks for GL errors.
 * Enabling will wrap Gdx.gl* instances with delegate classes which provide described functionality
 * and route GL calls to the actual GL instances.
 * @author Daniel Holderbaum
 * @author Jan Polák
 * @see GL20Interceptor
 * @see GL30Interceptor
 */
public class GLProfiler{
    private Graphics graphics;
    private GLInterceptor glInterceptor;
    private GLErrorListener listener;
    private boolean enabled = false;

    /**
     * Create a new instance of GLProfiler to monitor a {@link Graphics} instance's gl calls
     * @param graphics instance to monitor with this instance
     */
    public GLProfiler(Graphics graphics){
        this.graphics = graphics;
        GL30 gl30 = graphics.getGL30();
        if(gl30 != null){
            glInterceptor = new GL30Interceptor(this, graphics.getGL30());
        }else{
            glInterceptor = new GL20Interceptor(this, graphics.getGL20());
        }
        listener = GLErrorListener.LOGGING_LISTENER;
    }

    /** Enables profiling by replacing the {@code GL20} and {@code GL30} instances with profiling ones. */
    public void enable(){
        if(enabled) return;

        GL30 gl30 = graphics.getGL30();
        if(gl30 != null){
            graphics.setGL30((GL30)glInterceptor);
        }else{
            graphics.setGL20(glInterceptor);
        }

        enabled = true;
    }

    /** Disables profiling by resetting the {@code GL20} and {@code GL30} instances with the original ones. */
    public void disable(){
        if(!enabled) return;

        GL30 gl30 = graphics.getGL30();
        if(gl30 != null) graphics.setGL30(((GL30Interceptor)graphics.getGL30()).gl30);
        else graphics.setGL20(((GL20Interceptor)graphics.getGL20()).gl20);

        enabled = false;
    }

    /** @return the current {@link GLErrorListener} */
    public GLErrorListener getListener(){
        return listener;
    }

    /** Set the current listener for the {@link GLProfiler} to {@code errorListener} */
    public void setListener(GLErrorListener errorListener){
        this.listener = errorListener;
    }

    /** @return true if the GLProfiler is currently profiling */
    public boolean isEnabled(){
        return enabled;
    }

    /**
     * @return the total gl calls made since the last reset
     */
    public int getCalls(){
        return glInterceptor.calls;
    }

    /**
     * @return the total amount of texture bindings made since the last reset
     */
    public int getTextureBindings(){
        return glInterceptor.textureBindings;
    }

    public int getStateChanges(){
        return glInterceptor.stateChanges;
    }

    /**
     * @return the total amount of draw calls made since the last reset
     */
    public int getDrawCalls(){
        return glInterceptor.drawCalls;
    }

    /**
     * @return the total amount of shader switches made since the last reset
     */
    public int getShaderSwitches(){
        return glInterceptor.shaderSwitches;
    }

    /**
     * @return {@link FloatCounter} containing information about rendered vertices since the last reset
     */
    public FloatCounter getVertexCount(){
        return glInterceptor.vertexCount;
    }

    /**
     * Will reset the statistical information which has been collected so far. This should be called after every frame.
     * Error listener is kept as it is.
     */
    public void reset(){
        glInterceptor.reset();
    }

}
