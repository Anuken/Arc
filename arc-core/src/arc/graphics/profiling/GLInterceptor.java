package arc.graphics.profiling;

import arc.graphics.GL20;
import arc.math.FloatCounter;

public abstract class GLInterceptor implements GL20{
    public final FloatCounter vertexCount = new FloatCounter(0);
    public int calls;
    public int textureBindings;
    public int drawCalls;
    public int shaderSwitches;
    public int stateChanges;
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

    public void reset(){
        calls = 0;
        textureBindings = 0;
        drawCalls = 0;
        shaderSwitches = 0;
        stateChanges = 0;
        vertexCount.reset();
    }
}
