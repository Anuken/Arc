package io.anuke.arc.backends.sdl;

import io.anuke.arc.*;
import io.anuke.arc.audio.*;
import io.anuke.arc.files.*;

public class SdlAudio implements Audio{
    @Override
    public AudioDevice newAudioDevice(int samplingRate, boolean isMono){
        return null;
    }

    @Override
    public AudioRecorder newAudioRecorder(int samplingRate, boolean isMono){
        return null;
    }

    @Override
    public Sound newSound(FileHandle fileHandle){
        return null;
    }

    @Override
    public Music newMusic(FileHandle file){
        return null;
    }
}
