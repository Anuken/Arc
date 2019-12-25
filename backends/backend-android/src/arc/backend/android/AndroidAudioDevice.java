package arc.backend.android;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import arc.audio.AudioDevice;

/**
 * Implementation of the {@link AudioDevice} interface for Android using the AudioTrack class. You will need to set the permission
 * android.permission.RECORD_AUDIO in your manifest file.
 * @author mzechner
 */
class AndroidAudioDevice implements AudioDevice{
    /** the audio track **/
    private final AudioTrack track;
    /** whether this device is in mono or stereo mode **/
    private final boolean isMono;
    /** the latency in samples **/
    private final int latency;
    /** the mighty buffer **/
    private short[] buffer = new short[1024];

    AndroidAudioDevice(int samplingRate, boolean isMono){
        this.isMono = isMono;
        int minSize = AudioTrack.getMinBufferSize(samplingRate, isMono ? AudioFormat.CHANNEL_OUT_MONO
        : AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        track = new AudioTrack(AudioManager.STREAM_MUSIC, samplingRate, isMono ? AudioFormat.CHANNEL_OUT_MONO
        : AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, minSize, AudioTrack.MODE_STREAM);
        track.play();
        latency = minSize / (isMono ? 1 : 2);
    }

    @Override
    public void dispose(){
        track.stop();
        track.release();
    }

    @Override
    public boolean isMono(){
        return isMono;
    }

    @Override
    public void writeSamples(short[] samples, int offset, int numSamples){
        int writtenSamples = track.write(samples, offset, numSamples);
        while(writtenSamples != numSamples)
            writtenSamples += track.write(samples, offset + writtenSamples, numSamples - writtenSamples);
    }

    @Override
    public void writeSamples(float[] samples, int offset, int numSamples){
        if(buffer.length < samples.length) buffer = new short[samples.length];

        int bound = offset + numSamples;
        for(int i = offset, j = 0; i < bound; i++, j++){
            float fValue = samples[i];
            if(fValue > 1) fValue = 1;
            if(fValue < -1) fValue = -1;
            short value = (short)(fValue * Short.MAX_VALUE);
            buffer[j] = value;
        }

        int writtenSamples = track.write(buffer, 0, numSamples);
        while(writtenSamples != numSamples)
            writtenSamples += track.write(buffer, writtenSamples, numSamples - writtenSamples);
    }

    @Override
    public int getLatency(){
        return latency;
    }

    @Override

    public void setVolume(float volume){
        track.setVolume(volume);
    }
}
