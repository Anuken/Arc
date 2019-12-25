package arc.backend.teavm;

import arc.audio.*;
import arc.struct.*;

public class TeaSound implements Sound{
    private TeaFi file;
    private IntMap<TeaMusic> instances = new IntMap<>();
    private int nextId;

    public TeaSound(TeaFi file){
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
        final TeaMusic instance = new TeaMusic(file);
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
        for(TeaMusic music : instances.values()){
            music.dispose();
        }
        instances.clear();
    }

    @Override
    public void pause(){
        for(TeaMusic music : instances.values()){
            music.pause();
        }
    }

    @Override
    public void resume(){
        for(TeaMusic music : instances.values()){
            music.play();
        }
    }

    @Override
    public void dispose(){
        stop();
    }

    @Override
    public void stop(int soundId){
        TeaMusic music = instances.get(soundId);
        if(music != null){
            music.stop();
        }
    }

    @Override
    public void pause(int soundId){
        TeaMusic music = instances.get(soundId);
        if(music != null){
            music.pause();
        }
    }

    @Override
    public void resume(int soundId){
        TeaMusic music = instances.get(soundId);
        if(music != null){
            music.play();
        }
    }

    @Override
    public void setLooping(int soundId, boolean looping){
        TeaMusic music = instances.get(soundId);
        if(music != null){
            music.setLooping(looping);
        }
    }

    @Override
    public void setPitch(int soundId, float pitch){
    }

    @Override
    public void setVolume(int soundId, float volume){
        TeaMusic music = instances.get(soundId);
        if(music != null){
            music.setVolume(volume);
        }
    }

    @Override
    public void setPan(int soundId, float pan, float volume){
        TeaMusic music = instances.get(soundId);
        if(music != null){
            music.setPan(pan, volume);
        }
    }
}
