package arc.audio;

import arc.*;
import arc.Files.*;
import arc.files.*;
import arc.mock.*;
import arc.struct.*;
import arc.util.*;

public class SoloudAudio extends Audio{
    Seq<SoloudMusic> music = new Seq<>();

    /** Intializes Soloud audio. May throw an exception. */
    public SoloudAudio(){
        init();
    }

    protected void addUpdater(){
        Core.app.addListener(new ApplicationListener(){
            @Override
            public void update(){
                for(SoloudMusic music : music){
                    music.update();
                }
            }
        });
    }

    @Override
    public Sound newSound(Fi file){
        try{
            try{
                return new SoloudSound(file.readBytes());
            }catch(ArcRuntimeException e){
                throw new ArcRuntimeException("Error loading sound: " + file, e);
            }
        }catch(Throwable t){
            Log.err(t);
            return new MockSound();
        }
    }

    @Override
    public Music newMusic(Fi file){
        try{
            try{
                SoloudMusic out = new SoloudMusic(file);
                if(music.isEmpty()){
                    addUpdater();
                }
                music.add(out);
                return out;
            }catch(UnsupportedOperationException e){ //cache may be unavailable - don't crash in that case
                Log.err(e.getCause());
                return new MockMusic();
            }catch(ArcRuntimeException e){
                throw new ArcRuntimeException("Error loading music: " + file, e);
            }
        }catch(Throwable t){
            Log.err(t);
            return new MockMusic();
        }
    }

    @Override
    public void dispose(){
        deinit();
    }

    static class SoloudSound implements Sound{
        long handle;

        public SoloudSound(byte[] data){
            handle = wavLoad(data, data.length);
        }

        @Override
        public int play(float volume, float pitch, float pan, boolean loop){
            return sourcePlay(handle, volume, pitch, pan, loop);
        }

        @Override
        public void stop(){
            sourceStop(handle);
        }

        @Override
        public void pause(){
            //TODO remove
        }

        @Override
        public void resume(){
            //TODO remove
        }

        @Override
        public void dispose(){
            //TODO does nothing
        }

        @Override
        public void stop(int soundId){
            idStop(soundId);
        }

        @Override
        public void pause(int soundId){
            idPause(soundId, true);
        }

        @Override
        public void resume(int soundId){
            idPause(soundId, false);
        }

        @Override
        public void setLooping(int soundId, boolean looping){
            idLooping(soundId, looping);
        }

        @Override
        public void setPitch(int soundId, float pitch){
            idPitch(soundId, pitch);
        }

        @Override
        public void setVolume(int soundId, float volume){
            idVolume(soundId, volume);
        }

        @Override
        public void setPan(int soundId, float pan, float volume){
            idVolume(soundId, volume);
            idPan(soundId, pan);
        }
    }

    static class SoloudMusic implements Music{
        long handle;
        boolean playing, looping, paused;
        int voice;
        float volume = 1f, pitch = 1f, pan = 0f;
        OnCompletionListener listener;

        public SoloudMusic(Fi file){
            Fi result;

            try{
                if(file.type() == FileType.external){
                    Log.info("using direct file");
                    result = file;
                }else{
                    String name = file.nameWithoutExtension() + "__" + file.length() + "." + file.extension();
                    result = Core.files.cache(name);
                    //check if file already exists (use length as "hash")
                    if(!(result.exists() && !result.isDirectory() && result.length() == file.length())){
                        //save to the cached file
                        file.copyTo(result);
                    }
                    Log.info("using cache file: @", result.absolutePath());
                }
            }catch(Exception e){
                throw new UnsupportedOperationException(e);
            }

            handle = streamLoad(result.absolutePath());
        }

        public void update(){
            if(playing && !idValid(voice)){
                voice = 0;
                playing = paused = false;
                if(listener != null){
                    listener.complete(this);
                }
            }
        }

        @Override
        public void play(){
            if(!playing){
                playing = true;
                paused = false;
                voice = sourcePlay(handle, volume, pitch, pan, looping);
                idProtected(voice, true);
            }else if(paused){
                idPause(voice, paused = false);
            }
        }

        @Override
        public void pause(){
            if(playing && !paused){
                idPause(voice, paused = true);
            }
        }

