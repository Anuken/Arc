package arc.backend.teavm.emu;

import arc.backend.teavm.plugin.Annotations.*;

@Emulate(Throwable.class)
public class ThrowableEmu{
    public Throwable getCause(){
        return null;
    }
}
