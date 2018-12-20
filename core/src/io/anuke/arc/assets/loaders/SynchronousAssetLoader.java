package io.anuke.arc.assets.loaders;

import io.anuke.arc.assets.AssetLoaderParameters;
import io.anuke.arc.assets.AssetManager;
import io.anuke.arc.files.FileHandle;

public abstract class SynchronousAssetLoader<T, P extends AssetLoaderParameters<T>> extends AssetLoader<T, P>{
    public SynchronousAssetLoader(FileHandleResolver resolver){
        super(resolver);
    }

    public abstract T load(AssetManager assetManager, String fileName, FileHandle file, P parameter);
}
