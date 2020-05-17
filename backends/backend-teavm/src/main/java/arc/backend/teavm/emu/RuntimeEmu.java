package arc.backend.teavm.emu;

import arc.backend.teavm.plugin.Annotations.*;

@Emulate(Runtime.class)
public class RuntimeEmu{

    public int availableProcessors(){
        return 1;
    }
}
