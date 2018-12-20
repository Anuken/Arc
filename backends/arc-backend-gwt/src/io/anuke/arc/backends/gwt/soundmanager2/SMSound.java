package io.anuke.arc.backends.gwt.soundmanager2;

import com.google.gwt.core.client.JavaScriptObject;

public class SMSound{

    /** Constants for play state. */
    public static final int STOPPED = 0;
    public static final int PLAYING = 1;
    private JavaScriptObject jsSound;

    protected SMSound(JavaScriptObject jsSound){
        this.jsSound = jsSound;
    }

    /** Stops, unloads and destroys a sound, freeing resources etc. */
    public native final void destruct() /*-{
		this.@io.anuke.arc.backends.gwt.soundmanager2.SMSound::jsSound.destruct();
	}-*/;

    /**
     * The current location of the "play head" within the sound, specified in milliseconds (1 sec = 1000 msec).
     * @return The current playing position of the sound.
     */
    public native final int getPosition() /*-{
		return this.@io.anuke.arc.backends.gwt.soundmanager2.SMSound::jsSound.position;
	}-*/;

    /**
     * Seeks to a given position within a sound, specified by miliseconds (1000 msec = 1 second.) Affects position property.
     * @param position the position to seek to.
     */
    public native final void setPosition(int position) /*-{
		this.@io.anuke.arc.backends.gwt.soundmanager2.SMSound::jsSound.setPosition(position);
	}-*/;

    /** Pauses the given sound. (Does not toggle.) Affects paused property (boolean.) */
    public native final void pause() /*-{
		this.@io.anuke.arc.backends.gwt.soundmanager2.SMSound::jsSound.pause();
	}-*/;

    /** Starts playing the given sound. */
    public native final void play(SMSoundOptions options) /*-{
		this.@io.anuke.arc.backends.gwt.soundmanager2.SMSound::jsSound.play(
			{
				volume: options.@io.anuke.arc.backends.gwt.soundmanager2.SMSoundOptions::volume,
				pan: options.@io.anuke.arc.backends.gwt.soundmanager2.SMSoundOptions::pan,
				loops: options.@io.anuke.arc.backends.gwt.soundmanager2.SMSoundOptions::loops,
				from: options.@io.anuke.arc.backends.gwt.soundmanager2.SMSoundOptions::from,
				onfinish: function() {
					var callback = options.@io.anuke.arc.backends.gwt.soundmanager2.SMSoundOptions::callback;
					if(callback != null) {
						callback.@io.anuke.arc.backends.gwt.soundmanager2.SMSound.SMSoundCallback::onfinish()();
					}
				}
			}
		);
	}-*/;

    /** Starts playing the given sound. */
    public native final void play() /*-{
		this.@io.anuke.arc.backends.gwt.soundmanager2.SMSound::jsSound.play();
	}-*/;

    /** Resumes the currently-paused sound. Does not affect currently-playing sounds. */
    public native final void resume() /*-{
		this.@io.anuke.arc.backends.gwt.soundmanager2.SMSound::jsSound.resume();
	}-*/;

    /** Stops playing the given sound. */
    public native final void stop() /*-{
		this.@io.anuke.arc.backends.gwt.soundmanager2.SMSound::jsSound.stop();
	}-*/;

    /**
     * Gets the volume of the give sound.
     * @return the volume as a value between 0-100.
     */
    public native final int getVolume() /*-{
		return this.@io.anuke.arc.backends.gwt.soundmanager2.SMSound::jsSound.volume;
	}-*/;

    /**
     * Sets the volume of the given sound. Affects volume property.
     * @param volume the volume, accepted values: 0-100.
     */
    public native final void setVolume(int volume) /*-{
		this.@io.anuke.arc.backends.gwt.soundmanager2.SMSound::jsSound.setVolume(volume);
	}-*/;

    /**
     * Gets the pan of the give sound.
     * @return the pan as a value between -100-100. (L/R, 0 = center.)
     */
    public native final int getPan() /*-{
		return this.@io.anuke.arc.backends.gwt.soundmanager2.SMSound::jsSound.pan;
	}-*/;

    /**
     * Sets the stereo pan (left/right bias) of the given sound. Affects pan property.
     * @param pan the panning amount, accepted values: -100 to 100 (L/R, 0 = center.)
     */
    public native final void setPan(int pan) /*-{
		this.@io.anuke.arc.backends.gwt.soundmanager2.SMSound::jsSound.setPan(pan);
	}-*/;

    /**
     * Numeric value indicating the current playing state of the sound.
     * 0 = stopped/uninitialised.
     * 1 = playing or buffering sound (play has been called, waiting for data etc.).
     * Note that a 1 may not always guarantee that sound is being heard, given buffering and autoPlay status.
     * @return the current playing state.
     */
    public native final int getPlayState() /*-{
		return this.@io.anuke.arc.backends.gwt.soundmanager2.SMSound::jsSound.playState;
	}-*/;

    /** Boolean indicating pause status. True/False. */
    public native final boolean getPaused() /*-{
		return this.@io.anuke.arc.backends.gwt.soundmanager2.SMSound::jsSound.paused;
	}-*/;

    /** Number of times to loop the sound. */
    public native final int getLoops() /*-{
		return this.@io.anuke.arc.backends.gwt.soundmanager2.SMSound::jsSound.loops;
	}-*/;

    public interface SMSoundCallback{
        void onfinish();
    }
}