        @Override
        public void stop(){
            if(playing){
                playing = false;
                paused = false;
                sourceStop(handle);
            }
        }

        @Override
        public boolean isPlaying(){
            return playing && !paused;
        }

        @Override
        public boolean isLooping(){
            return looping;
        }

        @Override
        public void setLooping(boolean isLooping){
            this.looping = isLooping;

            if(playing){
                idLooping(voice, isLooping);
            }
        }

        @Override
        public float getVolume(){
            return volume;
        }

        @Override
        public void setVolume(float volume){
            this.volume = volume;
            if(playing){
                idVolume(voice, volume);
            }
        }

        @Override
        public void setPan(float pan, float volume){
            this.volume = volume;
            this.pan = pan;

            if(playing){
                idVolume(voice, volume);
                idPan(voice, pan);
            }
        }

        @Override
        public float getPosition(){
            if(!playing){
                return 0;
            }
            return idPosition(voice);
        }

        @Override
        public void setPosition(float position){
            if(playing){
                idSeek(voice, position);
            }
        }

        @Override
        public void dispose(){
            //TODO does nothing
        }

        @Override
        public void setCompletionListener(OnCompletionListener listener){
            this.listener = listener;
        }
    }


    /*JNI
    #include "soloud.h"
    #include "soloud_wav.h"
    #include "soloud_wavstream.h"
    #include "soloud_speech.h"
    #include "soloud_thread.h"
    #include <stdio.h>

    using namespace SoLoud;

    Soloud soloud;

    void throwError(JNIEnv* env, int result){
        jclass excClass = env->FindClass("arc/util/ArcRuntimeException");
        env->ThrowNew(excClass, soloud.getErrorString(result));
    }

     */

    static native void init(); /*
        int result = soloud.init();

        if(result != 0) throwError(env, result);
    */

    static native void deinit(); /*
        soloud.deinit();
    */

    static native long wavLoad(byte[] bytes, int length); /*
        Wav* wav = new Wav();

        int result = wav->loadMem((unsigned char*)bytes, length, true, true);

        if(result != 0) throwError(env, result);

        return (jlong)wav;
    */

    static native double wavDestroy(long handle); /*
        Wav* source = (Wav*)handle;
        delete source;
    */

    static native void idSeek(int id, float seconds); /*
        soloud.seek(id, seconds);
    */

    static native void idVolume(int id, float volume); /*
        soloud.setVolume(id, volume);
    */

    static native void idPan(int id, float pan); /*
        soloud.setPan(id, pan);
    */

    static native void idPitch(int id, float pitch); /*
        soloud.setRelativePlaySpeed(id, pitch);
    */

    static native void idPause(int id, boolean pause); /*
        soloud.setPause(id, pause);
    */

    static native void idProtected(int id, boolean protect); /*
        soloud.setProtectVoice(id, protect);
    */

    static native void idStop(int voice); /*
        soloud.stop(voice);
    */

    static native void idLooping(int voice, boolean looping); /*
        soloud.setLooping(voice, looping);
    */

    static native float idPosition(int voice); /*
        return (jfloat)soloud.getStreamPosition(voice);
    */

    static native boolean idValid(int voice); /*
        return soloud.isValidVoiceHandle(voice);
    */

    static native long streamLoad(String path); /*
        WavStream* stream = new WavStream();

        int result = stream->load(path);

        if(result != 0) throwError(env, result);

        //do not overlap music
        stream->setSingleInstance(true);

        return (jlong)stream;
    */

    static native double streamLength(long handle); /*
        WavStream* source = (WavStream*)handle;
        return (jdouble)source->getLength();
    */

    static native double streamDestroy(long handle); /*
        WavStream* source = (WavStream*)handle;
        delete source;
    */

    static native int sourcePlay(long handle, float volume, float pitch, float pan, boolean loop); /*
        AudioSource* wav = (AudioSource*)handle;

        int voice = soloud.play(*wav, volume, pan);
        soloud.setLooping(voice, loop);
        soloud.setRelativePlaySpeed(voice, pitch);

        return voice;
    */

    static native void sourceLoop(long handle, boolean loop); /*
        AudioSource* source = (AudioSource*)handle;
        source->setLooping(loop);
    */

    static native void sourceStop(long handle); /*
        AudioSource* source = (AudioSource*)handle;
        source->stop();
    */
}
