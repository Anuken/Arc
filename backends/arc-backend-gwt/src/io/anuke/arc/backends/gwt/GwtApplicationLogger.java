/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
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

package io.anuke.arc.backends.gwt;

import io.anuke.arc.Core;
import io.anuke.arc.utils.Log.LogHandler;
import io.anuke.arc.utils.Strings;
import com.google.gwt.user.client.ui.TextArea;

public class GwtApplicationLogger extends LogHandler{
    private TextArea log;

    public GwtApplicationLogger(TextArea log){
        this.log = log;
    }

    @Override
    public void info(String text, Object... args){
        String res = Strings.formatArgs(text, args);
        checkLogLabel();
        log.setText(log.getText() + "\n" + res);
        log.setCursorPos(log.getText().length() - 1);
        System.out.println(res);
    }

    @Override
    public void warn(String text, Object... args){
        String res = Strings.formatArgs(text, args);
        checkLogLabel();
        log.setText(log.getText() + "\n" + res);
        log.setCursorPos(log.getText().length() - 1);
        System.out.println(res);
    }

    @Override
    public void err(String text, Object... args){
        String res = Strings.formatArgs(text, args);
        checkLogLabel();
        log.setText(log.getText() + "\n" + res);
        log.setCursorPos(log.getText().length() - 1);
        System.out.println(res);
    }

    private void checkLogLabel(){
        if(log == null){
            ((GwtApplication)Core.app).log = log = new TextArea();

            // It's possible that log functions are called
            // before the app is initialized. E.g. SoundManager can call log functions before the app is initialized.
            // Since graphics is null, we're getting errors. The log size will be updated later, in case graphics was null
            if(Core.graphics != null){
                log.setSize(Core.graphics.getWidth() + "px", "200px");
            }else{
                log.setSize("400px", "200px"); // Dummy value
            }

            log.setReadOnly(true);
            ((GwtApplication)Core.app).getRootPanel().add(log);
        }
    }

    private String getMessages(Throwable e){
        StringBuilder sb = new StringBuilder();
        while(e != null){
            sb.append(e.getMessage() + "\n");
            e = e.getCause();
        }
        return sb.toString();
    }

    private String getStackTrace(Throwable e){
        StringBuilder sb = new StringBuilder();
        for(StackTraceElement trace : e.getStackTrace()){
            sb.append(trace.toString() + "\n");
        }
        return sb.toString();
    }

}
