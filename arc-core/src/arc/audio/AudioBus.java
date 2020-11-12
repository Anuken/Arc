package arc.audio;

import arc.*;

import static arc.audio.Soloud.*;

/** A*/
public class AudioBus extends AudioSource{
    protected long handle;

    public int id;

    public AudioBus(){
        if(Core.audio != null && Core.audio.initialized){
            init();
        }
    }

    AudioBus init(){
        if(handle != 0) return this;
        this.handle = busNew();
        this.id = sourcePlay(handle);
        return this;
    }

    public void play(){
        if(handle == 0 || idValid(id)) return;
        id = sourcePlay(handle);
    }

    public void stop(){
        Core.audio.stop(id);
    }

    public void fadeFilterParam(int filter, int attribute, float value, float timeSec){
        Core.audio.fadeFilterParam(id, filter, attribute, value, timeSec);
    }

    public void setFilterParam(int filter, int attribute, float value){
        Core.audio.setFilterParam(id, filter, attribute, value);
    }

    public void setVolume(float volume){
        Core.audio.setVolume(id, volume);
    }
}
