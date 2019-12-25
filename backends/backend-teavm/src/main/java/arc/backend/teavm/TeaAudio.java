package arc.backend.teavm;

import arc.*;
import arc.audio.*;
import arc.files.*;
import arc.util.*;

public class TeaAudio extends Audio{
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
        return new TeaSound((TeaFi)fileHandle);
    }

    @Override
    public Music newMusic(Fi file){
        return new TeaMusic((TeaFi)file);
    }
}
