package io.anuke.arc.backends.lwjgl3.audio;

import io.anuke.arc.Audio;
import io.anuke.arc.audio.AudioDevice;
import io.anuke.arc.audio.AudioRecorder;
import io.anuke.arc.audio.mock.*;
import io.anuke.arc.collection.*;
import io.anuke.arc.files.Fi;
import io.anuke.arc.math.Mathf;
import io.anuke.arc.util.ArcRuntimeException;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;

/** @author Nathan Sweet */
public class OpenALAudio extends Audio{
    private final int deviceBufferSize;
    private final int deviceBufferCount;
    Array<OpenALMusic> music = new Array<>(false, 1, OpenALMusic.class);
    long device;
    long context;
    boolean noDevice = false;
    private IntArray idleSources, allSources;
    private LongMap<Integer> soundIdToSource;
    private IntMap<Long> sourceToSoundId;
    private long nextSoundId = 0;
    private ObjectMap<String, Class<? extends OpenALSound>> extensionToSoundClass = new ObjectMap<>();
    private ObjectMap<String, Class<? extends OpenALMusic>> extensionToMusicClass = new ObjectMap<>();
    private OpenALSound[] recentSounds;
    private int mostRecetSound = -1;

    public OpenALAudio(){
        this(16, 9, 512);
    }

    public OpenALAudio(int simultaneousSources, int deviceBufferCount, int deviceBufferSize){
        this.deviceBufferSize = deviceBufferSize;
        this.deviceBufferCount = deviceBufferCount;

        registerSound("ogg", Ogg.Sound.class);
        registerMusic("ogg", Ogg.Music.class);
        registerSound("wav", Wav.Sound.class);
        registerMusic("wav", Wav.Music.class);
        registerSound("mp3", Mp3.Sound.class);
        registerMusic("mp3", Mp3.Music.class);

        device = alcOpenDevice((ByteBuffer)null);
        if(device == 0L){
            noDevice = true;
            return;
        }
        ALCCapabilities deviceCapabilities = ALC.createCapabilities(device);
        context = alcCreateContext(device, (IntBuffer)null);
        if(context == 0L){
            alcCloseDevice(device);
            noDevice = true;
            return;
        }
        if(!alcMakeContextCurrent(context)){
            noDevice = true;
            return;
        }
        AL.createCapabilities(deviceCapabilities);

        allSources = new IntArray(false, simultaneousSources);
        for(int i = 0; i < simultaneousSources; i++){
            int sourceID = alGenSources();
            if(alGetError() != AL_NO_ERROR) break;
            allSources.add(sourceID);
        }
        idleSources = new IntArray(allSources);
        soundIdToSource = new LongMap<>();
        sourceToSoundId = new IntMap<>();

        FloatBuffer orientation = (FloatBuffer)BufferUtils.createFloatBuffer(6)
        .put(new float[]{0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f}).flip();
        alListenerfv(AL_ORIENTATION, orientation);
        FloatBuffer velocity = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[]{0.0f, 0.0f, 0.0f}).flip();
        alListenerfv(AL_VELOCITY, velocity);
        FloatBuffer position = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[]{0.0f, 0.0f, 0.0f}).flip();
        alListenerfv(AL_POSITION, position);

        recentSounds = new OpenALSound[simultaneousSources];
    }

    public void registerSound(String extension, Class<? extends OpenALSound> soundClass){
        if(extension == null) throw new IllegalArgumentException("extension cannot be null.");
        if(soundClass == null) throw new IllegalArgumentException("soundClass cannot be null.");
        extensionToSoundClass.put(extension, soundClass);
    }

    public void registerMusic(String extension, Class<? extends OpenALMusic> musicClass){
        if(extension == null) throw new IllegalArgumentException("extension cannot be null.");
        if(musicClass == null) throw new IllegalArgumentException("musicClass cannot be null.");
        extensionToMusicClass.put(extension, musicClass);
    }

    @Override
    public OpenALSound newSound(Fi file){
        if(file == null) throw new IllegalArgumentException("file cannot be null.");
        Class<? extends OpenALSound> soundClass = extensionToSoundClass.get(file.extension().toLowerCase());
        if(soundClass == null) throw new ArcRuntimeException("Unknown file extension for sound: " + file);
        try{
            return soundClass.getConstructor(new Class[]{OpenALAudio.class, Fi.class}).newInstance(this, file);
        }catch(Exception ex){
            throw new ArcRuntimeException("Error creating sound " + soundClass.getName() + " for file: " + file, ex);
        }
    }

    @Override
    public OpenALMusic newMusic(Fi file){
        if(file == null) throw new IllegalArgumentException("file cannot be null.");
        Class<? extends OpenALMusic> musicClass = extensionToMusicClass.get(file.extension().toLowerCase());
        if(musicClass == null) throw new ArcRuntimeException("Unknown file extension for music: " + file);
        try{
            return musicClass.getConstructor(new Class[]{OpenALAudio.class, Fi.class}).newInstance(this, file);
        }catch(Exception ex){
            throw new ArcRuntimeException("Error creating music " + musicClass.getName() + " for file: " + file, ex);
        }
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
                AL10.alSourcef(sourceId, AL10.AL_GAIN, 1);
                AL10.alSourcef(sourceId, AL10.AL_PITCH, 1);
                AL10.alSource3f(sourceId, AL10.AL_POSITION, 0, 0, 1f);
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
        AL10.alSourcef(sourceId, AL10.AL_GAIN, volume);
    }

    public void setSoundLooping(long soundId, boolean looping){
        if(!soundIdToSource.containsKey(soundId)) return;
        int sourceId = soundIdToSource.get(soundId);
        alSourcei(sourceId, AL10.AL_LOOPING, looping ? AL10.AL_TRUE : AL10.AL_FALSE);
    }

    public void setSoundPitch(long soundId, float pitch){
        if(!soundIdToSource.containsKey(soundId)) return;
        int sourceId = soundIdToSource.get(soundId);
        AL10.alSourcef(sourceId, AL10.AL_PITCH, pitch);
    }

    public void setSoundPan(long soundId, float pan, float volume){
        if(!soundIdToSource.containsKey(soundId)) return;
        int sourceId = soundIdToSource.get(soundId);

        AL10.alSource3f(sourceId, AL10.AL_POSITION, Mathf.cos((pan - 1) * Mathf.PI / 2), 0,
        Mathf.sin((pan + 1) * Mathf.PI / 2));
        AL10.alSourcef(sourceId, AL10.AL_GAIN, volume);
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
        if(noDevice) return new MockAudioDevice();
        return new OpenALAudioDevice(this, sampleRate, isMono, deviceBufferSize, deviceBufferCount);
    }

    public AudioRecorder newAudioRecorder(int samplingRate, boolean isMono){
        if(noDevice) return new MockAudioRecorder();
        return new JavaSoundAudioRecorder(samplingRate, isMono);
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
}
