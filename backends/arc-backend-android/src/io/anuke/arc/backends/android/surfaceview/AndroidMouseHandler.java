/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package io.anuke.arc.backends.android.surfaceview;

import android.view.InputDevice;
import android.view.MotionEvent;
import io.anuke.arc.Core;
import io.anuke.arc.backends.android.surfaceview.AndroidInput.TouchEvent;

/**
 * Mouse handler for devices running Android >= 3.1.
 * @author Richard Martin
 */
public class AndroidMouseHandler{
    private int deltaX = 0;
    private int deltaY = 0;

    public boolean onGenericMotion(MotionEvent event, AndroidInput input){
        if((event.getSource() & InputDevice.SOURCE_CLASS_POINTER) == 0) return false;

        final int action = event.getAction() & MotionEvent.ACTION_MASK;

        int x = 0, y = 0;
        int scrollAmountX = 0;
        int scrollAmountY = 0;

        long timeStamp = System.nanoTime();
        synchronized(input){
            switch(action){
                case MotionEvent.ACTION_HOVER_MOVE:
                    x = (int)event.getX();
                    y = (int)event.getY();
                    if((x != deltaX) || (y != deltaY)){ // Avoid garbage events
                        postTouchEvent(input, TouchEvent.TOUCH_MOVED, x, y, 0, 0, timeStamp);
                        deltaX = x;
                        deltaY = y;
                    }
                    break;

                case MotionEvent.ACTION_SCROLL:
                    scrollAmountY = (int)-Math.signum(event.getAxisValue(MotionEvent.AXIS_VSCROLL));
                    scrollAmountX = (int)-Math.signum(event.getAxisValue(MotionEvent.AXIS_HSCROLL));
                    postTouchEvent(input, TouchEvent.TOUCH_SCROLLED, 0, 0, scrollAmountX, scrollAmountY, timeStamp);

            }
        }
        Core.graphics.requestRendering();
        return true;
    }

    private void postTouchEvent(AndroidInput input, int type, int x, int y, int scrollAmountX, int scrollAmountY, long timeStamp){
        TouchEvent event = input.usedTouchEvents.obtain();
        event.timeStamp = timeStamp;
        event.x = x;
        event.y = y;
        event.type = type;
        event.scrollAmountX = scrollAmountX;
        event.scrollAmountY = scrollAmountY;
        input.touchEvents.add(event);
    }

}
