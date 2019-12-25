package arc.backend.android;

import android.media.*;
import arc.audio.*;
import arc.struct.*;
import arc.util.TaskQueue;

import java.util.*;

final class AndroidSound implements Sound{
    private static int priority = 0;

    final SoundPool soundPool;
    final AudioManager manager;
    final int soundId;
    final IntArray streamIds = new IntArray(8);
    final TaskQueue queue;
    final int[] mappedIDs;
    int lastID;

    AndroidSound(SoundPool pool, AudioManager manager, TaskQueue queue, int maxSounds, int soundId){
        this.soundPool = pool;
        this.manager = manager;
        this.soundId = soundId;
        this.queue = queue;
        this.mappedIDs = new int[maxSounds];
        Arrays.fill(mappedIDs, -1);
    }

    private void run(Runnable run){
        if(queue.size() < 22){
            queue.post(run);
        }
    }

    private int map(int i){
        if(i < 0 || i>= mappedIDs.length) return -1;
        return mappedIDs[i];
    }

    @Override
    public void dispose(){
        run(() -> soundPool.unload(soundId));
    }

    @Override
    public void stop(){
        run(() -> {
            for(int i = 0, n = streamIds.size; i < n; i++)
                soundPool.stop(streamIds.get(i));
        });
    }

    @Override
    public void stop(int soundId){
        int mapped = map(soundId);
        if(mapped == -1) return;

        synchronized(soundPool){
            soundPool.stop(mapped);
        }
    }

    @Override
    public void pause(){
        run(soundPool::autoPause);
    }

    @Override
    public void pause(int soundId){
        int mapped = map(soundId);
        if(mapped == -1) return;

        run(() -> soundPool.pause(mapped));
    }

    @Override
    public void resume(){
        run(soundPool::autoResume);
    }

    @Override
    public void resume(int soundId){
        int mapped = map(soundId);
        if(mapped == -1) return;

        run(() -> soundPool.resume(mapped));
    }

    @Override
    public void setPitch(int soundId, float pitch){
        int mapped = map(soundId);
        if(mapped == -1) return;

        run(() -> soundPool.setRate(mapped, pitch));
    }

    @Override
    public void setVolume(int soundId, float volume){
        int mapped = map(soundId);
        if(mapped == -1) return;

        run(() -> soundPool.setVolume(mapped, volume, volume));
    }

    @Override
    public void setLooping(int soundId, boolean looping){
        int mapped = map(soundId);
        if(mapped == -1) return;

        run(() -> soundPool.setLoop(mapped, looping ? -1 : 0));
    }

    @Override
    public void setPan(int soundId, float pan, float volume){
        int mapped = map(soundId);
        if(mapped == -1) return;

        run(() -> {
            float leftVolume = volume;
            float rightVolume = volume;

            if(pan < 0){
                rightVolume *= (1 - Math.abs(pan));
            }else if(pan > 0){
                leftVolume *= (1 - Math.abs(pan));
            }
            soundPool.setVolume(mapped, leftVolume, rightVolume);
        });
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
        if(volume <= 0.001f && !loop) return -1;

        int returnID = (lastID ++) % mappedIDs.length;

        run(() -> {
            if(streamIds.size == 8) streamIds.pop();
            float leftVolume = volume;
            float rightVolume = volume;
            if(pan < 0){
                rightVolume *= (1 - Math.abs(pan));
            }else if(pan > 0){
                leftVolume *= (1 - Math.abs(pan));
            }
            int streamId = soundPool.play(soundId, leftVolume, rightVolume, priority(), loop ? -1 : 0, pitch);
            // standardise error code with other backends
            if(streamId == 0){
                return;
            }
            streamIds.insert(0, streamId);
            mappedIDs[returnID] = streamId;
            //return streamId;
        });

        return returnID;
    }

    int priority(){
        return priority++;
    }
}
