package arc.backend.sdl.audio;

import arc.audio.*;
import arc.struct.*;
import arc.files.*;
import arc.math.*;
import arc.util.*;

import java.nio.*;

import static arc.backend.sdl.jni.AL.*;

/** @author Nathan Sweet */
public abstract class ALMusic implements Music{
    static private final int bufferSize = 4096 * 10;
    static private final int bufferCount = 3;
    static private final int bytesPerSample = 2;
    static private final byte[] tempBytes = new byte[bufferSize];
    static private final ByteBuffer tempBuffer = Buffers.newByteBuffer(bufferSize);
    protected final Fi file;
    private final ALAudio audio;
    protected int bufferOverhead = 0;
    private FloatArray renderedSecondsQueue = new FloatArray(bufferCount);
    private IntBuffer buffers;
    private int sourceID = -1;
    private int format, sampleRate;
    private boolean isLooping, isPlaying;
    private float volume = 1;
    private float pan = 0;
    private float renderedSeconds, maxSecondsPerBuffer;
    private OnCompletionListener onCompletionListener;

    public ALMusic(ALAudio audio, Fi file){
        this.audio = audio;
        this.file = file;
        this.onCompletionListener = null;
    }

    protected void setup(int channels, int sampleRate){
        this.format = channels > 1 ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16;
        this.sampleRate = sampleRate;
        maxSecondsPerBuffer = (float)(bufferSize - bufferOverhead) / (bytesPerSample * channels * sampleRate);
    }

    @Override
    public void play(){

        if(audio.noDevice) return;
        if(sourceID == -1){
            sourceID = audio.obtainSource(true);
            if(sourceID == -1) return;

            audio.music.add(this);

            if(buffers == null){
                buffers = Buffers.newIntBuffer(bufferCount);
                alGenBuffers(buffers.remaining(), buffers);
                int errorCode = alGetError();
                if(errorCode != AL_NO_ERROR)
                    throw new ArcRuntimeException("Unable to allocate audio buffers. AL Error: " + errorCode);
            }

            alSourcei(sourceID, AL_LOOPING, AL_FALSE);
            setPan(pan, volume);

            boolean filled = false; // Check if there's anything to actually play.
            for(int i = 0; i < bufferCount; i++){
                int bufferID = buffers.get(i);
                if(!fill(bufferID)) break;
                filled = true;
                alSourceQueueBuffers(sourceID, bufferID);
            }
            if(!filled && onCompletionListener != null) onCompletionListener.complete(this);

            if(alGetError() != AL_NO_ERROR){
                stop();
                return;
            }
        }
        if(!isPlaying){
            alSourcePlay(sourceID);
            isPlaying = true;
        }
    }

    @Override
    public void stop(){
        if(audio.noDevice) return;
        if(sourceID == -1) return;
        audio.music.remove(this, true);
        reset();
        audio.freeSource(sourceID);
        sourceID = -1;
        renderedSeconds = 0;
        renderedSecondsQueue.clear();
        isPlaying = false;
    }

    @Override
    public void pause(){
        if(audio.noDevice) return;
        if(sourceID != -1) alSourcePause(sourceID);
        isPlaying = false;
    }

    @Override
    public boolean isPlaying(){
        if(audio.noDevice) return false;
        if(sourceID == -1) return false;
        return isPlaying;
    }

    @Override
    public boolean isLooping(){
        return isLooping;
    }

    @Override
    public void setLooping(boolean isLooping){
        this.isLooping = isLooping;
    }

    @Override
    public float getVolume(){
        return this.volume;
    }

    @Override
    public void setVolume(float volume){
        this.volume = volume;
        if(audio.noDevice) return;
        if(sourceID != -1) alSourcef(sourceID, AL_GAIN, volume);
    }

    @Override
    public void setPan(float pan, float volume){
        this.volume = volume;
        this.pan = pan;
        if(audio.noDevice) return;
        if(sourceID == -1) return;
        alSource3f(sourceID, AL_POSITION, Mathf.cos((pan - 1) * Mathf.PI / 2), 0,
        Mathf.sin((pan + 1) * Mathf.PI / 2));
        alSourcef(sourceID, AL_GAIN, volume);
    }

