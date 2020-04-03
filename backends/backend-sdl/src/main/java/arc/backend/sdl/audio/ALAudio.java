package arc.backend.sdl.audio;

import arc.*;
import arc.audio.*;
import arc.files.*;
import arc.math.*;
import arc.mock.*;
import arc.struct.*;
import arc.util.*;

import java.nio.*;

import static arc.backend.sdl.jni.AL.*;

public class ALAudio extends Audio{
    Array<ALMusic> music = new Array<>(false, 1, ALMusic.class);
    long device;
    long context;
    boolean noDevice = false;
    private IntArray idleSources, allSources;
    private IntIntMap soundIdToSource;
    private IntIntMap sourceToSoundId;
    private int nextSoundId = 0;
    private ObjectMap<String, SoundConstructor> soundTypes = new ObjectMap<>();
    private ObjectMap<String, MusicConstructor> musicTypes = new ObjectMap<>();
    private ALSound[] recentSounds;
    private int mostRecetSound = -1;

    public ALAudio(int simultaneousSources){
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

        allSources = new IntArray(false, simultaneousSources);
        for(int i = 0; i < simultaneousSources; i++){
            int sourceID = alGenSources();
            if(alGetError() != AL_NO_ERROR) break;
            allSources.add(sourceID);
        }
        idleSources = new IntArray(allSources);
        soundIdToSource = new IntIntMap();
        sourceToSoundId = new IntIntMap();

        FloatBuffer orientation = (FloatBuffer)Buffers.newFloatBuffer(6).put(new float[]{0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f}).flip();
        alListenerfv(AL_ORIENTATION, orientation);
        FloatBuffer velocity = (FloatBuffer)Buffers.newFloatBuffer(3).put(new float[]{0.0f, 0.0f, 0.0f}).flip();
        alListenerfv(AL_VELOCITY, velocity);
        FloatBuffer position = (FloatBuffer)Buffers.newFloatBuffer(3).put(new float[]{0.0f, 0.0f, 0.0f}).flip();
        alListenerfv(AL_POSITION, position);

        recentSounds = new ALSound[simultaneousSources];
    }

    void checkError(){
        int error = alGetError();
        if(error != AL_NO_ERROR){
            throw new ArcRuntimeException("AL Initialization error, code: " + error);
        }
    }

    @Override
    public ALSound newSound(Fi file){
        if(file == null) throw new IllegalArgumentException("file cannot be null.");
        SoundConstructor soundClass = soundTypes.get(file.extension().toLowerCase());
        if(soundClass == null) throw new ArcRuntimeException("Unknown file extension for sound: " + file);
        return soundClass.make(this, file);
    }

    @Override
    public ALMusic newMusic(Fi file){
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
                        int soundId = sourceToSoundId.get(sourceId, 0);
                        sourceToSoundId.remove(sourceId, 0);
                        soundIdToSource.remove(soundId, 0);
                    }

                    int soundId = nextSoundId++;
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
            int soundId = sourceToSoundId.remove(sourceID, 0);
            soundIdToSource.remove(soundId, 0);
        }
        idleSources.add(sourceID);
    }

    void freeBuffer(int bufferID){
        if(noDevice) return;
        for(int i = 0, n = idleSources.size; i < n; i++){
            int sourceID = idleSources.get(i);
            if(alGetSourcei(sourceID, AL_BUFFER) == bufferID){
                if(sourceToSoundId.containsKey(sourceID)){
                    int soundId = sourceToSoundId.remove(sourceID, 0);
                    soundIdToSource.remove(soundId, 0);
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
                    int soundId = sourceToSoundId.remove(sourceID, 0);
                    soundIdToSource.remove(soundId, 0);
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

    public int getSoundId(int sourceId){
        if(!sourceToSoundId.containsKey(sourceId)) return -1;
        return sourceToSoundId.get(sourceId, 0);
    }

    public void stopSound(int soundId){
        if(!soundIdToSource.containsKey(soundId)) return;
        int sourceId = soundIdToSource.get(soundId, 0);
        alSourceStop(sourceId);
    }

    public void pauseSound(int soundId){
        if(!soundIdToSource.containsKey(soundId)) return;
        int sourceId = soundIdToSource.get(soundId, 0);
        alSourcePause(sourceId);
    }

    public void resumeSound(int soundId){
        if(!soundIdToSource.containsKey(soundId)) return;
        int sourceId = soundIdToSource.get(soundId, 0);
        if(alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PAUSED)
            alSourcePlay(sourceId);
    }

    public void setSoundGain(int soundId, float volume){
        if(!soundIdToSource.containsKey(soundId)) return;
        int sourceId = soundIdToSource.get(soundId, 0);
        alSourcef(sourceId, AL_GAIN, volume);
    }

    public void setSoundLooping(int soundId, boolean looping){
        if(!soundIdToSource.containsKey(soundId)) return;
        int sourceId = soundIdToSource.get(soundId, 0);
        alSourcei(sourceId, AL_LOOPING, looping ? AL_TRUE : AL_FALSE);
    }

    public void setSoundPitch(int soundId, float pitch){
        if(!soundIdToSource.containsKey(soundId)) return;
        int sourceId = soundIdToSource.get(soundId, 0);
        alSourcef(sourceId, AL_PITCH, pitch);
    }

    public void setSoundPan(int soundId, float pan, float volume){
        if(!soundIdToSource.containsKey(soundId)) return;
        int sourceId = soundIdToSource.get(soundId, 0);

        alSource3f(sourceId, AL_POSITION, Mathf.cos((pan - 1) * Mathf.PI / 2), 0, Mathf.sin((pan + 1) * Mathf.PI / 2));
        alSourcef(sourceId, AL_GAIN, volume);
    }

    @Override
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

    @Override
    public AudioDevice newAudioDevice(int sampleRate, final boolean isMono){
        return new MockAudioDevice();
    }

    @Override
    public AudioRecorder newAudioRecorder(int samplingRate, boolean isMono){
        return new MockAudioRecorder();
    }

    /**
     * Retains a list of the most recently played sounds and stops the sound played least recently if necessary for a new sound to
     * play
     */
    protected void retain(ALSound sound, boolean stop){
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
    public void forget(ALSound sound){
        for(int i = 0; i < recentSounds.length; i++){
            if(recentSounds[i] == sound) recentSounds[i] = null;
        }
    }

    public interface SoundConstructor{
        ALSound make(ALAudio audio, Fi file);
    }

    public interface MusicConstructor{
        ALMusic make(ALAudio audio, Fi file);
    }
}
