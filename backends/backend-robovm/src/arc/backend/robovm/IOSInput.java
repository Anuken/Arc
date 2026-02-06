package arc.backend.robovm;

import arc.*;
import arc.backend.robovm.custom.UIAccelerometerDelegate;
import arc.backend.robovm.custom.UIAccelerometerDelegateAdapter;
import arc.backend.robovm.custom.*;
import arc.graphics.gl.*;
import arc.input.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.pooling.*;
import org.robovm.apple.audiotoolbox.*;
import org.robovm.apple.coregraphics.*;
import org.robovm.apple.foundation.*;
import org.robovm.apple.uikit.*;
import org.robovm.objc.annotation.*;
import org.robovm.rt.*;
import org.robovm.rt.bro.*;
import org.robovm.rt.bro.annotation.*;

@SuppressWarnings("deprecation")
public class IOSInput extends Input{
    static final int MAX_TOUCHES = 20;
    static final NSObjectWrapper<UIAcceleration> UI_ACCELERATION_WRAPPER = new NSObjectWrapper<>(UIAcceleration.class);
    private static final int POINTER_NOT_FOUND = -1;
    private static final NSObjectWrapper<UITouch> UI_TOUCH_WRAPPER = new NSObjectWrapper<>(UITouch.class);
    private final Pool<KeyEvent> keyEventPool = new Pool<KeyEvent>(16, 1000){
        protected KeyEvent newObject(){
            return new KeyEvent();
        }
    };
    private final Seq<KeyEvent> keyEvents = new Seq<>();
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
    Seq<TouchEvent> touchEvents = new Seq<>();
    TouchEvent currentEvent = null;
    float[] rotation = new float[3];
    Vec3 accel = new Vec3();
    boolean hasVibrator;
    boolean compassSupported;
    boolean keyboardCloseOnReturn;
    boolean softkeyboardActive = false;
    boolean showingTextInput;
    // Issue 773 indicates this may solve a premature GC issue
    UIAlertViewDelegate delegate;
    private long currentEventTimeStamp;
    private UITextField textfield = null;
    public IOSInput(IOSApplication app){
        this.app = app;
        this.config = app.config;
        this.keyboardCloseOnReturn = app.config.keyboardCloseOnReturn;
    }    private final UITextFieldDelegate textDelegate = new UITextFieldDelegateAdapter(){
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

            for(char c : chars){
                app.input.inputMultiplexer.keyTyped(c);
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
            app.input.inputMultiplexer.keyDown(KeyCode.enter);
            app.input.inputMultiplexer.keyTyped((char)13);
            Core.graphics.requestRendering();
            return false;
        }
    };

    void setupPeripherals(){
        setupAccelerometer();
        UIDevice device = UIDevice.getCurrentDevice();
        if(device.getModel().equalsIgnoreCase("iphone")) hasVibrator = true;

        if(app.getVersion() >= 9){
            UIForceTouchCapability forceTouchCapability = UIScreen.getMainScreen().getTraitCollection().getForceTouchCapability();
            pressureSupported = forceTouchCapability == UIForceTouchCapability.Available;
        }
    }

    protected void setupAccelerometer(){
        if(config.useAccelerometer){
            accelerometerDelegate = new UIAccelerometerDelegateAdapter(){

                @Method(selector = "accelerometer:didAccelerate:")
                public void didAccelerate(UIAccelerometer accelerometer, @Pointer long valuesPtr){
                    UIAcceleration values = UI_ACCELERATION_WRAPPER.wrap(valuesPtr);
                    float x = (float)values.getX() * 10;
                    float y = (float)values.getY() * 10;
                    float z = (float)values.getZ() * 10;

                    accel.set(-x, -y, -z);
                }
            };
            UIAccelerometer.getSharedAccelerometer().setDelegate(accelerometerDelegate);
            UIAccelerometer.getSharedAccelerometer().setUpdateInterval(config.accelerometerUpdate);
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

    @Override
    public float getPressure(int pointer){
        return pressures[pointer];
    }

    @Override
    public void getTextInput(TextInput input){
        if(input.multiline && false){
            //TODO: Crashes, apparently. https://github.com/Anuken/Mindustry/issues/8745
            CGRect rect = new CGRect(0, 0, app.getUIViewController().getView().getBounds().getWidth(), 250);
            UIViewController controller = new UIViewController();
            controller.setPreferredContentSize(rect.getSize());

            UITextView text = new UITextView(rect);
            text.setText(input.text);
            controller.getView().addSubview(text);
            controller.getView().setUserInteractionEnabled(true);
            controller.getView().bringSubviewToFront(text);
            if(input.numeric){
                text.setKeyboardType(UIKeyboardType.NumberPad);
            }

            UIAlertController alert = new UIAlertController(input.title, null);
            if(!input.message.isEmpty()) alert.setMessage(input.message);
            alert.getKeyValueCoder().setValue("contentViewController", controller);
            alert.addAction(new UIAlertAction("Ok", UIAlertActionStyle.Default, action -> Core.app.post(() -> input.accepted.get(text.getText()))));
            alert.addAction(new UIAlertAction("Cancel", UIAlertActionStyle.Destructive, action -> Core.app.post(input.canceled)));

            app.getUIViewController().presentViewController(alert, true, () -> {
            });
        }else{
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
                    showingTextInput = false;
                }

                @Override
                public void cancel(UIAlertView view){
                    input.canceled.run();
                    delegate = null;
                    showingTextInput = false;
                }
            };

            // build the view
            UIAlertView uiAlertView = new UIAlertView();
            uiAlertView.setTitle(input.title);
            if(!input.message.isEmpty()) uiAlertView.setMessage(input.message);
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

            showingTextInput = true;
            uiAlertView.show();
        }
    }

