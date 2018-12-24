package io.anuke.arc.backends.headless.mock;

import io.anuke.arc.Audio;
import io.anuke.arc.audio.AudioDevice;
import io.anuke.arc.audio.AudioRecorder;
import io.anuke.arc.audio.Music;
import io.anuke.arc.audio.Sound;
import io.anuke.arc.files.FileHandle;

/**
 * The headless backend does its best to mock elements. This is intended to make code-sharing between
 * server and client as simple as possible.
 */
public class MockAudio implements Audio{

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
