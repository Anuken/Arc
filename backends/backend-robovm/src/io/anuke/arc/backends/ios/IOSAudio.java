package io.anuke.arc.backends.ios;

import io.anuke.arc.Audio;
import io.anuke.arc.audio.AudioDevice;
import io.anuke.arc.audio.AudioRecorder;
import io.anuke.arc.audio.Music;
import io.anuke.arc.audio.Sound;
import io.anuke.arc.backends.ios.objectal.OALAudioTrack;
import io.anuke.arc.backends.ios.objectal.OALSimpleAudio;
import io.anuke.arc.files.FileHandle;
import io.anuke.arc.util.ArcRuntimeException;
import io.anuke.arc.util.Log;

public class IOSAudio implements Audio{

    public IOSAudio(IOSApplicationConfiguration config){
        OALSimpleAudio audio = OALSimpleAudio.sharedInstance();
        if(audio != null){
            audio.setAllowIpod(config.allowIpod);
            audio.setHonorSilentSwitch(!config.overrideRingerSwitch);
        }else{
            Log.errTag("IOSAudio", "No OALSimpleAudio instance available, audio will not be availabe");
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
    public Sound newSound(FileHandle fileHandle){
        return new IOSSound(fileHandle);
    }

    @Override
    public Music newMusic(FileHandle fileHandle){
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