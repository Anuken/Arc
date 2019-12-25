package arc.assets.loaders.resolvers;

import arc.Core;
import arc.assets.loaders.FileHandleResolver;
import arc.files.Fi;

public class AbsoluteFileHandleResolver implements FileHandleResolver{
    @Override
    public Fi resolve(String fileName){
        return Core.files.absolute(fileName);
    }
}
