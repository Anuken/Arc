package arc.graphics.profiling;

import arc.util.*;

import static arc.graphics.profiling.GLInterceptor.resolveErrorNumber;

/**
 * Listener for GL errors detected by {@link GLProfiler}.
 * @author Jan Polák
 * @see GLProfiler
 */
public interface GLErrorListener{

    /** Listener that will log using Log.error GL error name and GL function. */
    GLErrorListener LOGGING_LISTENER = error -> {
        String place = null;
        try{
            final StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            for(int i = 0; i < stack.length; i++){
                if("check".equals(stack[i].getMethodName())){
                    if(i + 1 < stack.length){
                        final StackTraceElement glMethod = stack[i + 1];
                        place = glMethod.getMethodName();
                    }
                    break;
                }
            }
        }catch(Exception ignored){
        }

        if(place != null){
            Log.err("[GLProfiler] Error @ from @", resolveErrorNumber(error), place);
            throw new RuntimeException(Strings.format("[GLProfiler] Error @ from @", resolveErrorNumber(error), place));
        }else{
            Log.err("[GLProfiler] Error @ at: @", resolveErrorNumber(error), new Exception());
            throw new RuntimeException(Strings.format("[GLProfiler] Error @", resolveErrorNumber(error)));
            // This will capture current stack trace for logging, if possible
        }


    };

    // Basic implementations
    /** Listener that will throw a ArcRuntimeException with error name. */
    GLErrorListener THROWING_LISTENER = error -> {
        throw new ArcRuntimeException("GLProfiler: Got GL error " + resolveErrorNumber(error));
    };

    /**
     * Put your error logging code here.
     * @see GLInterceptor#resolveErrorNumber(int)
     */
    void onError(int error);
}
