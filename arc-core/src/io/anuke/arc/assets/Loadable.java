package io.anuke.arc.assets;

import io.anuke.arc.collection.*;

public interface Loadable{
    void loadAsync();
    void loadSync();

    default Array<AssetDescriptor> getDependencies(){
        return null;
    }
}
