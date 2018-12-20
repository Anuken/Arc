package io.anuke.arc.assets.loaders;

import io.anuke.arc.Core;
import io.anuke.arc.assets.AssetDescriptor;
import io.anuke.arc.assets.AssetLoaderParameters;
import io.anuke.arc.assets.AssetManager;
import io.anuke.arc.audio.Sound;
import io.anuke.arc.collection.Array;
import io.anuke.arc.files.FileHandle;

/**
 * {@link AssetLoader} to load {@link Sound} instances.
 * @author mzechner
 */
public class SoundLoader extends AsynchronousAssetLoader<Sound, SoundLoader.SoundParameter>{

    private Sound sound;

    public SoundLoader(FileHandleResolver resolver){
        super(resolver);
    }

    /**
     * Returns the {@link Sound} instance currently loaded by this
     * {@link SoundLoader}.
     * @return the currently loaded {@link Sound}, otherwise {@code null} if
     * no {@link Sound} has been loaded yet.
     */
    protected Sound getLoadedSound(){
        return sound;
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, SoundParameter parameter){
        sound = Core.audio.newSound(file);
    }

    @Override
    public Sound loadSync(AssetManager manager, String fileName, FileHandle file, SoundParameter parameter){
        Sound sound = this.sound;
        this.sound = null;
        return sound;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, SoundParameter parameter){
        return null;
    }

    static public class SoundParameter extends AssetLoaderParameters<Sound>{
    }

}
