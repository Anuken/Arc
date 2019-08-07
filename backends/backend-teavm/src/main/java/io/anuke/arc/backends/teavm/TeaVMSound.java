package io.anuke.arc.backends.teavm;

import io.anuke.arc.audio.*;

import java.util.*;

/**
 *
 * @author Alexey Andreev
 */
public class TeaVMSound implements Sound {
    private TeaVMFileHandle file;
    private Map<Long, TeaVMMusic> instances = new HashMap<>();
    private long nextId;
    private float volume = 1;
    private float pitch = 1;
    private float pan = 0.5f;

    public TeaVMSound(TeaVMFileHandle file) {
        this.file = file;
    }

    @Override
    public long play(float volume, float pitch, float pan) {
        return play(volume, pitch, pan, false);
    }

    @Override
    public long loop(float volume, float pitch, float pan) {
        return play(volume, pitch, pan, true);
    }

    private long play(float volume, float pitch, float pan, boolean loop) {
        final long id = nextId++;
        final TeaVMMusic instance = new TeaVMMusic(file);
        instance.setVolume(volume);
        instance.setPan(pan, volume);
        instance.setLooping(loop);
        instance.setOnCompletionListener(music -> {
            instances.remove(id);
            instance.dispose();
        });
        instances.put(id, instance);
        instance.play();
        return id;
    }

    @Override
    public void stop() {
        for (TeaVMMusic music : instances.values()) {
            music.dispose();
        }
        instances.clear();
    }

    @Override
    public void pause() {
        for (TeaVMMusic music : instances.values()) {
            music.pause();
        }
    }

    @Override
    public void resume() {
        for (TeaVMMusic music : instances.values()) {
            music.play();
        }
    }

    @Override
    public void dispose() {
        stop();
    }

    @Override
    public void stop(long soundId) {
        TeaVMMusic music = instances.get(soundId);
        if (music != null) {
            music.stop();
        }
    }

    @Override
    public void pause(long soundId) {
        TeaVMMusic music = instances.get(soundId);
        if (music != null) {
            music.pause();
        }
    }

    @Override
    public void resume(long soundId) {
        TeaVMMusic music = instances.get(soundId);
        if (music != null) {
            music.play();
        }
    }

    @Override
    public void setLooping(long soundId, boolean looping) {
        TeaVMMusic music = instances.get(soundId);
        if (music != null) {
            music.setLooping(looping);
        }
    }

    @Override
    public void setPitch(long soundId, float pitch) {
    }

    @Override
    public void setVolume(long soundId, float volume) {
        TeaVMMusic music = instances.get(soundId);
        if (music != null) {
            music.setVolume(volume);
        }
    }

    @Override
    public void setPan(long soundId, float pan, float volume) {
        TeaVMMusic music = instances.get(soundId);
        if (music != null) {
            music.setPan(pan, volume);
        }
    }
}
