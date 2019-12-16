package io.anuke.arc.backends.teavm;

import io.anuke.arc.util.Log.*;
import io.anuke.arc.util.*;
import org.teavm.jso.*;

public class TeaVMLogger implements LogHandler{

    @Override
    public void log(LogLevel level, String text, Object... args){
        consoleLog("[" + level.name() + "]: " + Strings.format(text, args));
    }

    @JSBody(params = "message", script = "console.log(\"Arc: \" + message);")
    native static public void consoleLog(String message);
}
