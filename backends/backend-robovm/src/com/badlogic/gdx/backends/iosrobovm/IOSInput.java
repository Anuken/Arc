package com.badlogic.gdx.backends.iosrobovm;

import com.badlogic.gdx.backends.iosrobovm.custom.UIAcceleration;
import com.badlogic.gdx.backends.iosrobovm.custom.UIAccelerometerDelegate;
import com.badlogic.gdx.backends.iosrobovm.custom.UIAccelerometerDelegateAdapter;
import arc.Core;
import arc.Input;
import arc.struct.Array;
import arc.input.InputDevice;
import arc.input.KeyCode;
import arc.math.geom.Vec3;
import arc.util.ArcRuntimeException;
import arc.util.Log;
import arc.util.pooling.Pool;
import org.robovm.apple.audiotoolbox.AudioServices;
import org.robovm.apple.coregraphics.CGPoint;
import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.foundation.NSExtensions;
import org.robovm.apple.foundation.NSObject;
import org.robovm.apple.foundation.NSRange;
import org.robovm.apple.uikit.*;
import org.robovm.objc.annotation.Method;
import org.robovm.rt.VM;
import org.robovm.rt.bro.NativeObject;
import org.robovm.rt.bro.annotation.MachineSizedUInt;
import org.robovm.rt.bro.annotation.Pointer;

@SuppressWarnings("deprecation")
public class IOSInput extends Input{
    static final int MAX_TOUCHES = 20;
    static final NSObjectWrapper<UIAcceleration> UI_ACCELERATION_WRAPPER = new NSObjectWrapper<>(UIAcceleration.class);
    private static final int POINTER_NOT_FOUND = -1;
    private static final NSObjectWrapper<UITouch> UI_TOUCH_WRAPPER = new NSObjectWrapper<>(UITouch.class);
    protected UIAccelerometerDelegate accelerometerDelegate;
    IOSApplication app;
    IOSApplicationConfiguration config;
    int[] deltaX = new int[MAX_TOUCHES];
    int[] deltaY = new int[MAX_TOUCHES];
    int[] touchX = new int[MAX_TOUCHES];
    int[] touchY = new int[MAX_TOUCHES];
    float[] pressures = new float[MAX_TOUCHES];
    boolean pressureSupported;
    // we store the pointer to the UITouch struct here, or 0
    long[] touchDown = new long[MAX_TOUCHES];
    int numTouched = 0;
    boolean justTouched = false;
    Pool<TouchEvent> touchEventPool = new Pool<TouchEvent>(){
        @Override
        protected TouchEvent newObject(){
            return new TouchEvent();
        }
    };
    Array<TouchEvent> touchEvents = new Array<>();
    TouchEvent currentEvent = null;
    float[] rotation = new float[3];
    Vec3 accel = new Vec3();

    boolean hasVibrator;
    boolean compassSupported;
    boolean keyboardCloseOnReturn;
    // Issue 773 indicates this may solve a premature GC issue
    UIAlertViewDelegate delegate;
    private UITextField textfield = null;
    private final UITextFieldDelegate textDelegate = new UITextFieldDelegateAdapter(){
        @Override
        public boolean shouldChangeCharacters(UITextField textField, NSRange range, String string){
            for(int i = 0; i < range.getLength(); i++){
                app.input.inputMultiplexer.keyTyped((char)8);
            }

            if(string.isEmpty()){
                if(range.getLength() > 0) Core.graphics.requestRendering();
                return false;
            }

            char[] chars = new char[string.length()];
            string.getChars(0, string.length(), chars, 0);

            for(int i = 0; i < chars.length; i++){
                app.input.inputMultiplexer.keyTyped(chars[i]);
            }
            Core.graphics.requestRendering();

            return true;
        }

        @Override
        public boolean shouldEndEditing(UITextField textField){
            // Text field needs to have at least one symbol - so we can use backspace
            textField.setText("x");
            Core.graphics.requestRendering();

            return true;
        }

        @Override
        public boolean shouldReturn(UITextField textField){
            if(keyboardCloseOnReturn) setOnscreenKeyboardVisible(false);
            app.input.inputMultiplexer.keyDown(KeyCode.ENTER);
            app.input.inputMultiplexer.keyTyped((char)13);
            Core.graphics.requestRendering();
            return false;
        }
    };

    public IOSInput(IOSApplication app){
        this.app = app;
        this.config = app.config;
        this.keyboardCloseOnReturn = app.config.keyboardCloseOnReturn;
    }

