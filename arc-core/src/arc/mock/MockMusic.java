package arc.mock;

import arc.audio.*;

/**
 * The headless backend does its best to mock elements. This is intended to make code-sharing between
 * server and client as simple as possible.
 */
public class MockMusic implements Music{
    @Override
    public void play(){

    }

    @Override
    public void pause(){

    }

    @Override
    public void stop(){

    }

    @Override
    public boolean isPlaying(){
        return false;
    }

    @Override
    public boolean isLooping(){
        return false;
    }

    @Override
    public void setLooping(boolean isLooping){

    }

    @Override
    public float getVolume(){
        return 0;
    }

    @Override
    public void setVolume(float volume){

    }

    @Override
    public void setPan(float pan, float volume){

    }

    @Override
    public float getPosition(){
        return 0;
    }

    @Override
    public void setPosition(float position){

    }

    @Override
    public void dispose(){

    }

    @Override
    public void setCompletionListener(OnCompletionListener listener){

    }
}
