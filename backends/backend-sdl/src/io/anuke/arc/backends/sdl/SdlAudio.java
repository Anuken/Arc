package io.anuke.arc.backends.sdl;

import io.anuke.arc.*;
import io.anuke.arc.audio.*;
import io.anuke.arc.backends.sdl.audio.*;
import io.anuke.arc.files.*;

public class SdlAudio implements Audio{

    public SdlAudio(){
        //SDLMixer.Mix_OpenAudio(22050, 40, 4096);
        //SDLMixer.Mix_Init();
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
        //SDLMixer.Mix_Quit();
        //SDLMixer.Mix_CloseAudio();
    }
}