    void setupPeripherals(){
        setupAccelerometer();
        UIDevice device = UIDevice.getCurrentDevice();
        if(device.getModel().equalsIgnoreCase("iphone")) hasVibrator = true;

        if(app.getIosVersion() >= 9){
            UIForceTouchCapability forceTouchCapability = UIScreen.getMainScreen().getTraitCollection().getForceTouchCapability();
            pressureSupported = forceTouchCapability == UIForceTouchCapability.Available;
        }
    }

    protected void setupAccelerometer(){
        if(config.useAccelerometer){
            accelerometerDelegate = new UIAccelerometerDelegateAdapter(){

                @Method(selector = "accelerometer:didAccelerate:")
                public void didAccelerate(com.badlogic.gdx.backends.iosrobovm.custom.UIAccelerometer accelerometer, @Pointer long valuesPtr){
                    com.badlogic.gdx.backends.iosrobovm.custom.UIAcceleration values = UI_ACCELERATION_WRAPPER.wrap(valuesPtr);
                    float x = (float)values.getX() * 10;
                    float y = (float)values.getY() * 10;
                    float z = (float)values.getZ() * 10;

                    accel.set(-x, -y, -z);
                }
            };
            com.badlogic.gdx.backends.iosrobovm.custom.UIAccelerometer.getSharedAccelerometer().setDelegate(accelerometerDelegate);
            com.badlogic.gdx.backends.iosrobovm.custom.UIAccelerometer.getSharedAccelerometer().setUpdateInterval(config.accelerometerUpdate);
        }
    }

    @Override
    public Vec3 getAccelerometer(){
        return accel;
    }

    @Override
    public int mouseX(){
        return touchX[0];
    }

    @Override
    public int mouseX(int pointer){
        return touchX[pointer];
    }

    @Override
    public int deltaX(){
        return deltaX[0];
    }

    @Override
    public int deltaX(int pointer){
        return deltaX[pointer];
    }

    @Override
    public int mouseY(){
        return touchY[0];
    }

    @Override
    public int mouseY(int pointer){
        return touchY[pointer];
    }

    @Override
    public int deltaY(){
        return deltaY[0];
    }

    @Override
    public int deltaY(int pointer){
        return deltaY[pointer];
    }

