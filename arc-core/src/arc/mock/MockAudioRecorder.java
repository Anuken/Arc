package arc.mock;

import arc.audio.*;

/**
 * The headless backend does its best to mock elements. This is intended to make code-sharing between
 * server and client as simple as possible.
 */
public class MockAudioRecorder implements AudioRecorder{

    @Override
    public void read(short[] samples, int offset, int numSamples){

    }

    @Override
    public void dispose(){

    }
}
