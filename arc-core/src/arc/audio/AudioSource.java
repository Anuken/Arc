package arc.audio;

import arc.*;
import arc.util.*;

import static arc.audio.Soloud.*;

public abstract class AudioSource implements Disposable{
    protected long handle;

    public void setFilter(int index, @Nullable AudioFilter filter){
        if(handle == 0) return;
        sourceFilter(handle, index, filter == null ? 0 : filter.handle);
    }

    public void setFilter(@Nullable AudioFilter filter){
        setFilter(0, filter);
    }

    /** Sets the priority of this source. Sources with higher priorities will not get cut off by those of lower priorities. */
    public void setPriority(float priority){
        if(handle == 0) return;
        sourcePriority(handle, priority);
    }

    /** Sets the priority of this source. Sources with higher priorities will not get cut off by those of lower priorities. */
    public void setMaxConcurrent(int max){
        if(handle == 0) return;
        sourceMaxConcurrent(handle, max);
    }

    /** Sets the minimum playtime (in seconds) that a sound must have in order to be interrupted when its concurrent limit is reached. */
    public void setMinConcurrentInterrupt(float seconds){
        if(handle == 0) return;
        sourceMinConcurrentInterrupt(handle, seconds);
    }

    /** @return number of currently playing instances */
    public int countPlaying(){
        if(handle == 0) return  0;
        return Core.audio.countPlaying(this);
    }

    public void setSingleInstance(boolean singleInstance){
        if(handle == 0 || !Core.audio.initialized) return;
        sourceSingleInstance(handle, singleInstance);
    }

    @Override
    public void dispose(){
        //TODO does nothing
    }
}
