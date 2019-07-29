package io.anuke.arc.backends.sdl;

import io.anuke.arc.*;
import io.anuke.arc.audio.*;
import io.anuke.arc.backends.sdl.audio.*;
import io.anuke.arc.files.*;
import sdl.*;

public class SdlAudio implements Audio{

    public SdlAudio(){
        int i = SDLMixer.Mix_OpenAudio(22050, 2, 4096);
        if(i == -1) throw new SDLError();
        i = SDLMixer.Mix_Init();
        if(i == -1) throw new SDLError();
    }

    @Override
    public AudioDevice newAudioDevice(int samplingRate, boolean isMono){
        return null;
    }

    @Override
    public AudioRecorder newAudioRecorder(int samplingRate, boolean isMono){
        return null;
    }

    @Override
    public Sound newSound(FileHandle file){
        return new SdlSound(file);
    }

    @Override
    public Music newMusic(FileHandle file){
        return new SdlMusic(file);
    }

    @Override
    public void dispose(){
        SDLMixer.Mix_CloseAudio();
    }
}
