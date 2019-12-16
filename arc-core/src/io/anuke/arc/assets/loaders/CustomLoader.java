package io.anuke.arc.assets.loaders;

import io.anuke.arc.assets.*;
import io.anuke.arc.assets.loaders.resolvers.*;
import io.anuke.arc.collection.*;
import io.anuke.arc.files.*;

public abstract class CustomLoader extends AsynchronousAssetLoader{
    public Runnable loaded = () -> {};

    public CustomLoader(){
        super(new InternalFileHandleResolver());
    }

    @Override
    public Object loadSync(AssetManager manager, String fileName, Fi file, AssetLoaderParameters parameter){
        loaded.run();
        return this;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, Fi file, AssetLoaderParameters parameter){
        return null;
    }
}
