package io.anuke.arc.backends.android.surfaceview;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnGenericMotionListener;
import io.anuke.arc.Application;

import java.util.ArrayList;

/**
 * Subclass of AndroidInput, used on Android +3.x to get generic motion events for things like gamepads/joysticks and so on.
 * @author mzechner
 */
public class AndroidInputThreePlus extends AndroidInput implements OnGenericMotionListener{
    private final AndroidMouseHandler mouseHandler;
    ArrayList<OnGenericMotionListener> genericMotionListeners = new ArrayList<>();

    public AndroidInputThreePlus(Application activity, Context context, Object view, AndroidApplicationConfiguration config){
        super(activity, context, view, config);
        // we hook into View, for LWPs we call onTouch below directly from
        // within the AndroidLivewallpaperEngine#onTouchEvent() method.
        if(view instanceof View){
            View v = (View)view;
            v.setOnGenericMotionListener(this);
        }
        mouseHandler = new AndroidMouseHandler();
    }

    @Override
    public boolean onGenericMotion(View view, MotionEvent event){
        if(mouseHandler.onGenericMotion(event, this)) return true;
        for(int i = 0, n = genericMotionListeners.size(); i < n; i++)
            if(genericMotionListeners.get(i).onGenericMotion(view, event)) return true;
        return false;
    }

    public void addGenericMotionListener(OnGenericMotionListener listener){
        genericMotionListeners.add(listener);
    }
}
