package io.anuke.arc.audio.mock;

import io.anuke.arc.*;
import io.anuke.arc.audio.*;
import io.anuke.arc.files.*;

/**
 * The headless backend does its best to mock elements. This is intended to make code-sharing between
 * server and client as simple as possible.
 */
public class MockAudio extends Audio{

    @Override
    public AudioDevice newAudioDevice(int samplingRate, boolean isMono){
        return new MockAudioDevice();
    }

    @Override
    public AudioRecorder newAudioRecorder(int samplingRate, boolean isMono){
        return new MockAudioRecorder();
    }

    @Override
    public Sound newSound(FileHandle fileHandle){
        return new MockSound();
    }

    @Override
    public Music newMusic(FileHandle file){
        return new MockMusic();
    }
}
