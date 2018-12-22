package io.anuke.arc.util;

public class ArcNativesLoader{
    static public boolean disableNativesLoading = false;

    static private boolean nativesLoaded;

    /** Loads the arc native libraries if they have not already been loaded. */
    static public synchronized void load(){
        if(nativesLoaded) return;
        nativesLoaded = true;

        if(disableNativesLoading) return;

        new SharedLibraryLoader().load("gdx");
    }
}
