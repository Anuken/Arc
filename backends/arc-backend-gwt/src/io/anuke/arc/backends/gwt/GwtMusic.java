package io.anuke.arc.backends.gwt;

import io.anuke.arc.Core;
import io.anuke.arc.audio.Music;
import io.anuke.arc.backends.gwt.soundmanager2.SMSound;
import io.anuke.arc.backends.gwt.soundmanager2.SMSound.SMSoundCallback;
import io.anuke.arc.backends.gwt.soundmanager2.SMSoundOptions;
import io.anuke.arc.backends.gwt.soundmanager2.SoundManager;
import io.anuke.arc.files.FileHandle;

public class GwtMusic implements Music, SMSoundCallback{
    private boolean isPlaying = false;
    private boolean isLooping = false;
    private SMSound sound;
    private float volume = 1f;
    private float pan = 0f;
    private SMSoundOptions soundOptions;
    private OnCompletionListener onCompletionListener;

    public GwtMusic(FileHandle file){
        String url = ((GwtApplication)Core.app).getBaseUrl() + file.path();
        sound = SoundManager.createSound(url);
        soundOptions = new SMSoundOptions();
        soundOptions.callback = this;
    }

    @Override
    public void play(){
        if(isPlaying()) return;
        if(sound.getPaused()){
            resume();
            return;
        }
        soundOptions.volume = (int)(volume * 100);
        soundOptions.pan = (int)(pan * 100);
        soundOptions.loops = 1;
        soundOptions.from = 0;
        sound.play(soundOptions);
        isPlaying = true;
    }

    public void resume(){
        sound.resume();
    }

    @Override
    public void pause(){
        sound.pause();
        isPlaying = false;
    }

    @Override
    public void stop(){
        sound.stop();
        isPlaying = false;
    }

    @Override
    public boolean isPlaying(){
        isPlaying = !sound.getPaused() && sound.getPlayState() == 1;
        return isPlaying;
    }

    @Override
    public boolean isLooping(){
        return isLooping;
    }

    @Override
    public void setLooping(boolean isLooping){
        this.isLooping = isLooping;
    }

    @Override
    public float getVolume(){
        return volume;
    }

    @Override
    public void setVolume(float volume){
        sound.setVolume((int)(volume * 100));
        this.volume = volume;
    }

    @Override
    public void setPan(float pan, float volume){
        sound.setPan((int)(pan * 100));
        sound.setVolume((int)(volume * 100));
        this.pan = pan;
        this.volume = volume;
    }

    @Override
    public float getPosition(){
        return sound.getPosition() / 1000f;
    }

    @Override
    public void setPosition(float position){
        sound.setPosition((int)(position * 1000f));
    }

    @Override
    public void dispose(){
        sound.destruct();
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener){
        onCompletionListener = listener;
    }

    @Override
    public void onfinish(){
        if(isLooping)
            play();
        else if(onCompletionListener != null)
            onCompletionListener.onCompletion(this);
    }
}