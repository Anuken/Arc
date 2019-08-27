package io.anuke.arc.assets;

import io.anuke.arc.collection.*;
import io.anuke.arc.util.reflect.*;

public interface Loadable{
    default void loadAsync(){

    }

    default void loadSync(){

    }

    default String getName(){
        return ClassReflection.getSimpleName(getClass());
    }

    default Array<AssetDescriptor> getDependencies(){
        return null;
    }
}
