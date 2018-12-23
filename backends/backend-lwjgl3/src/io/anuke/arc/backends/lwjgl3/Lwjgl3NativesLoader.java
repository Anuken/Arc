package io.anuke.arc.backends.lwjgl3;

import io.anuke.arc.util.ArcNativesLoader;

public final class Lwjgl3NativesLoader{

    static{
        System.setProperty("org.lwjgl.input.Mouse.allowNegativeMouseCoords", "true");
    }

    public static void load(){
        ArcNativesLoader.load();
    }
}
