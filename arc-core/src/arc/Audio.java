package arc;

import arc.audio.*;
import arc.files.Fi;
import arc.util.*;

/**
 * This interface encapsulates the creation and management of audio resources. It allows you to get direct access to the audio
 * hardware via the {@link AudioDevice} and {@link AudioRecorder} interfaces, create sound effects via the {@link Sound} interface
 * and play music streams via the {@link Music} interface.
 *
 * <p>
 * All resources created via this interface have to be disposed as soon as they are no longer used.
 * </p>
 *
 * <p>
 * Note that all {@link Music} instances will be automatically paused when the {@link ApplicationListener#pause()} method is
 * called, and automatically resumed when the {@link ApplicationListener#resume()} method is called.
 * </p>
 * @author mzechner
 */
public abstract class Audio implements Disposable{
    /** Falloff when playing audio.*/
    public float falloff = 16000f;

    /**
     * Creates a new {@link AudioDevice} either in mono or stereo mode. The AudioDevice has to be disposed via its
     * {@link AudioDevice#dispose()} method when it is no longer used.
     * @param samplingRate the sampling rate.
     * @param isMono whether the AudioDevice should be in mono or stereo mode
     * @return the AudioDevice
     * @throws ArcRuntimeException in case the device could not be created
     */
    @Nullable
    public AudioDevice newAudioDevice(int samplingRate, boolean isMono){
        return null;
    }

    /**
     * Creates a new {@link AudioRecorder}. The AudioRecorder has to be disposed after it is no longer used.
     * @param samplingRate the sampling rate in Hertz
     * @param isMono whether the recorder records in mono or stereo
     * @return the AudioRecorder
     * @throws ArcRuntimeException in case the recorder could not be created
     */
    @Nullable
    public AudioRecorder newAudioRecorder(int samplingRate, boolean isMono){
        return null;
    }

    /** Sets a -global- filter at a specified index. */
    public void setFilter(int index, @Nullable AudioFilter filter){

    }

    /** Fades a specific sound's filter parameter to a value over several seconds.
     * See SoLoud documentation for details on attributes and filter types. */
    public void fadeFilter(int soundId, int filter, int attribute, float value, float timeSec){

    }

    /**
     * <p>
     * Creates a new {@link Sound} which is used to play back audio effects such as gun shots or explosions. The Sound's audio data
     * is retrieved from the file specified via the {@link Fi}. Note that the complete audio data is loaded into RAM. You
     * should therefore not load big audio files with this methods. The current upper limit for decoded audio is 1 MB.
     * </p>
     *
     * <p>
     * Currently supported formats are WAV, MP3 and OGG. Except OGG, which doesn't actually work on iOS!
     * </p>
     *
     * <p>
     * The Sound has to be disposed if it is no longer used via the {@link Sound#dispose()} method.
     * </p>
     * @return the new Sound
     * @throws ArcRuntimeException in case the sound could not be loaded
     */
    public abstract Sound newSound(Fi file);

    /**
     * Creates a new {@link Music} instance which is used to play back a music stream from a file. Currently supported formats are
     * WAV, MP3 and OGG. The Music instance has to be disposed if it is no longer used via the {@link Music#dispose()} method.
     * Music instances are automatically paused when {@link ApplicationListener#pause()} is called and resumed when
     * {@link ApplicationListener#resume()} is called.
     * @param file the FileHandle
     * @return the new Music or null if the Music could not be loaded
     * @throws ArcRuntimeException in case the music could not be loaded
     */
    public abstract Music newMusic(Fi file);

    @Override
    public void dispose(){

    }
}
