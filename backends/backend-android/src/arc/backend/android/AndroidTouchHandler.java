package arc.backend.android;

import android.content.*;
import android.view.*;
import arc.*;
import arc.backend.android.AndroidInput.*;
import arc.input.*;

/**
 * Multitouch handler for devices running Android >= 2.0. If device is capable of (fake) multitouch this will report additional
 * pointers.
 * @author badlogicgames@gmail.com
 */
public class AndroidTouchHandler{

    public void onTouch(MotionEvent event, AndroidInput input){
        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        int pointerId = event.getPointerId(pointerIndex);

        int x, y;
        int realPointerIndex;
        KeyCode button;

        long timeStamp = System.nanoTime();
        synchronized(input){
            switch(action){
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    realPointerIndex = input.getFreePointerIndex(); // get a free pointer index as reported by Input.getX() etc.
                    if(realPointerIndex >= AndroidInput.NUM_TOUCHES) break;
                    input.realId[realPointerIndex] = pointerId;
                    x = (int)event.getX(pointerIndex);
                    y = (int)event.getY(pointerIndex);
                    button = toButton(event.getButtonState());
                    if(button != KeyCode.unknown)
                        postTouchEvent(input, TouchEvent.TOUCH_DOWN, x, y, realPointerIndex, button, timeStamp);
                    input.touchX[realPointerIndex] = x;
                    input.touchY[realPointerIndex] = Core.graphics.getHeight() - 1 - y;
                    input.deltaX[realPointerIndex] = 0;
                    input.deltaY[realPointerIndex] = 0;
                    input.touched[realPointerIndex] = (button != KeyCode.unknown);
                    input.button[realPointerIndex] = button.ordinal();
                    input.pressure[realPointerIndex] = event.getPressure(pointerIndex);
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_OUTSIDE:
                    realPointerIndex = input.lookUpPointerIndex(pointerId);
                    if(realPointerIndex == -1) break;
                    if(realPointerIndex >= AndroidInput.NUM_TOUCHES) break;
                    input.realId[realPointerIndex] = -1;
                    x = (int)event.getX(pointerIndex);
                    y = (int)event.getY(pointerIndex);
                    button = KeyCode.byOrdinal(input.button[realPointerIndex]);
                    if(button != KeyCode.unknown)
                        postTouchEvent(input, TouchEvent.TOUCH_UP, x, y, realPointerIndex, button, timeStamp);
                    input.touchX[realPointerIndex] = x;
                    input.touchY[realPointerIndex] = Core.graphics.getHeight() - 1 - y;
                    input.deltaX[realPointerIndex] = 0;
                    input.deltaY[realPointerIndex] = 0;
                    input.touched[realPointerIndex] = false;
                    input.button[realPointerIndex] = 0;
                    input.pressure[realPointerIndex] = 0;
                    break;

                case MotionEvent.ACTION_CANCEL:
                    for(int i = 0; i < input.realId.length; i++){
                        input.realId[i] = -1;
                        input.touchX[i] = 0;
                        input.touchY[i] = 0;
                        input.deltaX[i] = 0;
                        input.deltaY[i] = 0;
                        input.touched[i] = false;
                        input.button[i] = 0;
                        input.pressure[i] = 0;
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    int pointerCount = event.getPointerCount();
                    for(int i = 0; i < pointerCount; i++){
                        pointerIndex = i;
                        pointerId = event.getPointerId(pointerIndex);
                        x = (int)event.getX(pointerIndex);
                        y = (int)event.getY(pointerIndex);
                        realPointerIndex = input.lookUpPointerIndex(pointerId);
                        if(realPointerIndex == -1) continue;
                        if(realPointerIndex >= AndroidInput.NUM_TOUCHES) break;
                        button = KeyCode.byOrdinal(input.button[realPointerIndex]);
                        if(button != KeyCode.unknown)
                            postTouchEvent(input, TouchEvent.TOUCH_DRAGGED, x, y, realPointerIndex, button, timeStamp);
                        else
                            postTouchEvent(input, TouchEvent.TOUCH_MOVED, x, y, realPointerIndex, KeyCode.mouseLeft, timeStamp);
                        input.deltaX[realPointerIndex] = x - input.touchX[realPointerIndex];
                        input.deltaY[realPointerIndex] = -(y - input.touchY[realPointerIndex]);
                        input.touchX[realPointerIndex] = x;
                        input.touchY[realPointerIndex] = Core.graphics.getHeight() - 1 - y;
                        input.pressure[realPointerIndex] = event.getPressure(pointerIndex);
                    }
                    break;
            }
        }
        Core.graphics.requestRendering();
    }

    private KeyCode toButton(int button){
        if(button == 0 || button == 1) return KeyCode.mouseLeft;
        if(button == 2) return KeyCode.mouseRight;
        if(button == 4) return KeyCode.mouseMiddle;
        if(button == 8) return KeyCode.mouseBack;
        if(button == 16) return KeyCode.mouseForward;
        return KeyCode.unknown;
    }

    private void postTouchEvent(AndroidInput input, int type, int x, int y, int pointer, KeyCode button, long timeStamp){
        TouchEvent event = input.usedTouchEvents.obtain();
        event.timeStamp = timeStamp;
        event.pointer = pointer;
        event.x = x;
        event.y = Core.graphics.getHeight() - y - 1;
        event.type = type;
        event.button = button;
        input.touchEvents.add(event);
    }

    public boolean supportsMultitouch(Context activity){
        return activity.getPackageManager().hasSystemFeature("android.hardware.touchscreen.multitouch");
    }
}
