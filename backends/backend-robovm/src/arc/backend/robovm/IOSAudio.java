package arc.backend.robovm;

import arc.*;
import arc.audio.*;
import arc.backend.robovm.objectal.*;
import arc.files.*;
import arc.mock.*;
import arc.util.*;

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
        Log.err("Error opening music file at " + path);
        return new MockMusic();
    }

}