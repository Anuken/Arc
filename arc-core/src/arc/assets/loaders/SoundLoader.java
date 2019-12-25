package arc.assets.loaders;

import arc.Core;
import arc.assets.AssetDescriptor;
import arc.assets.AssetLoaderParameters;
import arc.assets.AssetManager;
import arc.audio.Sound;
import arc.struct.Array;
import arc.files.Fi;

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
    public void loadAsync(AssetManager manager, String fileName, Fi file, SoundParameter parameter){
        sound = Core.audio.newSound(file);
    }

    @Override
    public Sound loadSync(AssetManager manager, String fileName, Fi file, SoundParameter parameter){
        Sound sound = this.sound;
        this.sound = null;
        return sound;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, Fi file, SoundParameter parameter){
        return null;
    }

    public static class SoundParameter extends AssetLoaderParameters<Sound>{
        public SoundParameter(){
        }

        public SoundParameter(LoadedCallback loadedCallback){
            super(loadedCallback);
        }
    }

}
