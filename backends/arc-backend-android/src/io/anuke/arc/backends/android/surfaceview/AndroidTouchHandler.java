package io.anuke.arc.backends.android.surfaceview;

import android.content.Context;
import android.view.MotionEvent;

public interface AndroidTouchHandler{
    void onTouch(MotionEvent event, AndroidInput input);

    boolean supportsMultitouch(Context app);
}