    @Override
    public boolean isTouched(){
        for(int pointer = 0; pointer < MAX_TOUCHES; pointer++){
            if(touchDown[pointer] != 0){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean justTouched(){
        return justTouched;
    }

    @Override
    public boolean isTouched(int pointer){
        return touchDown[pointer] != 0;
    }

    @Override
    public float getPressure(){
        return pressures[0];
    }

    // hack for software keyboard support
    // uses a hidden textfield to capture input
    // see: http://www.badlogicgames.com/forum/viewtopic.php?f=17&t=11788

    @Override
    public float getPressure(int pointer){
        return pressures[pointer];
    }

    @Override
    public void getTextInput(TextInput input){
        buildUIAlertView(input).show();
    }

    @Override
    public void setOnscreenKeyboardVisible(boolean visible){
        if(textfield == null) createDefaultTextField();
        if(visible){
            textfield.becomeFirstResponder();
            textfield.setDelegate(textDelegate);
        }else{
            textfield.resignFirstResponder();
        }
    }

    /**
     * Set the keyboard to close when the UITextField return key is pressed
     * @param shouldClose Whether or not the keyboard should clsoe on return key press
     */
    public void setKeyboardCloseOnReturnKey(boolean shouldClose){
        keyboardCloseOnReturn = shouldClose;
    }

    public UITextField getKeyboardTextField(){
        if(textfield == null) createDefaultTextField();
        return textfield;
    }

    private void createDefaultTextField(){
        textfield = new UITextField(new CGRect(10, 10, 100, 50));
        //Parameters
        // Setting parameters
        textfield.setKeyboardType(UIKeyboardType.Default);
        textfield.setReturnKeyType(UIReturnKeyType.Done);
        textfield.setAutocapitalizationType(UITextAutocapitalizationType.None);
        textfield.setAutocorrectionType(UITextAutocorrectionType.No);
        textfield.setSpellCheckingType(UITextSpellCheckingType.No);
        textfield.setHidden(true);
        // Text field needs to have at least one symbol - so we can use backspace
        textfield.setText("x");
        app.getUIViewController().getView().addSubview(textfield);
    }

    /**
     * Builds an {@link UIAlertView} with an added {@link UITextField} for inputting text.

     * @return UiAlertView
     */
    private UIAlertView buildUIAlertView(TextInput input){
        delegate = new UIAlertViewDelegateAdapter(){
            @Override
            public void clicked(UIAlertView view, long clicked){
                if(clicked == 0){
                    // user clicked "Cancel" button
                    input.canceled.run();
                }else if(clicked == 1){
                    // user clicked "Ok" button
                    UITextField textField = view.getTextField(0);
                    input.accepted.get(textField.getText());
                }
                delegate = null;
            }

            @Override
            public void cancel(UIAlertView view){
                input.canceled.run();
                delegate = null;
            }
        };

        // build the view
        final UIAlertView uiAlertView = new UIAlertView();
        uiAlertView.setTitle(input.title);
        uiAlertView.addButton("Cancel");
        uiAlertView.addButton("Ok");
        uiAlertView.setAlertViewStyle(UIAlertViewStyle.PlainTextInput);
        uiAlertView.setDelegate(delegate);

        //TODO no max length support
        UITextField textField = uiAlertView.getTextField(0);
        textField.setText(input.text);
        if(input.numeric){
            textField.setKeyboardType(UIKeyboardType.NumberPad);
        }

        return uiAlertView;
    }

    @Override
    public void vibrate(int milliseconds){
        AudioServices.playSystemSound(4095);
    }

    @Override
    public long getCurrentEventTime(){
        return currentEvent.timestamp;
    }

    @Override
    public boolean isPeripheralAvailable(Peripheral peripheral){
        if(peripheral == Peripheral.Accelerometer && config.useAccelerometer) return true;
        if(peripheral == Peripheral.MultitouchScreen) return true;
        if(peripheral == Peripheral.Vibrator) return hasVibrator;
        if(peripheral == Peripheral.Compass) return compassSupported;
        if(peripheral == Peripheral.OnscreenKeyboard) return true;
        if(peripheral == Peripheral.Pressure) return pressureSupported;
        return false;
    }

    @Override
    public int getRotation(){
        // we measure orientation counter clockwise, just like on Android
        switch(app.uiApp.getStatusBarOrientation()){
            case LandscapeLeft:
                return 270;
            case PortraitUpsideDown:
                return 180;
            case LandscapeRight:
                return 90;
            case Portrait:
            default:
                return 0;
        }
    }

    @Override
    public Orientation getNativeOrientation(){
        switch(app.uiApp.getStatusBarOrientation()){
            case LandscapeLeft:
            case LandscapeRight:
                return Orientation.Landscape;
            default:
                return Orientation.Portrait;
        }
    }

    protected void onTouch(long touches){
        toTouchEvents(touches);
        Core.graphics.requestRendering();
    }

    void processEvents(){
        synchronized(touchEvents){
            justTouched = false;
            for(TouchEvent event : touchEvents){
                currentEvent = event;
                switch(event.phase){
                    case Began:
                        inputMultiplexer.touchDown(event.x, event.y, event.pointer, KeyCode.MOUSE_LEFT);
                        if(numTouched >= 1) justTouched = true;
                        break;
                    case Cancelled:
                    case Ended:
                        inputMultiplexer.touchUp(event.x, event.y, event.pointer, KeyCode.MOUSE_LEFT);
                        break;
                    case Moved:
                    case Stationary:
                        inputMultiplexer.touchDragged(event.x, event.y, event.pointer);
                        break;
                }
            }
            touchEventPool.freeAll(touchEvents);
            touchEvents.clear();
        }
    }

    void processDevices(){
        for(InputDevice device : devices){
            device.update();
        }
    }

    private int getFreePointer(){
        for(int i = 0; i < touchDown.length; i++){
            if(touchDown[i] == 0) return i;
        }
        throw new ArcRuntimeException("Couldn't find free pointer id!");
    }

    private int findPointer(UITouch touch){
        long ptr = touch.getHandle();
        for(int i = 0; i < touchDown.length; i++){
            if(touchDown[i] == ptr) return i;
        }
        // If pointer is not found
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < touchDown.length; i++){
            sb.append(i).append(":").append(touchDown[i]).append(" ");
        }
        Log.errTag("IOSInput", "Pointer ID lookup failed: " + ptr + ", " + sb.toString());
        return POINTER_NOT_FOUND;
    }

    private void toTouchEvents(long touches){
        long array = NSSetExtensions.allObjects(touches);
        int length = (int)NSArrayExtensions.count(array);
        for(int i = 0; i < length; i++){
            long touchHandle = NSArrayExtensions.objectAtIndex$(array, i);
            UITouch touch = UI_TOUCH_WRAPPER.wrap(touchHandle);
            final int locX, locY;
            // Get and map the location to our drawing space
            {
                CGPoint loc = touch.getLocationInView(touch.getWindow());
                final CGRect bounds = app.getCachedBounds();
                locX = (int)(loc.getX() * app.displayScaleFactor - bounds.getMinX());
                locY = (int)bounds.getHeight() - 1 - (int)(loc.getY() * app.displayScaleFactor - bounds.getMinY());
                // app.debug("IOSInput","pos= "+loc+"  bounds= "+bounds+" x= "+locX+" locY= "+locY);
            }

            // if its not supported, we will simply use 1.0f when touch is present
            float pressure = 1.0f;
            if(pressureSupported){
                pressure = (float)touch.getForce();
            }

            synchronized(touchEvents){
                UITouchPhase phase = touch.getPhase();
                TouchEvent event = touchEventPool.obtain();
                event.x = locX;
                event.y = locY;
                event.phase = phase;
                event.timestamp = (long)(touch.getTimestamp() * 1000000000);

                if(phase == UITouchPhase.Began){
                    event.pointer = getFreePointer();
                    touchDown[event.pointer] = touch.getHandle();
                    touchX[event.pointer] = event.x;
                    touchY[event.pointer] = event.y;
                    deltaX[event.pointer] = 0;
                    deltaY[event.pointer] = 0;
                    pressures[event.pointer] = pressure;
                    numTouched++;
                }else if(phase == UITouchPhase.Moved || phase == UITouchPhase.Stationary){
                    event.pointer = findPointer(touch);
                    if(event.pointer != POINTER_NOT_FOUND){
                        deltaX[event.pointer] = event.x - touchX[event.pointer];
                        deltaY[event.pointer] = event.y - touchY[event.pointer];
                        touchX[event.pointer] = event.x;
                        touchY[event.pointer] = event.y;
                        pressures[event.pointer] = pressure;
                    }
                }else if(phase == UITouchPhase.Cancelled || phase == UITouchPhase.Ended){
                    event.pointer = findPointer(touch);
                    if(event.pointer != POINTER_NOT_FOUND){
                        touchDown[event.pointer] = 0;
                        touchX[event.pointer] = event.x;
                        touchY[event.pointer] = event.y;
                        deltaX[event.pointer] = 0;
                        deltaY[event.pointer] = 0;
                        pressures[event.pointer] = 0;
                        numTouched--;
                    }
                }

                if(event.pointer != POINTER_NOT_FOUND){
                    touchEvents.add(event);
                }else{
                    touchEventPool.free(event);
                }
            }
        }
    }

    private static class NSObjectWrapper<T extends NSObject>{
        private static final long HANDLE_OFFSET;

        static{
            try{
                HANDLE_OFFSET = VM.getInstanceFieldOffset(VM.getFieldAddress(NativeObject.class.getDeclaredField("handle")));
            }catch(Throwable t){
                throw new Error(t);
            }
        }

        private final T instance;

        public NSObjectWrapper(Class<T> cls){
            instance = VM.allocateObject(cls);
        }

        public T wrap(long handle){
            VM.setLong(VM.getObjectAddress(instance) + HANDLE_OFFSET, handle);
            return instance;
        }
    }

    private static class NSSetExtensions extends NSExtensions{
        @Method(selector = "allObjects")
        public static native @Pointer
        long allObjects(@Pointer long thiz);
    }

    private static class NSArrayExtensions extends NSExtensions{
        @Method(selector = "objectAtIndex:")
        public static native @Pointer
        long objectAtIndex$(@Pointer long thiz, @MachineSizedUInt long index);

        @Method(selector = "count")
        public static native @MachineSizedUInt
        long count(@Pointer long thiz);
    }

    static class TouchEvent{
        UITouchPhase phase;
        long timestamp;
        int x, y;
        int pointer;
    }

    private class HiddenTextField extends UITextField{
        public HiddenTextField(CGRect frame){
            super(frame);

            setKeyboardType(UIKeyboardType.Default);
            setReturnKeyType(UIReturnKeyType.Done);
            setAutocapitalizationType(UITextAutocapitalizationType.None);
            setAutocorrectionType(UITextAutocorrectionType.No);
            setSpellCheckingType(UITextSpellCheckingType.No);
            setHidden(true);
        }

        @Override
        public void deleteBackward(){
            app.input.inputMultiplexer.keyTyped((char)8);
            super.deleteBackward();
            Core.graphics.requestRendering();
        }
    }
}
