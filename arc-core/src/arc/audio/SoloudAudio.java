package arc.audio;

import arc.*;
import arc.Files.*;
import arc.files.*;
import arc.mock.*;
import arc.struct.*;
import arc.util.*;

import java.io.*;

public class SoloudAudio extends Audio{
    Seq<SoloudMusic> music = new Seq<>();

    /** Intializes Soloud audio. May throw an exception. */
    public SoloudAudio(){
        init();
        setMaxSounds(16);
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
            SoloudMusic out = new SoloudMusic(file);
            if(music.isEmpty()){
                addUpdater();
            }
            music.add(out);
            return out;
        }catch(Throwable t){
            Log.err(t);
            return new MockMusic();
        }
    }

    @Override
    public void setFilter(int index, @Nullable AudioFilter filter){
        setGlobalFilter(index, filter == null ? 0 : filter.handle);
    }

    @Override
    public void dispose(){
        deinit();
    }

    @Override
    public void setMaxSounds(int max){

    }

    static class SoloudSound implements Sound{
        long handle;

        public SoloudSound(byte[] data){
            handle = wavLoad(data, data.length);
        }

        @Override
        public void setFilter(int index, @Nullable AudioFilter filter){
            sourceFilter(handle, index, filter == null ? 0 : filter.handle);
        }

        @Override
        public int play(float volume, float pitch, float pan, boolean loop){
            return sourcePlay(handle, volume, pitch, pan, loop, true);
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

        public SoloudMusic(Fi file) throws IOException{
            Fi result;

            if(file.type() == FileType.external){
                result = file;
            }else{
                String name = file.nameWithoutExtension() + "__" + file.length() + "." + file.extension();
                result = Core.files.cache(name);
                //check if file already exists (use length as "hash")
                if(!(result.exists() && !result.isDirectory() && result.length() == file.length())){
                    //save to the cached file
                    file.copyTo(result);
                }
            }

            handle = streamLoad(result.file().getCanonicalPath());
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
        public void setFilter(int index, @Nullable AudioFilter filter){
            sourceFilter(handle, index, filter == null ? 0 : filter.handle);
        }

        @Override
        public void play(){
            if(!playing){
                playing = true;
                paused = false;
                voice = sourcePlay(handle, volume, pitch, pan, looping, false);
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

    //TODO
    public abstract static class AudioFilter{
        long handle;

        protected AudioFilter(long handle){
            this.handle = handle;
        }
    }

    public static class BiquadFilter extends AudioFilter{
        public BiquadFilter(){ super(filterBiquad()); }
        public void set(int type, float frequency, float resonance){ biquadSet(handle, type, frequency, resonance); }
    }

    public static class EchoFilter extends AudioFilter{
        public EchoFilter(){ super(filterEcho()); }
        public void set(float delay, float decay, float filter){ echoSet(handle, delay, decay, filter); }
    }

    public static class LofiFilter extends AudioFilter{
        public LofiFilter(){ super(filterLofi()); }
        public void set(float sampleRate, float depth){ lofiSet(handle, sampleRate, depth); }
    }

    public static class FlangerFilter extends AudioFilter{
        public FlangerFilter(){ super(filterFlanger()); }
        public void set(float delay, float frequency){ flangerSet(handle, delay, frequency); }
    }

    public static class WaveShaperFilter extends AudioFilter{
        public WaveShaperFilter(){ super(filterWaveShaper()); }
        public void set(float amount){ waveShaperSet(handle, amount); }
    }

    public static class BassBoostFilter extends AudioFilter{
        public BassBoostFilter(){ super(filterBassBoost()); }
        public void set(float amount){ bassBoostSet(handle, amount); }
    }

    public static class RobotizeFilter extends AudioFilter{
        public RobotizeFilter(){ super(filterRobotize()); }
        public void set(float freq, int waveform){ robotizeSet(handle, freq, waveform); }
    }

    public static class FreeverbFilter extends AudioFilter{
        public FreeverbFilter(){ super(filterFreeverb()); }
        public void set(float mode, float roomSize, float damp, float width){ freeverbSet(handle, mode, roomSize, damp, width); }
    }

    /*JNI
    #include "soloud.h"
    #include "soloud_file.h"
    #include "soloud_wav.h"
    #include "soloud_wavstream.h"
    #include "soloud_speech.h"
    #include "soloud_thread.h"
    #include "soloud_filter.h"
    #include "soloud_biquadresonantfilter.h"
    #include "soloud_echofilter.h"
    #include "soloud_lofifilter.h"
    #include "soloud_flangerfilter.h"
    #include "soloud_waveshaperfilter.h"
    #include "soloud_bassboostfilter.h"
    #include "soloud_robotizefilter.h"
    #include "soloud_freeverbfilter.h"
    #include <stdio.h>

    using namespace SoLoud;

    Soloud soloud;
    int maxSounds;

    void throwError(JNIEnv* env, int result){
        jclass excClass = env->FindClass("arc/util/ArcRuntimeException");
        env->ThrowNew(excClass, soloud.getErrorString(result));
    }

    */

    public static native boolean canFopen(String path); /*
        return fopen(path, "rb") != NULL;
    */

    public static native boolean canSoloudFopen(String path); /*
        DiskFile fp;
		int res = fp.open(path);

		return (res == SO_NO_ERROR);
    */

    public static native int soloudFopenCode(String path); /*
        DiskFile fp;
		int res = fp.open(path);

		return res;
    */

    static native void init(); /*
        int result = soloud.init();

        if(result != 0) throwError(env, result);
    */

    static native void deinit(); /*
        soloud.deinit();
    */

    static native void maxSounds(int val); /*
        maxSounds = val;
    */

    static native void biquadSet(long handle, int type, float frequency, float resonance); /*
        ((BiquadResonantFilter*)handle)->setParams(type, frequency, resonance);
    */

    static native void echoSet(long handle, float delay, float decay, float filter); /*
        ((EchoFilter*)handle)->setParams(delay, decay, filter);
    */

    static native void lofiSet(long handle, float sampleRate, float bitDepth); /*
        ((LofiFilter*)handle)->setParams(sampleRate, bitDepth);
    */

    static native void flangerSet(long handle, float delay, float frequency); /*
        ((FlangerFilter*)handle)->setParams(delay, frequency);
    */

    static native void waveShaperSet(long handle, float amount); /*
        ((WaveShaperFilter*)handle)->setParams(amount);
    */

    static native void bassBoostSet(long handle, float amount); /*
        ((BassboostFilter*)handle)->setParams( amount);
    */

    static native void robotizeSet(long handle, float freq, int waveform); /*
        ((RobotizeFilter*)handle)->setParams(freq, waveform);
    */

    static native void freeverbSet(long handle, float mode, float roomSize, float damp, float width); /*
        ((FreeverbFilter*)handle)->setParams(mode, roomSize, damp, width);
    */

    static native long filterBiquad(); /* return (jlong)(new BiquadResonantFilter()); */
    static native long filterEcho(); /* return (jlong)(new EchoFilter()); */
    static native long filterLofi(); /* return (jlong)(new LofiFilter()); */
    static native long filterFlanger(); /* return (jlong)(new FlangerFilter()); */
    static native long filterBassBoost(); /* return (jlong)(new BassboostFilter()); */
    static native long filterWaveShaper(); /* return (jlong)(new WaveShaperFilter()); */
    static native long filterRobotize(); /* return (jlong)(new RobotizeFilter()); */
    static native long filterFreeverb(); /* return (jlong)(new FreeverbFilter()); */

    static native void setGlobalFilter(int index, long handle); /*
        soloud.setGlobalFilter(index, ((Filter*)handle));
    */

    static native long wavLoad(byte[] bytes, int length); /*
        Wav* wav = new Wav();

        int result = wav->loadMem((unsigned char*)bytes, length, true, true);

        if(result != 0) throwError(env, result);

        //do not play when inaudible
        wav->setInaudibleBehavior(false, true);

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

    static native int sourcePlay(long handle, float volume, float pitch, float pan, boolean loop, boolean kill); /*
        AudioSource* wav = (AudioSource*)handle;

        //don't play at all when there are too many voices
        if(soloud.getVoiceCount() > maxSounds && kill){
            return 0;
        }

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

    static native void sourceFilter(long handle, int index, long filter); /*
        ((AudioSource*)handle)->setFilter(index, ((Filter*)filter));
    */
}
