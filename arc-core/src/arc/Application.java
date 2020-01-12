package arc;

import arc.struct.*;
import arc.graphics.g2d.*;
import arc.util.*;

public interface Application extends Disposable{

    /** Returns a list of all the application listeners used. */
    Array<ApplicationListener> getListeners();

    /** Adds a new application listener. */
    default void addListener(ApplicationListener listener){
        synchronized(getListeners()){
            getListeners().add(listener);
        }
    }

    /** Removes an application listener. */
    default void removeListener(ApplicationListener listener){
        synchronized(getListeners()){
            getListeners().remove(listener);
        }
    }

    /** @return what {@link ApplicationType} this application has, e.g. Android or Desktop */
    ApplicationType getType();

    /** @return the Android API level on Android, the major OS version on iOS (5, 6, 7, ..), or 0 on the desktop. */
    default int getVersion(){
        return 0;
    }

    /** @return the Java heap memory use in bytes. */
    default long getJavaHeap(){
        return 0;
    }

    /** @return the Native heap memory use in bytes. Only valid on Android. */
    default long getNativeHeap(){
        return 0;
    }

    String getClipboardText();

    void setClipboardText(String text);

    /** Open a folder in the system's file browser.
     * @return whether this operation was successful. */
    default boolean openFolder(String file){
        return false;
    }

    /**
     * Posts a runnable on the main loop thread.
     * <p>
     * In a multi-window application, the {@linkplain Core#graphics} and {@linkplain Core#input} values may be
     * unpredictable at the time the Runnable is executed. If graphics or input are needed, they can be copied
     * to a variable to be used in the Runnable. For example:
     * <p><code>
     * final Graphics graphics = Gdx.graphics;
     */
    void post(Runnable runnable);

    /**
     * Schedule an exit from the application. On android, this will cause a call to pause() and dispose() some time in the future,
     * it will not immediately finish your application.
     * On iOS this should be avoided in production as it breaks Apples guidelines.
     */
    void exit();

    /** Disposes of core resources. */
    @Override
    default void dispose(){
        if(Core.assets != null){
            Core.assets.dispose();
            Core.assets = null;
        }

        if(Core.scene != null){
            Core.scene.dispose();
            Core.scene = null;
        }

        if(Core.atlas != null){
            Core.atlas.dispose();
            Core.atlas = null;
        }

        if(Core.batch != null){
            Core.batch.dispose();
            Core.batch = null;
        }

        if(Core.input != null){
            Core.input.dispose();
        }

        Fill.dispose();
        Events.dispose();
    }

    /** Enumeration of possible {@link Application} types */
    enum ApplicationType{
        Android, Desktop, HeadlessDesktop, WebGL, iOS
    }
}
