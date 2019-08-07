package io.anuke.arc.backends.teavm.emu;

import io.anuke.arc.backends.teavm.plugin.Annotations.Emulate;
import io.anuke.arc.util.io.*;

import java.io.*;

@Emulate(Streams.class)
public class StreamUtilsEmu{

    public static void closeQuietly (Closeable c) {

    }
}
