package io.anuke.arc.backends.teavm;

import io.anuke.arc.*;
import io.anuke.arc.audio.*;
import io.anuke.arc.files.*;
import io.anuke.arc.util.*;

public class TeaVMAudio extends Audio{
    @Override
    public AudioDevice newAudioDevice(int samplingRate, boolean isMono){
        throw new ArcRuntimeException("AudioDevice not supported by TeaVM backend");
    }

    @Override
    public AudioRecorder newAudioRecorder(int samplingRate, boolean isMono){
        throw new ArcRuntimeException("AudioDevice not supported by TeaVM backend");
    }

    @Override
    public Sound newSound(Fi fileHandle){
        return new TeaVMSound((TeaVMFi)fileHandle);
    }

    @Override
    public Music newMusic(Fi file){
        return new TeaVMMusic((TeaVMFi)file);
    }
}
