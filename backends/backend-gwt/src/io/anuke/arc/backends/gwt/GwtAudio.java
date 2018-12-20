package io.anuke.arc.backends.gwt;

import io.anuke.arc.Audio;
import io.anuke.arc.audio.AudioDevice;
import io.anuke.arc.audio.AudioRecorder;
import io.anuke.arc.audio.Music;
import io.anuke.arc.audio.Sound;
import io.anuke.arc.files.FileHandle;
import io.anuke.arc.utils.ArcRuntimeException;

public class GwtAudio implements Audio{
    @Override
    public AudioDevice newAudioDevice(int samplingRate, boolean isMono){
        throw new ArcRuntimeException("AudioDevice not supported by GWT backend");
    }

    @Override
    public AudioRecorder newAudioRecorder(int samplingRate, boolean isMono){
        throw new ArcRuntimeException("AudioRecorder not supported by GWT backend");
    }

    @Override
    public Sound newSound(FileHandle fileHandle){
        return new GwtSound(fileHandle);
    }

    @Override
    public Music newMusic(FileHandle file){
        return new GwtMusic(file);
    }
}
