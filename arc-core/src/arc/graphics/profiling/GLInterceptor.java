package arc.graphics.profiling;

import arc.graphics.GL20;
import arc.math.FloatCounter;

public abstract class GLInterceptor implements GL20{

    protected final FloatCounter vertexCount = new FloatCounter(0);
    protected int calls;
    protected int textureBindings;
    protected int drawCalls;
    protected int shaderSwitches;
    protected GLProfiler glProfiler;

    protected GLInterceptor(GLProfiler profiler){
        this.glProfiler = profiler;
    }

    public static String resolveErrorNumber(int error){
        switch(error){
            case GL_INVALID_VALUE:
                return "GL_INVALID_VALUE";
            case GL_INVALID_OPERATION:
                return "GL_INVALID_OPERATION";
            case GL_INVALID_FRAMEBUFFER_OPERATION:
                return "GL_INVALID_FRAMEBUFFER_OPERATION";
            case GL_INVALID_ENUM:
                return "GL_INVALID_ENUM";
            case GL_OUT_OF_MEMORY:
                return "GL_OUT_OF_MEMORY";
            default:
                return "number " + error;
        }
    }

    public int getCalls(){
        return calls;
    }

    public int getTextureBindings(){
        return textureBindings;
    }

    public int getDrawCalls(){
        return drawCalls;
    }

    public int getShaderSwitches(){
        return shaderSwitches;
    }

    public FloatCounter getVertexCount(){
        return vertexCount;
    }

    public void reset(){
        calls = 0;
        textureBindings = 0;
        drawCalls = 0;
        shaderSwitches = 0;
        vertexCount.reset();
    }
}
