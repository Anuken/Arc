package io.anuke.arc.backends.teavm;

import io.anuke.arc.audio.*;
import io.anuke.arc.collection.*;

public class TeaVMSound implements Sound{
    private TeaVMFi file;
    private IntMap<TeaVMMusic> instances = new IntMap<>();
    private int nextId;

    public TeaVMSound(TeaVMFi file){
        this.file = file;
    }

    @Override
    public int play(float volume, float pitch, float pan){
        return play(volume, pitch, pan, false);
    }

    @Override
    public int loop(float volume, float pitch, float pan){
        return play(volume, pitch, pan, true);
    }

    private int play(float volume, float pitch, float pan, boolean loop){
        final int id = nextId++;
        final TeaVMMusic instance = new TeaVMMusic(file);
        instance.setVolume(volume);
        instance.setPan(pan, volume);
        instance.setLooping(loop);
        instance.setCompletionListener(music -> {
            instances.remove(id);
            instance.dispose();
        });
        instances.put(id, instance);
        instance.play();
        return id;
    }

    @Override
    public void stop(){
        for(TeaVMMusic music : instances.values()){
            music.dispose();
        }
        instances.clear();
    }

    @Override
    public void pause(){
        for(TeaVMMusic music : instances.values()){
            music.pause();
        }
    }

    @Override
    public void resume(){
        for(TeaVMMusic music : instances.values()){
            music.play();
        }
    }

    @Override
    public void dispose(){
        stop();
    }

    @Override
    public void stop(int soundId){
        TeaVMMusic music = instances.get(soundId);
        if(music != null){
            music.stop();
        }
    }

    @Override
    public void pause(int soundId){
        TeaVMMusic music = instances.get(soundId);
        if(music != null){
            music.pause();
        }
    }

    @Override
    public void resume(int soundId){
        TeaVMMusic music = instances.get(soundId);
        if(music != null){
            music.play();
        }
    }

    @Override
    public void setLooping(int soundId, boolean looping){
        TeaVMMusic music = instances.get(soundId);
        if(music != null){
            music.setLooping(looping);
        }
    }

    @Override
    public void setPitch(int soundId, float pitch){
    }

    @Override
    public void setVolume(int soundId, float volume){
        TeaVMMusic music = instances.get(soundId);
        if(music != null){
            music.setVolume(volume);
        }
    }

    @Override
    public void setPan(int soundId, float pan, float volume){
        TeaVMMusic music = instances.get(soundId);
        if(music != null){
            music.setPan(pan, volume);
        }
    }
}
