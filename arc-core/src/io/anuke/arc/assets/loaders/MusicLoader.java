package io.anuke.arc.assets.loaders;

import io.anuke.arc.Core;
import io.anuke.arc.assets.AssetDescriptor;
import io.anuke.arc.assets.AssetLoaderParameters;
import io.anuke.arc.assets.AssetManager;
import io.anuke.arc.audio.Music;
import io.anuke.arc.collection.Array;
import io.anuke.arc.files.Fi;

/**
 * {@link AssetLoader} for {@link Music} instances. The Music instance is loaded synchronously.
 * @author mzechner
 */
public class MusicLoader extends AsynchronousAssetLoader<Music, MusicLoader.MusicParameter>{

    private Music music;

    public MusicLoader(FileHandleResolver resolver){
        super(resolver);
    }

    /**
     * Returns the {@link Music} instance currently loaded by this
     * {@link MusicLoader}.
     * @return the currently loaded {@link Music}, otherwise {@code null} if
     * no {@link Music} has been loaded yet.
     */
    protected Music getLoadedMusic(){
        return music;
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, Fi file, MusicParameter parameter){
        music = Core.audio.newMusic(file);
    }

    @Override
    public Music loadSync(AssetManager manager, String fileName, Fi file, MusicParameter parameter){
        Music music = this.music;
        this.music = null;
        return music;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, Fi file, MusicParameter parameter){
        return null;
    }

    public static class MusicParameter extends AssetLoaderParameters<Music>{
        public MusicParameter(){
        }

        public MusicParameter(LoadedCallback loadedCallback){
            super(loadedCallback);
        }
    }

}
