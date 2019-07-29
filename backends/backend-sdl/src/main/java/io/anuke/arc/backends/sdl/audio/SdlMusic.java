package io.anuke.arc.backends.sdl.audio;

import io.anuke.arc.audio.*;
import io.anuke.arc.files.*;

public class SdlMusic implements Music{

    public SdlMusic(FileHandle file){


    }

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
    public void setOnCompletionListener(OnCompletionListener listener){

    }
}
