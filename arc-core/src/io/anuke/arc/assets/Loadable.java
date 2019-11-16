package io.anuke.arc.assets;

import io.anuke.arc.collection.*;

public interface Loadable{
    default void loadAsync(){

    }

    default void loadSync(){

    }

    default String getName(){
        return getClass().getSimpleName();
    }

    default Array<AssetDescriptor> getDependencies(){
        return null;
    }
}
