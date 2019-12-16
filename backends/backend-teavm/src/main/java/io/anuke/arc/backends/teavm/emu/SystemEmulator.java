package io.anuke.arc.backends.teavm.emu;

import io.anuke.arc.backends.teavm.plugin.Annotations.*;

@Emulate(System.class)
public class SystemEmulator{
    public static String getenv(String var0){
        return null;
    }
}
