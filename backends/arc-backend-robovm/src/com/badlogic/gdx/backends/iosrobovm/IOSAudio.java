/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.iosrobovm;

import io.anuke.arc.Audio;
import io.anuke.arc.audio.AudioDevice;
import io.anuke.arc.audio.AudioRecorder;
import io.anuke.arc.audio.Music;
import io.anuke.arc.audio.Sound;
import com.badlogic.gdx.backends.iosrobovm.objectal.OALAudioTrack;
import com.badlogic.gdx.backends.iosrobovm.objectal.OALSimpleAudio;
import io.anuke.arc.files.FileHandle;
import io.anuke.arc.utils.ArcRuntimeException;
import io.anuke.arc.utils.Log;

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