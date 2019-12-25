package com.badlogic.gdx.backends.iosrobovm;

import arc.Audio;
import arc.audio.AudioDevice;
import arc.audio.AudioRecorder;
import arc.audio.Music;
import arc.audio.Sound;
import com.badlogic.gdx.backends.iosrobovm.objectal.OALAudioTrack;
import com.badlogic.gdx.backends.iosrobovm.objectal.OALSimpleAudio;
import arc.files.Fi;
import arc.util.ArcRuntimeException;
import arc.util.Log;

public class IOSAudio extends Audio{

    public IOSAudio(IOSApplicationConfiguration config){
        OALSimpleAudio audio = OALSimpleAudio.sharedInstance();
        if(audio != null){
            audio.setAllowIpod(config.allowIpod);
            audio.setHonorSilentSwitch(!config.overrideRingerSwitch);
        }else{
            Log.errTag("IOSAudio", "No OALSimpleAudio instance available, audio will not be available");
        }
    }

    @Override
    public AudioDevice newAudioDevice(int samplingRate, boolean isMono){
        return null; //no support
    }

    @Override
    public AudioRecorder newAudioRecorder(int samplingRate, boolean isMono){
        return null; //no support
    }

    @Override
    public Sound newSound(Fi fileHandle){
        return new IOSSound(fileHandle);
    }

    @Override
    public Music newMusic(Fi fileHandle){
        String path = fileHandle.file().getPath().replace('\\', '/');
        OALAudioTrack track = OALAudioTrack.create();
        if(track != null){
            if(track.preloadFile(path)){
                return new IOSMusic(track);
            }
        }
        throw new ArcRuntimeException("Error opening music file at " + path);
    }

}