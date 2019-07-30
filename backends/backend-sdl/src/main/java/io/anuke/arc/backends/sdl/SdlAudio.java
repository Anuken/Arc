package io.anuke.arc.backends.sdl;

import io.anuke.arc.*;
import io.anuke.arc.audio.*;
import io.anuke.arc.files.*;
import sdl.*;

public class SdlAudio extends Audio{
    private SdlMusic currentlyPlaying;

    public SdlAudio(){
        int i = SDLMixer.openAudio(22050*2, 2, 1024);
        if(i == -1) throw new SDLError();
        i = SDLMixer.init();
        if(i == -1) throw new SDLError();

        //this seems like a good number..?
        SDLMixer.allocateChannels(64);

        //hook into the listener
        SDLMixer.hookMusicFinished(() -> Core.app.post(() -> {
            if(currentlyPlaying != null && currentlyPlaying.listener != null){
                currentlyPlaying.listener.complete(currentlyPlaying);
                currentlyPlaying = null;
            }
        }));
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
        SDLMixer.closeAudio();
    }

    public class SdlSound implements Sound{
        private final long handle;

        public SdlSound(FileHandle file){
            byte[] bytes = file.readBytes();
            handle = SDLMixer.loadWAVBytes(bytes, bytes.length);
            if(handle == 0) throw new SDLError();
        }

        @Override
        public long play(float volume, float pitch, float pan){
            return play(volume, pitch, pan, false);
        }

        @Override
        public long loop(float volume, float pitch, float pan){
            return play(volume, pitch, pan, true);
        }

        //doesn't support setting pitch at all.
        //fantastic.
        long play(float volume, float pitch, float pan, boolean looping){
            if(volume < 0.1f) return -1; //don't play quiet sounds, it's not worth it
            float left = 1f - (pan + 1)/2f;
            int pl = (int)(left * 254);

            //SDLMixer.volumeChunk(handle, (int)(volume * 128));
            int sound = SDLMixer.playChannel(-1, handle, looping ? -1 : 0);
            if(sound == -1) return -1;
            SDLMixer.setPanning(sound, pl, 254 - pl);
            SDLMixer.volume(sound, (int)(volume * 128));
            return sound;
        }

        @Override
        public void dispose(){
            SDLMixer.freeChunk(handle);
        }

        //don't care about any of these... yet

        @Override public void stop(long soundId){}
        @Override public void pause(long soundId){}
        @Override public void resume(long soundId){}
        @Override public void stop(){}
        @Override public void pause(){}
        @Override public void resume(){}
        @Override public void setLooping(long soundId, boolean looping){}
        @Override public void setPitch(long soundId, float pitch){}
        @Override public void setVolume(long soundId, float volume){}
        @Override public void setPan(long soundId, float pan, float volume){}
    }

    public class SdlMusic implements Music{
        private final long handle;
        private float volume = 1f;
        private OnCompletionListener listener;

        public SdlMusic(FileHandle file){
            byte[] bytes = file.readBytes();
            handle = SDLMixer.loadMusicBytes(bytes, bytes.length);
            if(handle == 0) throw new SDLError();
        }

        @Override
        public void play(){
            SDLMixer.playMusic(handle, 1);
            currentlyPlaying = this;
        }

        @Override
        public void pause(){
            if(currentlyPlaying == this){
                currentlyPlaying = null;
                SDLMixer.pauseMusic();
            }
        }

        @Override
        public void stop(){
            if(currentlyPlaying == this){
                currentlyPlaying = null;
                SDLMixer.haltMusic();
            }
        }

        @Override
        public boolean isPlaying(){
            return currentlyPlaying == this;
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
            return volume;
        }

        @Override
        public void setVolume(float volume){
            if(currentlyPlaying == this){
                this.volume = volume;
                SDLMixer.volumeMusic((int)(volume * 128));
            }
        }

        @Override
        public void setPan(float pan, float volume){

        }

        @Override
        public float getPosition(){
            //unimplemented
            return 0;
        }

        @Override
        public void setPosition(float position){
            //unimplemented, because the API is so bad
            //who had the idea of making the position differ by format? how could that be even remotely close to a good idea?
        }

        @Override
        public void dispose(){
            if(currentlyPlaying == this){
                currentlyPlaying = null;
            }

            SDLMixer.freeMusic(handle);
        }

        @Override
        public void setCompletionListener(OnCompletionListener listener){
            this.listener = listener;
        }
    }
}
