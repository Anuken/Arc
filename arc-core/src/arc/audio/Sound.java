package arc.audio;

import arc.*;
import arc.files.*;
import arc.math.*;
import arc.math.geom.*;

import static arc.audio.Soloud.*;

/**
 * <p>
 * A Sound is a short audio clip that can be played numerous times in parallel. It's completely loaded into memory so only load
 * small audio files. Call the {@link #dispose()} method when you're done using the Sound.
 * </p>
 *
 * <p>
 * Sound instances are created via a call to {@link Audio#newSound(Fi)}.
 * </p>
 *
 * <p>
 * Calling the {@link #play()} or {@link #play(float)} method will return a long which is an id to that instance of the sound. You
 * can use this id to modify the playback of that sound instance.
 * </p>
 *
 * <p>
 * <b>Note</b>: any values provided will not be clamped, it is the developer's responsibility to do so
 * </p>
 */
public class Sound extends AudioSource{
    public AudioBus bus = Core.audio == null ? null : Core.audio.soundBus;

    long framePlayed;
    Fi file;

    /** Creates an empty sound. This sound cannot be played until it is loaded. */
    public Sound(){

    }

    /** Loads a sound from a file. */
    public Sound(Fi file){
        load(file);
    }

    public void load(Fi file){
        byte[] data = file.readBytes();
        this.file = file;
        handle = wavLoad(data, data.length);
    }

    /**
     * Plays the sound. If the sound is already playing, it will be played again, concurrently.
     * @param volume the volume in the range [0,1]
     * @param pitch the pitch multiplier, 1 == default, >1 == faster, <1 == slower, the value has to be between 0.5 and 2.0
     * @param pan panning in the range -1 (full left) to 1 (full right). 0 is center position.
     * @return the id of the sound instance if successful, or -1 on failure.
     */
    public int play(float volume, float pitch, float pan, boolean loop){
        if(handle == 0 || framePlayed == Core.graphics.getFrameId() || bus == null || !Core.audio.initialized) return -1;
        framePlayed = Core.graphics.getFrameId();
        return sourcePlayBus(handle, bus.handle, volume, pitch, pan, loop);
    }

    /** Sets the bus that will be used for the next play of this SFX. */
    public void setBus(AudioBus bus){
        this.bus = bus;
    }

    public void stop(){
        if(handle == 0) return;
        sourceStop(handle);
    }

    public float calcPan(float x, float y){
        if(Core.app.isHeadless()) return 0f;

        return Mathf.clamp((x - Core.camera.position.x) / (Core.camera.width / 2f), -0.9f, 0.9f);
    }

    public float calcVolume(float x, float y){
        return calcFalloff(x, y) * Core.settings.getInt("sfxvol") / 100f;
    }

    public float calcFalloff(float x, float y){
        if(Core.app.isHeadless()) return 1f;

        float dst = Mathf.dst(x, y, Core.camera.position.x, Core.camera.position.y);
        return Mathf.clamp(1f / (dst * dst / Core.audio.falloff));
    }

    /**
     * Plays this sound at a certain position, with correct panning and volume applied.
     * Automatically uses the "sfxvolume" setting.
     */
    public int at(float x, float y, float pitch){
        return at(x, y, pitch, 1f);
    }

    /**
     * Plays this sound at a certain position, with correct panning and volume applied.
     * Automatically uses the "sfxvolume" setting.
     */
    public int at(float x, float y, float pitch, float volume){
        float vol = calcVolume(x, y) * volume;
        if(vol < 0.01f) return -1; //discard
        return play(vol, pitch, calcPan(x, y));
    }

    /**
     * Plays this sound at a certain position, with correct panning and volume applied.
     * Automatically uses the "sfxvolume" setting.
     */
    public int at(float x, float y){
        return at(x, y, 1f);
    }

    /** Plays #at() with this position. */
    public int at(Position pos){
        return at(pos.getX(), pos.getY());
    }

    /** Plays #at() with this position. */
    public int at(Position pos, float pitch){
        return at(pos.getX(), pos.getY(), pitch);
    }

    /**
     * Plays the sound. If the sound is already playing, it will be played again, concurrently.
     * Automatically uses the "sfxvolume" setting.
     * @return the id of the sound instance if successful, or -1 on failure.
     */
    public int play(){
        return play(1f * Core.settings.getInt("sfxvol") / 100f);
    }

    /**
     * Plays the sound. If the sound is already playing, it will be played again, concurrently.
     * Ignores SFX volume setting.
     * @param volume the volume in the range [0,1]
     * @return the id of the sound instance if successful, or -1 on failure.
     */
    public int play(float volume){
        return play(volume, 1f, 0f);
    }

    /**
     * Plays the sound. If the sound is already playing, it will be played again, concurrently.
     * @param volume the volume in the range [0,1]
     * @param pitch the pitch multiplier, 1 == default, >1 == faster, <1 == slower, the value has to be between 0.5 and 2.0
     * @param pan panning in the range -1 (full left) to 1 (full right). 0 is center position.
     * @return the id of the sound instance if successful, or -1 on failure.
     */
    public int play(float volume, float pitch, float pan){
        return play(volume, pitch, pan, false);
    }

    /**
     * Plays the sound, looping. If the sound is already playing, it will be played again, concurrently.
     * @return the id of the sound instance if successful, or -1 on failure.
     */
    public int loop(){
        return loop(1f);
    }

    /**
     * Plays the sound, looping. If the sound is already playing, it will be played again, concurrently.
     * @param volume the volume in the range [0, 1]
     * @return the id of the sound instance if successful, or -1 on failure.
     */
    public int loop(float volume){
        return loop(volume, 1f, 0f);
    }

    /**
     * Plays the sound, looping. If the sound is already playing, it will be played again, concurrently.
     * @param volume the volume in the range [0,1]
     * @param pitch the pitch multiplier, 1 == default, >1 == faster, <1 == slower, the value has to be between 0.5 and 2.0
     * @param pan panning in the range -1 (full left) to 1 (full right). 0 is center position.
     * @return the id of the sound instance if successful, or -1 on failure.
     */
    public int loop(float volume, float pitch, float pan){
        return play(volume, pitch, pan, true);
    }

    @Override
    public String toString(){
        return "SoloudSound: " + file;
    }
}
