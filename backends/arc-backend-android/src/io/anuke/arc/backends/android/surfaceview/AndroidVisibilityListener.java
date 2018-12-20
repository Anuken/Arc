package io.anuke.arc.backends.android.surfaceview;

import android.view.View;
import io.anuke.arc.utils.Log;

/**
 * Allows immersive mode support while maintaining compatibility with Android versions before API Level 19 (4.4)
 * @author Unkn0wn0ne
 */
public class AndroidVisibilityListener{

    public void createListener(final AndroidApplicationBase application){
        try{
            View rootView = application.getApplicationWindow().getDecorView();
            rootView.setOnSystemUiVisibilityChangeListener(arg0 -> application.getHandler().post(() -> application.useImmersiveMode(true)));
        }catch(Throwable t){
            Log.err("Can't create OnSystemUiVisibilityChangeListener, unable to use immersive mode.", t);
        }
    }
}
