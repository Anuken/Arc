package com.badlogic.gdx.backends.iosrobovm;

import arc.Core;
import arc.audio.Music;
import com.badlogic.gdx.backends.iosrobovm.objectal.AVAudioPlayerDelegateAdapter;
import com.badlogic.gdx.backends.iosrobovm.objectal.OALAudioTrack;
import org.robovm.apple.foundation.NSObject;

/** @author Niklas Therning */
public class IOSMusic implements Music{
    private final OALAudioTrack track;
    OnCompletionListener onCompletionListener;

    public IOSMusic(OALAudioTrack track){
        this.track = track;
        this.track.setDelegate(new AVAudioPlayerDelegateAdapter(){
            @Override
            public void didFinishPlaying(NSObject player, boolean success){
                final OnCompletionListener listener = onCompletionListener;
                if(listener != null){
                    Core.app.post(() -> listener.complete(IOSMusic.this));
                }
            }
        });
    }

    @Override
    public void play(){
        if(track.isPaused()){
            track.setPaused(false);
        }else if(!track.isPlaying()){
            track.play();
        }
    }

    @Override
    public void pause(){
        if(track.isPlaying()){
            track.setPaused(true);
        }
    }

    @Override
    public void stop(){
        track.stop();
    }

    @Override
    public boolean isPlaying(){
        return track.isPlaying() && !track.isPaused();
    }

    @Override
    public boolean isLooping(){
        return track.getNumberOfLoops() == -1;
    }

    @Override
    public void setLooping(boolean isLooping){
        track.setNumberOfLoops(isLooping ? -1 : 0);
    }

    @Override
    public float getPosition(){
        return (float)(track.getCurrentTime());
    }

    @Override
    public void setPosition(float position){
        track.setCurrentTime(position);
    }

    @Override
    public void dispose(){
        track.clear();
    }

    @Override
    public float getVolume(){
        return track.getVolume();
    }

    @Override
    public void setVolume(float volume){
        track.setVolume(volume);
    }

    @Override
    public void setPan(float pan, float volume){
        track.setPan(pan);
        track.setVolume(volume);
    }

    @Override
    public void setCompletionListener(OnCompletionListener listener){
        this.onCompletionListener = listener;
    }

}
