package io.anuke.arc.assets.loaders;

import io.anuke.arc.assets.AssetDescriptor;
import io.anuke.arc.assets.AssetLoaderParameters;
import io.anuke.arc.collection.Array;
import io.anuke.arc.files.FileHandle;

/**
 * Abstract base class for asset loaders.
 * @param <T> the class of the asset the loader supports
 * @param <P> the class of the loading parameters the loader supports.
 * @author mzechner
 */
public abstract class AssetLoader<T, P extends AssetLoaderParameters<T>>{
    /** {@link FileHandleResolver} used to map from plain asset names to {@link FileHandle} instances **/
    private FileHandleResolver resolver;

    /**
     * Constructor, sets the {@link FileHandleResolver} to use to resolve the file associated with the asset name.
     */
    public AssetLoader(FileHandleResolver resolver){
        this.resolver = resolver;
    }

    /**
     * @param fileName file name to resolve
     * @return handle to the file, as resolved by the {@link FileHandleResolver} set on the loader
     */
    public FileHandle resolve(String fileName){
        return resolver.resolve(fileName);
    }

    /**
     * Returns the assets this asset requires to be loaded first. This method may be called on a thread other than the GL thread.
     * @param fileName name of the asset to load
     * @param file the resolved file to load
     * @param parameter parameters for loading the asset
     * @return other assets that the asset depends on and need to be loaded first or null if there are no dependencies.
     */
    public abstract Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, P parameter);
}
