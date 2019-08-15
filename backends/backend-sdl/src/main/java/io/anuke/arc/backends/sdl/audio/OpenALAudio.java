package io.anuke.arc.backends.sdl.audio;

import io.anuke.arc.*;
import io.anuke.arc.audio.*;
import io.anuke.arc.audio.mock.*;
import io.anuke.arc.collection.*;
import io.anuke.arc.files.*;
import io.anuke.arc.math.*;
import io.anuke.arc.util.*;

import java.nio.*;

import static sdl.AL.*;

public class OpenALAudio extends Audio{
    Array<OpenALMusic> music = new Array<>(false, 1, OpenALMusic.class);
    long device;
    long context;
    boolean noDevice = false;
    private IntArray idleSources, allSources;
    private LongMap<Integer> soundIdToSource;
    private IntMap<Long> sourceToSoundId;
    private long nextSoundId = 0;
    private ObjectMap<String, SoundConstructor> soundTypes = new ObjectMap<>();
    private ObjectMap<String, MusicConstructor> musicTypes = new ObjectMap<>();
    private OpenALSound[] recentSounds;
    private int mostRecetSound = -1;

    public OpenALAudio(int simultaneousSources){
        soundTypes.put("ogg", Ogg.Sound::new);
        musicTypes.put("ogg", Ogg.Music::new);
        soundTypes.put("wav", Wav.Sound::new);
        musicTypes.put("wav", Wav.Music::new);
        soundTypes.put("mp3", Mp3.Sound::new);
        musicTypes.put("mp3", Mp3.Music::new);

        device = alcOpenDevice();
        if(device == 0L){
            noDevice = true;
            checkError();
            return;
        }
        //long deviceCapabilities = createCapabilities(device);
        context = alcCreateContext(device, null);
        if(context == 0L){
            alcCloseDevice(device);
            noDevice = true;
            checkError();
            return;
        }
        if(!alcMakeContextCurrent(context)){
            noDevice = true;
            checkError();
            return;
        }
        Log.info("OpenAL " + alGetString(AL_VERSION));
        //AL.createCapabilities(deviceCapabilities);

        allSources = new IntArray(false, simultaneousSources);
        for(int i = 0; i < simultaneousSources; i++){
            int sourceID = alGenSources();
            if(alGetError() != AL_NO_ERROR) break;
            allSources.add(sourceID);
        }
        idleSources = new IntArray(allSources);
        soundIdToSource = new LongMap<>();
        sourceToSoundId = new IntMap<>();

        FloatBuffer orientation = (FloatBuffer)BufferUtils.newFloatBuffer(6)
        .put(new float[]{0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f}).flip();
        alListenerfv(AL_ORIENTATION, orientation);
        FloatBuffer velocity = (FloatBuffer)BufferUtils.newFloatBuffer(3).put(new float[]{0.0f, 0.0f, 0.0f}).flip();
        alListenerfv(AL_VELOCITY, velocity);
        FloatBuffer position = (FloatBuffer)BufferUtils.newFloatBuffer(3).put(new float[]{0.0f, 0.0f, 0.0f}).flip();
        alListenerfv(AL_POSITION, position);

        recentSounds = new OpenALSound[simultaneousSources];
    }

    void checkError(){
        int error = alGetError();
        if(error != AL_NO_ERROR){
            throw new ArcRuntimeException("AL Initialization error, code: " + error);
        }
    }

    @Override
    public OpenALSound newSound(FileHandle file){
        if(file == null) throw new IllegalArgumentException("file cannot be null.");
        SoundConstructor soundClass = soundTypes.get(file.extension().toLowerCase());
        if(soundClass == null) throw new ArcRuntimeException("Unknown file extension for sound: " + file);
        return soundClass.make(this, file);
    }

    @Override
    public OpenALMusic newMusic(FileHandle file){
        if(file == null) throw new IllegalArgumentException("file cannot be null.");
        MusicConstructor musicClass = musicTypes.get(file.extension().toLowerCase());
        if(musicClass == null) throw new ArcRuntimeException("Unknown file extension for music: " + file);
        return musicClass.make(this, file);
    }

    int obtainSource(boolean isMusic){
        if(noDevice) return 0;
        for(int i = 0, n = idleSources.size; i < n; i++){
            int sourceId = idleSources.get(i);
            int state = alGetSourcei(sourceId, AL_SOURCE_STATE);
            if(state != AL_PLAYING && state != AL_PAUSED){
                if(isMusic){
                    idleSources.removeIndex(i);
                }else{
                    if(sourceToSoundId.containsKey(sourceId)){
                        long soundId = sourceToSoundId.get(sourceId);
                        sourceToSoundId.remove(sourceId);
                        soundIdToSource.remove(soundId);
                    }

                    long soundId = nextSoundId++;
                    sourceToSoundId.put(sourceId, soundId);
                    soundIdToSource.put(soundId, sourceId);
                }
                alSourceStop(sourceId);
                alSourcei(sourceId, AL_BUFFER, 0);
                alSourcef(sourceId, AL_GAIN, 1);
                alSourcef(sourceId, AL_PITCH, 1);
                alSource3f(sourceId, AL_POSITION, 0, 0, 1f);
                return sourceId;
            }
        }
        return -1;
    }

    void freeSource(int sourceID){
        if(noDevice) return;
        alSourceStop(sourceID);
        alSourcei(sourceID, AL_BUFFER, 0);
        if(sourceToSoundId.containsKey(sourceID)){
            long soundId = sourceToSoundId.remove(sourceID);
            soundIdToSource.remove(soundId);
        }
        idleSources.add(sourceID);
    }

