package arc.backend.android;

import android.media.MediaPlayer;
import arc.Core;
import arc.audio.Music;
import arc.util.Log;

import java.io.IOException;

public class AndroidMusic implements Music, MediaPlayer.OnCompletionListener{
    private final AndroidAudio audio;
    protected boolean wasPlaying = false;
    protected OnCompletionListener onCompletionListener;
    private MediaPlayer player;
    private boolean isPrepared = true;
    private float volume = 1f;

    AndroidMusic(AndroidAudio audio, MediaPlayer player){
        this.audio = audio;
        this.player = player;
        this.onCompletionListener = null;
        this.player.setOnCompletionListener(this);
    }

    @Override
    public void dispose(){
        if(player == null) return;
        try{
            player.release();
        }catch(Throwable t){
            Log.infoTag("AndroidMusic", "error while disposing AndroidMusic instance, non-fatal");
        }finally{
            player = null;
            onCompletionListener = null;
            synchronized(audio.musics){
                audio.musics.remove(this);
            }
        }
    }

    @Override
    public boolean isLooping(){
        if(player == null) return false;
        try{
            return player.isLooping();
        }catch(Exception e){
            // NOTE: isLooping() can potentially throw an exception and crash the application
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void setLooping(boolean isLooping){
        if(player == null) return;
        player.setLooping(isLooping);
    }

    @Override
    public boolean isPlaying(){
        if(player == null) return false;
        try{
            return player.isPlaying();
        }catch(Exception e){
            // NOTE: isPlaying() can potentially throw an exception and crash the application
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void pause(){
        if(player == null) return;
        try{
            if(player.isPlaying()){
                player.pause();
            }
        }catch(Exception e){
            // NOTE: isPlaying() can potentially throw an exception and crash the application
            e.printStackTrace();
        }
        wasPlaying = false;
    }

    @Override
    public void play(){
        if(player == null) return;
        try{
            if(player.isPlaying()) return;
        }catch(Exception e){
            // NOTE: isPlaying() can potentially throw an exception and crash the application
            e.printStackTrace();
            return;
        }

        try{
            if(!isPrepared){
                player.prepare();
                isPrepared = true;
            }
            player.start();
        }catch(IllegalStateException | IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public float getVolume(){
        return volume;
    }

    @Override
    public void setVolume(float volume){
        if(player == null) return;
        player.setVolume(volume, volume);
        this.volume = volume;
    }

    @Override
    public void setPan(float pan, float volume){
        if(player == null) return;
        float leftVolume = volume;
        float rightVolume = volume;

        if(pan < 0){
            rightVolume *= (1 - Math.abs(pan));
        }else if(pan > 0){
            leftVolume *= (1 - Math.abs(pan));
        }

        player.setVolume(leftVolume, rightVolume);
        this.volume = volume;
    }

    @Override
    public void stop(){
        if(player == null) return;
        if(isPrepared){
            player.seekTo(0);
        }
        player.stop();
        isPrepared = false;
    }

    @Override
    public float getPosition(){
        if(player == null) return 0.0f;
        return player.getCurrentPosition() / 1000f;
    }

    public void setPosition(float position){
        if(player == null) return;
        try{
            if(!isPrepared){
                player.prepare();
                isPrepared = true;
            }
            player.seekTo((int)(position * 1000));
        }catch(IllegalStateException | IOException e){
            e.printStackTrace();
        }
    }

    public float getDuration(){
        if(player == null) return 0.0f;
        return player.getDuration() / 1000f;
    }

    @Override
    public void setCompletionListener(OnCompletionListener listener){
        onCompletionListener = listener;
    }

    @Override
    public void onCompletion(MediaPlayer mp){
        if(onCompletionListener != null){
            Core.app.post(() -> onCompletionListener.complete(AndroidMusic.this));
        }
    }

}
