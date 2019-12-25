package arc.backend.android;

import android.app.*;
import android.content.*;
import android.content.res.*;
import android.media.*;
import android.os.*;
import arc.*;
import arc.Files.*;
import arc.audio.*;
import arc.files.*;
import arc.util.TaskQueue;
import arc.util.*;
import arc.util.async.*;

import java.io.*;
import java.util.*;

/**
 * An implementation of the {@link Audio} interface for Android.
 * @author mzechner
 */
@SuppressWarnings("deprecation")
public final class AndroidAudio extends Audio implements Runnable{
    private static final int updateInterval = 60;
    protected final List<AndroidMusic> musics = new ArrayList<>();
    private final SoundPool soundPool;
    private final AudioManager manager;
    private final int maxSounds;

    final Thread thread;
    final TaskQueue queue = new TaskQueue();

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

            maxSounds = config.maxSimultaneousSounds;
            thread = Threads.daemon(this);
        }else{
            maxSounds = 0;
            thread = null;
            soundPool = null;
            manager = null;
        }
    }

    @Override
    public void run(){
        while(true){
            try{
                synchronized(soundPool){
                    queue.run();
                }
                Thread.sleep(1000 / updateInterval);
            }catch(InterruptedException e){
                return;
            }catch(Exception e){
                Core.app.post(() -> {
                    throw new RuntimeException(e);
                });
            }
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
        synchronized(soundPool){
            this.soundPool.autoPause();
        }
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
        synchronized(soundPool){
            this.soundPool.autoResume();
        }
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
    public Music newMusic(Fi file){
        if(soundPool == null){
            throw new ArcRuntimeException("Android audio is not enabled by the application config.");
        }
        AndroidFi aHandle = (AndroidFi)file;

        MediaPlayer mediaPlayer = new MediaPlayer();

        if(aHandle.type() == FileType.internal){
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
     * @see Audio#newMusic(Fi)
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
    public Sound newSound(Fi file){
        if(soundPool == null){
            throw new ArcRuntimeException("Android audio is not enabled by the application config.");
        }
        //make sure the file is of type AndroidFileHandle, and if not, make it so
        if(!(file instanceof AndroidFi)){
            Fi destination = Core.files.local("__android_audio__").child(file.name());
            if(!destination.exists() || destination.length() != file.length()){
                file.copyTo(destination);
            }
            file = destination;
        }

        AndroidFi aHandle = (AndroidFi)file;
        if(aHandle.type() == FileType.internal){
            try{
                AssetFileDescriptor descriptor = aHandle.getAssetFileDescriptor();
                AndroidSound sound = new AndroidSound(soundPool, manager, queue, maxSounds, soundPool.load(descriptor, 1));
                descriptor.close();
                return sound;
            }catch(IOException ex){
                throw new ArcRuntimeException("Error loading audio file: " + file
                + "\nNote: Internal audio files must be placed in the assets directory.", ex);
            }
        }else{
            try{
                return new AndroidSound(soundPool, manager, queue, maxSounds, soundPool.load(aHandle.file().getPath(), 1));
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
            ArrayList<AndroidMusic> musicsCopy = new ArrayList<>(musics);
            for(AndroidMusic music : musicsCopy){
                music.dispose();
            }
        }
        if(thread != null){
            thread.interrupt();
        }
        soundPool.release();
    }
}