    @Override
    public boolean isShowingTextInput(){
        return showingTextInput;
    }

    // hack for software keyboard support
    // uses a hidden textfield to capture input
    // see: http://www.badlogicgames.com/forum/viewtopic.php?f=17&t=11788

    @Override
    public void setOnscreenKeyboardVisible(boolean visible){
        if(textfield == null) createDefaultTextField();
        softkeyboardActive = visible;
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

    @Override
    public void vibrate(int milliseconds){
        AudioServices.playSystemSound(4095);
    }

    @Override
    public long getCurrentEventTime(){
        return currentEventTimeStamp;
    }

    @Override
    public boolean isPeripheralAvailable(Peripheral peripheral){
        if(peripheral == Peripheral.accelerometer && config.useAccelerometer) return true;
        if(peripheral == Peripheral.multitouchScreen) return true;
        if(peripheral == Peripheral.vibrator) return hasVibrator;
        if(peripheral == Peripheral.compass) return compassSupported;
        if(peripheral == Peripheral.onscreenKeyboard) return true;
        if(peripheral == Peripheral.pressure) return pressureSupported;
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
                return Orientation.landscape;
            default:
                return Orientation.portrait;
        }
    }

    protected void onTouch(long touches){
        toTouchEvents(touches);
        Core.graphics.requestRendering();
    }

    public boolean onKey(UIKey key, boolean down){
        if(key == null){
            return false;
        }

        KeyCode keyCode = IOSKeymap.getKeyCode(key);

        if(keyCode != KeyCode.unknown) synchronized(keyEvents){
            KeyEvent event = keyEventPool.obtain();
            long timeStamp = System.nanoTime();
            event.timeStamp = timeStamp;
            event.keyChar = 0;
            event.keyCode = keyCode;
            event.type = down ? KeyEvent.KEY_DOWN : KeyEvent.KEY_UP;
            keyEvents.add(event);

            if(!down){
                char character;

                switch(keyCode){
                    case del:
                        character = 8;
                        break;
                    case forwardDel:
                        character = 127;
                        break;
                    case enter:
                        character = 13;
                        break;
                    default:
                        String characters = key.getCharacters();
                        // special keys return constants like "UIKeyInputF5", so we check for length 1
                        character = (characters != null && characters.length() == 1) ? characters.charAt(0) : 0;
                }

                if(character >= 0){
                    event = keyEventPool.obtain();
                    event.timeStamp = timeStamp;
                    event.type = KeyEvent.KEY_TYPED;
                    event.keyCode = keyCode;
                    event.keyChar = character;
                    keyEvents.add(event);
                }

            }

        }

        return isCatch(keyCode);
    }

    void processEvents(){
        synchronized(touchEvents){
            justTouched = false;
            for(TouchEvent event : touchEvents){
                currentEventTimeStamp = event.timestamp;
                currentEvent = event;
                switch(event.phase){
                    case Began:
                        inputMultiplexer.touchDown(event.x, event.y, event.pointer, KeyCode.mouseLeft);
                        if(numTouched >= 1) justTouched = true;
                        break;
                    case Cancelled:
                    case Ended:
                        inputMultiplexer.touchUp(event.x, event.y, event.pointer, KeyCode.mouseLeft);
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


        synchronized(keyEvents){
            for(KeyEvent e : keyEvents){
                currentEventTimeStamp = e.timeStamp;
                switch(e.type){
                    case KeyEvent.KEY_DOWN:
                        inputMultiplexer.keyDown(e.keyCode);

                        break;
                    case KeyEvent.KEY_UP:
                        inputMultiplexer.keyUp(e.keyCode);
                        break;
                    case KeyEvent.KEY_TYPED:
                        // don't process key typed events if soft keyboard is active
                        // the soft keyboard hook already catches the changes
                        if(!softkeyboardActive) inputMultiplexer.keyTyped(e.keyChar);
                }

            }
            keyEventPool.freeAll(keyEvents);
            keyEvents.clear();
        }
    }

    void processDevices(){
        keyboard.postUpdate();
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
        Log.errTag("IOSInput", "Pointer ID lookup failed: " + ptr + ", " + sb);
        return POINTER_NOT_FOUND;
    }

    private void toTouchEvents(long touches){
        long array = NSSetExtensions.allObjects(touches);
        int length = (int)NSArrayExtensions.count(array);
        IOSScreenBounds screenBounds = app.getScreenBounds();
        for(int i = 0; i < length; i++){
            long touchHandle = NSArrayExtensions.objectAtIndex$(array, i);
            UITouch touch = UI_TOUCH_WRAPPER.wrap(touchHandle);
            final int locX, locY;
            // Get and map the location to our drawing space
            CGPoint loc = touch.getLocationInView(app.graphics.view);
            if(config.hdpiMode == HdpiMode.pixels){
                locX = (int)((loc.getX() - screenBounds.x) * app.pixelsPerPoint);
                locY = screenBounds.backBufferHeight - 1 - (int)((loc.getY() - screenBounds.y) * app.pixelsPerPoint);
            }else{
                locX = (int)(loc.getX() - screenBounds.x);
                locY = screenBounds.height - 1 - (int)(loc.getY() - screenBounds.y);
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

    static class KeyEvent {
        static final int KEY_DOWN = 0;
        static final int KEY_UP = 1;
        static final int KEY_TYPED = 2;

        long timeStamp;
        int type;
        KeyCode keyCode;
        char keyChar;
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
