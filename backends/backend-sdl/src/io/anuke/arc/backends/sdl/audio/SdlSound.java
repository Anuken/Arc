package io.anuke.arc.backends.sdl.audio;

import io.anuke.arc.audio.*;
import io.anuke.arc.files.*;

public class SdlSound implements Sound{
   // private final long handle;

    public SdlSound(FileHandle file){
        byte[] bytes = file.readBytes();
        //handle = SDLMixer.Mix_LoadWAVBytes(bytes, bytes.length);
    }

    @Override
    public long play(float volume, float pitch, float pan){
        float left = 1f - pan;
        int pl = (int)(left * 255);
        int pr = 254 - pl;

        //int sound = SDLMixer.Mix_PlayChannel(-1, handle, 0);
        //SDLMixer.MixVolume(sound, (int)(volume * 128));
        //SDLMixer.Mix_SetPanning(sound, pl, pr);
        //return sound;
        return 0;
    }

    //none of these are particularly useful or implemented

    /*
    @Override
    public long loop(float volume, float pitch, float pan){
        float left = 1f - pan;
        int pl = (int)(left * 255);
        int pr = 254 - pl;

        //int sound = SDLMixer.Mix_PlayChannel(-1, handle, -1);
        //SDLMixer.MixVolume(sound, (int)(volume * 128));
        //SDLMixer.Mix_SetPanning(sound, pl, pr);
        //return sound;
        return 0;
    }*/

    @Override public long loop(float volume, float pitch, float pan){return 0;}
    @Override public void stop(long soundId){}
    @Override public void pause(long soundId){}
    @Override public void resume(long soundId){}
    @Override public void stop(){}
    @Override public void pause(){}
    @Override public void resume(){}
    @Override public void dispose(){}
    @Override public void setLooping(long soundId, boolean looping){}
    @Override public void setPitch(long soundId, float pitch){}
    @Override public void setVolume(long soundId, float volume){}
    @Override public void setPan(long soundId, float pan, float volume){}
}
