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

package io.anuke.arc.backends.gwt;

import io.anuke.arc.Audio;
import io.anuke.arc.audio.AudioDevice;
import io.anuke.arc.audio.AudioRecorder;
import io.anuke.arc.audio.Music;
import io.anuke.arc.audio.Sound;
import io.anuke.arc.files.FileHandle;
import io.anuke.arc.utils.ArcRuntimeException;

public class GwtAudio implements Audio{
    @Override
    public AudioDevice newAudioDevice(int samplingRate, boolean isMono){
        throw new ArcRuntimeException("AudioDevice not supported by GWT backend");
    }

    @Override
    public AudioRecorder newAudioRecorder(int samplingRate, boolean isMono){
        throw new ArcRuntimeException("AudioRecorder not supported by GWT backend");
    }

    @Override
    public Sound newSound(FileHandle fileHandle){
        return new GwtSound(fileHandle);
    }

    @Override
    public Music newMusic(FileHandle file){
        return new GwtMusic(file);
    }
}