    @Override
    public float getPosition(){
        if(audio.noDevice) return 0;
        if(sourceID == -1) return 0;
        return renderedSeconds + alGetSourcef(sourceID, AL_SEC_OFFSET);
    }

    @Override
    public void setPosition(float position){
        if(audio.noDevice) return;
        if(sourceID == -1) return;
        boolean wasPlaying = isPlaying;
        isPlaying = false;
        alSourceStop(sourceID);
        alSourceUnqueueBuffers(sourceID, buffers);
        while(renderedSecondsQueue.size > 0){
            renderedSeconds = renderedSecondsQueue.pop();
        }
        if(position <= renderedSeconds){
            reset();
            renderedSeconds = 0;
        }
        while(renderedSeconds < (position - maxSecondsPerBuffer)){
            if(read(tempBytes) <= 0) break;
            renderedSeconds += maxSecondsPerBuffer;
        }
        renderedSecondsQueue.add(renderedSeconds);
        boolean filled = false;
        for(int i = 0; i < bufferCount; i++){
            int bufferID = buffers.get(i);
            if(!fill(bufferID)) break;
            filled = true;
            alSourceQueueBuffers(sourceID, bufferID);
        }
        renderedSecondsQueue.pop();
        if(!filled){
            stop();
            if(onCompletionListener != null) onCompletionListener.complete(this);
        }
        alSourcef(sourceID, AL_SEC_OFFSET, position - renderedSeconds);
        if(wasPlaying){
            alSourcePlay(sourceID);
            isPlaying = true;
        }
    }

    /**
     * Fills as much of the buffer as possible and returns the number of bytes filled. Returns <= 0 to indicate the end of the
     * stream.
     */
    abstract public int read(byte[] buffer);

    /** Resets the stream to the beginning. */
    abstract public void reset();

    /** By default, does just the same as reset(). Used to add special behaviour in Ogg.Music. */
    protected void loop(){
        reset();
    }

    public int getChannels(){
        return format == AL_FORMAT_STEREO16 ? 2 : 1;
    }

    public int getRate(){
        return sampleRate;
    }

    public void update(){
        if(audio.noDevice) return;
        if(sourceID == -1) return;

        boolean end = false;
        int buffers = alGetSourcei(sourceID, AL_BUFFERS_PROCESSED);
        while(buffers-- > 0){
            int bufferID = alSourceUnqueueBuffers(sourceID);
            if(bufferID == AL_INVALID_VALUE) break;
            if(renderedSecondsQueue.size > 0) renderedSeconds = renderedSecondsQueue.pop();
            if(end) continue;
            if(fill(bufferID))
                alSourceQueueBuffers(sourceID, bufferID);
            else
                end = true;
        }
        if(end && alGetSourcei(sourceID, AL_BUFFERS_QUEUED) == 0){
            stop();
            if(onCompletionListener != null) onCompletionListener.complete(this);
        }

        // A buffer underflow will cause the source to stop.
        if(isPlaying && alGetSourcei(sourceID, AL_SOURCE_STATE) != AL_PLAYING) alSourcePlay(sourceID);
    }

    private boolean fill(int bufferID){
        tempBuffer.clear();
        int length = read(tempBytes);
        if(length <= 0){
            if(isLooping){
                loop();
                length = read(tempBytes);
                if(length <= 0) return false;
                if(renderedSecondsQueue.size > 0){
                    renderedSecondsQueue.set(0, 0);
                }
            }else
                return false;
        }
        float previousLoadedSeconds = renderedSecondsQueue.size > 0 ? renderedSecondsQueue.first() : 0;
        float currentBufferSeconds = maxSecondsPerBuffer * (float)length / (float)bufferSize;
        renderedSecondsQueue.insert(0, previousLoadedSeconds + currentBufferSeconds);

        tempBuffer.put(tempBytes, 0, length).flip();
        alBufferData(bufferID, format, tempBuffer, tempBuffer.remaining(), sampleRate);
        return true;
    }

    @Override
    public void dispose(){
        stop();
        if(audio.noDevice) return;
        if(buffers == null) return;
        alDeleteBuffers(buffers);
        buffers = null;
        onCompletionListener = null;
    }

    @Override
    public void setCompletionListener(OnCompletionListener listener){
        onCompletionListener = listener;
    }

    public int getSourceId(){
        return sourceID;
    }
}
