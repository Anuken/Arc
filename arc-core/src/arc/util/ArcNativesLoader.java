package arc.util;

public class ArcNativesLoader{
    public static boolean disableNativesLoading = false;

    static private boolean nativesLoaded;

    /** Loads the arc native libraries if they have not already been loaded. */
    public static synchronized void load(){
        if(nativesLoaded) return;
        nativesLoaded = true;

        if(disableNativesLoading) return;

        new SharedLibraryLoader().load("gdx");
    }
}
