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

import io.anuke.arc.utils.ArcRuntimeException;
import io.anuke.arc.utils.Log;

import static io.anuke.arc.graphics.profiling.GLInterceptor.resolveErrorNumber;

/**
 * @author Jan Polák
 * @see GLProfiler
 */
public interface GLErrorListener{

    /** Listener that will log using Gdx.app.error GL error name and GL function. */
    GLErrorListener LOGGING_LISTENER = new GLErrorListener(){

        public void onError(int error){
            final Exception exc = new Exception();
            String place = null;
            try{
                final StackTraceElement[] stack = exc.getStackTrace();
                for(int i = 0; i < stack.length; i++){
                    if(stack[i].getMethodName().contains("check")){
                        // GWT is mangling names, but this should (at least in dev mode) work.
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
                Log.err("[GLProfiler] Error " + resolveErrorNumber(error) + " from " + place);
            }else{
                StringBuilder sb = new StringBuilder("Error ");
                sb.append(resolveErrorNumber(error));
                sb.append(" at:\n");
                try{
                    final StackTraceElement[] stack = exc.getStackTrace();
                    for(int i = 0; i < stack.length; i++){
                        sb.append(stack[i].toString()).append('\n');
                    }
                }catch(Exception ignored){
                    sb.append(" (Failed to print stack trace: ").append(ignored).append(")");
                }
                Log.errTag("GLProfiler", sb.toString());
                // GWT backend seems to have trouble printing stack traces reliably
            }
        }
    };

    // Basic implementations
    /** Listener that will throw a ArcRuntimeException with error name. */
    GLErrorListener THROWING_LISTENER = new GLErrorListener(){

        public void onError(int error){
            throw new ArcRuntimeException("GLProfiler: Got GL error " + resolveErrorNumber(error));
        }
    };

    /**
     * Put your error logging code here.
     * @see GLInterceptor#resolveErrorNumber(int)
     */
    void onError(int error);
}
