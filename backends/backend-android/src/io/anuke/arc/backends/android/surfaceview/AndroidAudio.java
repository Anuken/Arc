package io.anuke.arc.backends.android.surfaceview;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import io.anuke.arc.Audio;
import io.anuke.arc.Files.FileType;
import io.anuke.arc.audio.AudioDevice;
import io.anuke.arc.audio.AudioRecorder;
import io.anuke.arc.audio.Music;
import io.anuke.arc.audio.Sound;
import io.anuke.arc.files.FileHandle;
import io.anuke.arc.util.ArcRuntimeException;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of the {@link Audio} interface for Android.
 * @author mzechner
 */
@SuppressWarnings("deprecation")
public final class AndroidAudio implements Audio{
    protected final List<AndroidMusic> musics = new ArrayList<AndroidMusic>();
    private final SoundPool soundPool;
    private final AudioManager manager;

    public AndroidAudio(Context context, AndroidApplicationConfiguration config){
        if(!config.disableAudio){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                AudioAttributes audioAttrib = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
                soundPool = new SoundPool.Builder().setAudioAttributes(audioAttrib).setMaxStreams(config.maxSimultaneousSounds).build();
            }else{
                soundPool = new SoundPool(config.maxSimultaneousSounds, AudioManager.STREAM_MUSIC, 0);// srcQuality: the sample-rate converter quality. Currently has no effect. Use 0 for the default.
            }
            manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            if(context instanceof Activity){
                ((Activity)context).setVolumeControlStream(AudioManager.STREAM_MUSIC);
            }
        }else{
            soundPool = null;
            manager = null;
        }
    }

    protected void pause(){
        if(soundPool == null){
            return;
        }
        synchronized(musics){
            for(AndroidMusic music : musics){
                if(music.isPlaying()){
                    music.pause();
                    music.wasPlaying = true;
                }else
                    music.wasPlaying = false;
            }
        }
        this.soundPool.autoPause();
    }

    protected void resume(){
        if(soundPool == null){
            return;
        }
        synchronized(musics){
            for(int i = 0; i < musics.size(); i++){
                if(musics.get(i).wasPlaying) musics.get(i).play();
            }
        }
        this.soundPool.autoResume();
    }

    /** {@inheritDoc} */
    @Override
    public AudioDevice newAudioDevice(int samplingRate, boolean isMono){
        if(soundPool == null){
            throw new ArcRuntimeException("Android audio is not enabled by the application config.");
        }
        return new AndroidAudioDevice(samplingRate, isMono);
    }

    /** {@inheritDoc} */
    @Override
    public Music newMusic(FileHandle file){
        if(soundPool == null){
            throw new ArcRuntimeException("Android audio is not enabled by the application config.");
        }
        AndroidFileHandle aHandle = (AndroidFileHandle)file;

        MediaPlayer mediaPlayer = new MediaPlayer();

        if(aHandle.type() == FileType.Internal){
            try{
                AssetFileDescriptor descriptor = aHandle.getAssetFileDescriptor();
                mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                descriptor.close();
                mediaPlayer.prepare();
                AndroidMusic music = new AndroidMusic(this, mediaPlayer);
                synchronized(musics){
                    musics.add(music);
                }
                return music;
            }catch(Exception ex){
                throw new ArcRuntimeException("Error loading audio file: " + file
                + "\nNote: Internal audio files must be placed in the assets directory.", ex);
            }
        }else{
            try{
                mediaPlayer.setDataSource(aHandle.file().getPath());
                mediaPlayer.prepare();
                AndroidMusic music = new AndroidMusic(this, mediaPlayer);
                synchronized(musics){
                    musics.add(music);
                }
                return music;
            }catch(Exception ex){
                throw new ArcRuntimeException("Error loading audio file: " + file, ex);
            }
        }

    }

    /**
     * Creates a new Music instance from the provided FileDescriptor. It is the caller's responsibility to close the file
     * descriptor. It is safe to do so as soon as this call returns.
     * @param fd the FileDescriptor from which to create the Music
     * @see Audio#newMusic(FileHandle)
     */
    public Music newMusic(FileDescriptor fd){
        if(soundPool == null){
            throw new ArcRuntimeException("Android audio is not enabled by the application config.");
        }

        MediaPlayer mediaPlayer = new MediaPlayer();

        try{
            mediaPlayer.setDataSource(fd);
            mediaPlayer.prepare();

            AndroidMusic music = new AndroidMusic(this, mediaPlayer);
            synchronized(musics){
                musics.add(music);
            }
            return music;
        }catch(Exception ex){
            throw new ArcRuntimeException("Error loading audio from FileDescriptor", ex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Sound newSound(FileHandle file){
        if(soundPool == null){
            throw new ArcRuntimeException("Android audio is not enabled by the application config.");
        }
        AndroidFileHandle aHandle = (AndroidFileHandle)file;
        if(aHandle.type() == FileType.Internal){
            try{
                AssetFileDescriptor descriptor = aHandle.getAssetFileDescriptor();
                AndroidSound sound = new AndroidSound(soundPool, manager, soundPool.load(descriptor, 1));
                descriptor.close();
                return sound;
            }catch(IOException ex){
                throw new ArcRuntimeException("Error loading audio file: " + file
                + "\nNote: Internal audio files must be placed in the assets directory.", ex);
            }
        }else{
            try{
                return new AndroidSound(soundPool, manager, soundPool.load(aHandle.file().getPath(), 1));
            }catch(Exception ex){
                throw new ArcRuntimeException("Error loading audio file: " + file, ex);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public AudioRecorder newAudioRecorder(int samplingRate, boolean isMono){
        if(soundPool == null){
            throw new ArcRuntimeException("Android audio is not enabled by the application config.");
        }
        return new AndroidAudioRecorder(samplingRate, isMono);
    }

    /** Kills the soundpool and all other resources */
    public void dispose(){
        if(soundPool == null){
            return;
        }
        synchronized(musics){
            // gah i hate myself.... music.dispose() removes the music from the list...
            ArrayList<AndroidMusic> musicsCopy = new ArrayList<AndroidMusic>(musics);
            for(AndroidMusic music : musicsCopy){
                music.dispose();
            }
        }
        soundPool.release();
    }
}
