package io.anuke.arc.backends.teavm.emu;

import io.anuke.arc.backends.teavm.plugin.Annotations.*;

@Emulate(Throwable.class)
public class ThrowableEmu{
    public Throwable getCause(){
        return null;
    }
}
