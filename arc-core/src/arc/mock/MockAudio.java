package arc.mock;

import arc.*;
import arc.audio.*;
import arc.files.*;

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
    public Sound newSound(Fi fileHandle){
        return new MockSound();
    }

    @Override
    public Music newMusic(Fi file){
        return new MockMusic();
    }
}