    void freeBuffer(int bufferID){
        if(noDevice) return;
        for(int i = 0, n = idleSources.size; i < n; i++){
            int sourceID = idleSources.get(i);
            if(alGetSourcei(sourceID, AL_BUFFER) == bufferID){
                if(sourceToSoundId.containsKey(sourceID)){
                    long soundId = sourceToSoundId.remove(sourceID);
                    soundIdToSource.remove(soundId);
                }
                alSourceStop(sourceID);
                alSourcei(sourceID, AL_BUFFER, 0);
            }
        }
    }

    void stopSourcesWithBuffer(int bufferID){
        if(noDevice) return;
        for(int i = 0, n = idleSources.size; i < n; i++){
            int sourceID = idleSources.get(i);
            if(alGetSourcei(sourceID, AL_BUFFER) == bufferID){
                if(sourceToSoundId.containsKey(sourceID)){
                    long soundId = sourceToSoundId.remove(sourceID);
                    soundIdToSource.remove(soundId);
                }
                alSourceStop(sourceID);
            }
        }
    }

    void pauseSourcesWithBuffer(int bufferID){
        if(noDevice) return;
        for(int i = 0, n = idleSources.size; i < n; i++){
            int sourceID = idleSources.get(i);
            if(alGetSourcei(sourceID, AL_BUFFER) == bufferID)
                alSourcePause(sourceID);
        }
    }

    void resumeSourcesWithBuffer(int bufferID){
        if(noDevice) return;
        for(int i = 0, n = idleSources.size; i < n; i++){
            int sourceID = idleSources.get(i);
            if(alGetSourcei(sourceID, AL_BUFFER) == bufferID){
                if(alGetSourcei(sourceID, AL_SOURCE_STATE) == AL_PAUSED)
                    alSourcePlay(sourceID);
            }
        }
    }

    public void update(){
        if(noDevice) return;
        for(int i = 0; i < music.size; i++)
            music.items[i].update();
    }

    public long getSoundId(int sourceId){
        if(!sourceToSoundId.containsKey(sourceId)) return -1;
        return sourceToSoundId.get(sourceId);
    }

    public void stopSound(long soundId){
        if(!soundIdToSource.containsKey(soundId)) return;
        int sourceId = soundIdToSource.get(soundId);
        alSourceStop(sourceId);
    }

    public void pauseSound(long soundId){
        if(!soundIdToSource.containsKey(soundId)) return;
        int sourceId = soundIdToSource.get(soundId);
        alSourcePause(sourceId);
    }

    public void resumeSound(long soundId){
        if(!soundIdToSource.containsKey(soundId)) return;
        int sourceId = soundIdToSource.get(soundId);
        if(alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PAUSED)
            alSourcePlay(sourceId);
    }

    public void setSoundGain(long soundId, float volume){
        if(!soundIdToSource.containsKey(soundId)) return;
        int sourceId = soundIdToSource.get(soundId);
        alSourcef(sourceId, AL_GAIN, volume);
    }

    public void setSoundLooping(long soundId, boolean looping){
        if(!soundIdToSource.containsKey(soundId)) return;
        int sourceId = soundIdToSource.get(soundId);
        alSourcei(sourceId, AL_LOOPING, looping ? AL_TRUE : AL_FALSE);
    }

    public void setSoundPitch(long soundId, float pitch){
        if(!soundIdToSource.containsKey(soundId)) return;
        int sourceId = soundIdToSource.get(soundId);
        alSourcef(sourceId, AL_PITCH, pitch);
    }

    public void setSoundPan(long soundId, float pan, float volume){
        if(!soundIdToSource.containsKey(soundId)) return;
        int sourceId = soundIdToSource.get(soundId);

        alSource3f(sourceId, AL_POSITION, Mathf.cos((pan - 1) * Mathf.PI / 2), 0,
        Mathf.sin((pan + 1) * Mathf.PI / 2));
        alSourcef(sourceId, AL_GAIN, volume);
    }

    public void dispose(){
        if(noDevice) return;
        for(int i = 0, n = allSources.size; i < n; i++){
            int sourceID = allSources.get(i);
            int state = alGetSourcei(sourceID, AL_SOURCE_STATE);
            if(state != AL_STOPPED) alSourceStop(sourceID);
            alDeleteSources(sourceID);
        }

        sourceToSoundId.clear();
        soundIdToSource.clear();

        alcDestroyContext(context);
        alcCloseDevice(device);
    }

    public AudioDevice newAudioDevice(int sampleRate, final boolean isMono){
        return new MockAudioDevice();
    }

    public AudioRecorder newAudioRecorder(int samplingRate, boolean isMono){
        return new MockAudioRecorder();
    }

    /**
     * Retains a list of the most recently played sounds and stops the sound played least recently if necessary for a new sound to
     * play
     */
    protected void retain(OpenALSound sound, boolean stop){
        // Move the pointer ahead and wrap
        mostRecetSound++;
        mostRecetSound %= recentSounds.length;

        if(stop){
            // Stop the least recent sound (the one we are about to bump off the buffer)
            if(recentSounds[mostRecetSound] != null) recentSounds[mostRecetSound].stop();
        }

        recentSounds[mostRecetSound] = sound;
    }

    /** Removes the disposed sound from the least recently played list */
    public void forget(OpenALSound sound){
        for(int i = 0; i < recentSounds.length; i++){
            if(recentSounds[i] == sound) recentSounds[i] = null;
        }
    }

    public interface SoundConstructor{
        OpenALSound make(OpenALAudio audio, FileHandle file);
    }

    public interface MusicConstructor{
        OpenALMusic make(OpenALAudio audio, FileHandle file);
    }
}
