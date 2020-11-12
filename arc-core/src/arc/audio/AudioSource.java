package arc.audio;

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

    @Override
    public void dispose(){
        //TODO does nothing
    }
}
