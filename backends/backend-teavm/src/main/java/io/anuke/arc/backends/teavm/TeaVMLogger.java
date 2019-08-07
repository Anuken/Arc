package io.anuke.arc.backends.teavm;

import io.anuke.arc.util.*;
import io.anuke.arc.util.Log.*;
import org.teavm.jso.JSBody;

public class TeaVMLogger extends LogHandler{

    @Override
    public void print(String text, Object... args){
        consoleLog(Strings.format(text, args));
    }

    @JSBody(params = "message", script = "console.log(\"TeaVM: \" + message);")
    native static public void consoleLog(String message);
}
