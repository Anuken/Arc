package io.anuke.arc.assets.loaders.resolvers;

import io.anuke.arc.Core;
import io.anuke.arc.assets.loaders.FileHandleResolver;
import io.anuke.arc.files.FileHandle;

public class InternalFileHandleResolver implements FileHandleResolver{
    @Override
    public FileHandle resolve(String fileName){
        return Core.files.internal(fileName);
    }
}
