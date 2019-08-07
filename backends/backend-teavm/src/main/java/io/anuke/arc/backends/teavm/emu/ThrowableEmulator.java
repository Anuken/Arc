package io.anuke.arc.backends.teavm.emu;

import io.anuke.arc.backends.teavm.plugin.Annotations.Emulate;

@Emulate(Throwable.class)
public class ThrowableEmulator{
    public Throwable getCause(){
        return null;
    }
}
