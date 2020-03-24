package arc.backend.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import arc.Application;
import arc.struct.Array;

/**
 * Interface that abstracts the Android application class usages, so that libGDX can be used with a fragment (or with any other
 * client code)
 * @author Bartol Karuza (me@bartolkaruza.com)
 * @author davebaol
 */
public interface AndroidApplicationBase extends Application{
    int MINIMUM_SDK = 12;

    /**
     * The application or activity context
     * @return the {@link Context}
     */
    Context getContext();

    /** A set of usable runnables*/
    Array<Runnable> getRunnables();

    /** The currently executed runnables*/
    Array<Runnable> getExecutedRunnables();

    /**
     * Method signifies an intent of the caller to execute some action on the UI Thread.
     * @param runnable The runnable to be executed
     */
    void runOnUiThread(Runnable runnable);

    /**
     * Method signifies an intent to start an activity, may be the default method of the {@link Activity} class
     * @param intent The {@link Intent} for starting an activity
     */
    void startActivity(Intent intent);

    /**
     * Returns the Window associated with the application
     * @return The {@link Window} associated with the application
     */
    Window getApplicationWindow();

    /**
     * Returns the WindowManager associated with the application
     * @return The {@link WindowManager} associated with the application
     */
    WindowManager getWindowManager();

    /**
     * Activates Android 4.4 KitKat's 'Immersive Mode' feature.
     * @param b Whether or not to use immersive mode
     */
    void useImmersiveMode(boolean b);

    /**
     * Returns the Handler object created by the application
     * @return The {@link Handler} object created by the application
     */
    Handler getHandler();
}
