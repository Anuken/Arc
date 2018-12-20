package io.anuke.arc.assets;

public class AssetLoaderParameters<T>{

    public LoadedCallback loadedCallback;

    /**
     * Callback interface that will be invoked when the {@link AssetManager} loaded an asset.
     * @author mzechner
     */
    public interface LoadedCallback{
        void finishedLoading(AssetManager assetManager, String fileName, Class type);
    }
}
