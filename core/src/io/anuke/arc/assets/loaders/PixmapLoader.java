package io.anuke.arc.assets.loaders;

import io.anuke.arc.assets.AssetDescriptor;
import io.anuke.arc.assets.AssetLoaderParameters;
import io.anuke.arc.assets.AssetManager;
import io.anuke.arc.collection.Array;
import io.anuke.arc.files.FileHandle;
import io.anuke.arc.graphics.Pixmap;

/**
 * {@link AssetLoader} for {@link Pixmap} instances. The Pixmap is loaded asynchronously.
 * @author mzechner
 */
public class PixmapLoader extends AsynchronousAssetLoader<Pixmap, PixmapLoader.PixmapParameter>{
    Pixmap pixmap;

    public PixmapLoader(FileHandleResolver resolver){
        super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, PixmapParameter parameter){
        pixmap = null;
        pixmap = new Pixmap(file);
    }

    @Override
    public Pixmap loadSync(AssetManager manager, String fileName, FileHandle file, PixmapParameter parameter){
        Pixmap pixmap = this.pixmap;
        this.pixmap = null;
        return pixmap;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, PixmapParameter parameter){
        return null;
    }

    static public class PixmapParameter extends AssetLoaderParameters<Pixmap>{
    }
}
