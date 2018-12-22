/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package io.anuke.arc.graphics.profiling;

import io.anuke.arc.util.ArcRuntimeException;
import io.anuke.arc.util.Log;

import static io.anuke.arc.graphics.profiling.GLInterceptor.resolveErrorNumber;

/**
 * Listener for GL errors detected by {@link GLProfiler}.
 * @author Jan Polák
 * @see GLProfiler
 */
public interface GLErrorListener{

    /** Listener that will log using Gdx.app.error GL error name and GL function. */
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
            Log.err("[GLProfiler] Error {0} from {1}", resolveErrorNumber(error), place);
        }else{
            Log.err("[GLProfiler] Error {0} at: {1}", resolveErrorNumber(error), new Exception());
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
