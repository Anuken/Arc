package arc.mock;

import arc.audio.*;

/**
 * The headless backend does its best to mock elements. This is intended to make code-sharing between
 * server and client as simple as possible.
 */
public class MockAudioDevice implements AudioDevice{

    @Override
    public boolean isMono(){
        return false;
    }

    @Override
    public void writeSamples(short[] samples, int offset, int numSamples){

    }

    @Override
    public void writeSamples(float[] samples, int offset, int numSamples){

    }

    @Override
    public int getLatency(){
        return 0;
    }

    @Override
    public void dispose(){

    }

    @Override
    public void setVolume(float volume){

    }
}
