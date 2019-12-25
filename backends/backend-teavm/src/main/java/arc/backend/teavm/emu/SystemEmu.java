package arc.backend.teavm.emu;

import arc.backend.teavm.plugin.Annotations.*;

@Emulate(System.class)
public class SystemEmu{
    public static String getenv(String var0){
        return null;
    }
}
