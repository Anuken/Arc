package arc.audio;

import arc.Application;
import arc.ApplicationListener;
import arc.Audio;
import arc.files.Fi;
import arc.util.Disposable;

/**
 * <p>
 * A Music instance represents a streamed audio file. The interface supports pausing, resuming
 * and so on. When you are done with using the Music instance you have to dispose it via the {@link #dispose()} method.
 * </p>
 *
 * <p>
 * Music instances are created via {@link Audio#newMusic(Fi)}.
 * </p>
 *
 * <p>
 * Music instances are automatically paused and resumed when an {@link Application} is paused or resumed. See
 * {@link ApplicationListener}.
 * </p>
 *
 * <p>
 * <b>Note</b>: any values provided will not be clamped, it is the developer's responsibility to do so
 * </p>
 * @author mzechner
 */
public interface Music extends Disposable{
    /**
     * Starts the play back of the music stream. In case the stream was paused this will resume the play back. In case the music
     * stream is finished playing this will restart the play back.
     */
    void play();

    /**
     * Pauses the play back. If the music stream has not been started yet or has finished playing a call to this method will be
     * ignored.
     */
    void pause();

    /** Stops a playing or paused Music instance. Next time play() is invoked the Music will start from the beginning. */
    void stop();

    /** @return whether this music stream is playing */
    boolean isPlaying();

    /** @return whether the music stream is playing. */
    boolean isLooping();

    /**
     * Sets whether the music stream is looping. This can be called at any time, whether the stream is playing.
     * @param isLooping whether to loop the stream
     */
    void setLooping(boolean isLooping);

    /** @return the volume of this music stream. */
    float getVolume();

    /**
     * Sets the volume of this music stream. The volume must be given in the range [0,1] with 0 being silent and 1 being the
     * maximum volume.
     */
    void setVolume(float volume);

    /**
     * Sets the panning and volume of this music stream.
     * @param pan panning in the range -1 (full left) to 1 (full right). 0 is center position.
     * @param volume the volume in the range [0,1].
     */
    void setPan(float pan, float volume);

    /** Returns the playback position in seconds. */
    float getPosition();

    /** Set the playback position in seconds. */
    void setPosition(float position);

    /** Needs to be called when the Music is no longer needed. */
    void dispose();

    /**
     * Register a callback to be invoked when the end of a music stream has been reached during playback.
     * @param listener the callback that will be run.
     */
    void setCompletionListener(OnCompletionListener listener);

    /** Interface definition for a callback to be invoked when playback of a music stream has completed. */
    interface OnCompletionListener{
        /**
         * Called when the end of a media source is reached during playback.
         * @param music the Music that reached the end of the file
         */
        void complete(Music music);
    }
}
