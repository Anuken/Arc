package io.anuke.arc.backends.teavm.emu;

import io.anuke.arc.backends.teavm.plugin.Annotations.Emulate;

@Emulate(Thread.class)
public class ThreadEmulator{
    public void setDaemon(boolean b){}
}
