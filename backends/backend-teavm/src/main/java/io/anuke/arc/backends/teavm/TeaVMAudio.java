package io.anuke.arc.backends.teavm;

import io.anuke.arc.*;
import io.anuke.arc.audio.*;
import io.anuke.arc.files.*;
import io.anuke.arc.util.*;

/**
 *
 * @author Alexey Andreev
 */
public class TeaVMAudio extends Audio {
    @Override
    public AudioDevice newAudioDevice(int samplingRate, boolean isMono) {
        throw new ArcRuntimeException("AudioDevice not supported by TeaVM backend");
    }

    @Override
    public AudioRecorder newAudioRecorder(int samplingRate, boolean isMono) {
        throw new ArcRuntimeException("AudioDevice not supported by TeaVM backend");
    }

    @Override
    public Sound newSound(FileHandle fileHandle) {
        return new TeaVMSound((TeaVMFileHandle)fileHandle);
    }

    @Override
    public Music newMusic(FileHandle file) {
        return new TeaVMMusic((TeaVMFileHandle)file);
    }
}
