package io.anuke.arc.assets.loaders.resolvers;

import io.anuke.arc.Core;
import io.anuke.arc.assets.loaders.FileHandleResolver;
import io.anuke.arc.files.Fi;

public class AbsoluteFileHandleResolver implements FileHandleResolver{
    @Override
    public Fi resolve(String fileName){
        return Core.files.absolute(fileName);
    }
}
