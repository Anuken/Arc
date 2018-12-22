package io.anuke.arc.backends.gwt;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.dom.client.KeyCodes;
import io.anuke.arc.Core;
import io.anuke.arc.Input;
import io.anuke.arc.collection.Bits;
import io.anuke.arc.collection.IntMap;
import io.anuke.arc.collection.IntSet;
import io.anuke.arc.collection.IntSet.IntSetIterator;
import io.anuke.arc.input.InputProcessor;
import io.anuke.arc.input.KeyCode;
import io.anuke.arc.util.Time;

public class GwtInput extends Input{
    static final int MAX_TOUCHES = 20;
    // these are absent from KeyCodes; we know not why...
    private static final int KEY_PAUSE = 19;
    private static final int KEY_CAPS_LOCK = 20;
    private static final int KEY_SPACE = 32;
    private static final int KEY_INSERT = 45;
    private static final int KEY_0 = 48;
    private static final int KEY_1 = 49;
    private static final int KEY_2 = 50;
    private static final int KEY_3 = 51;
    private static final int KEY_4 = 52;
    private static final int KEY_5 = 53;
    private static final int KEY_6 = 54;
    private static final int KEY_7 = 55;
    private static final int KEY_8 = 56;
    private static final int KEY_9 = 57;
    private static final int KEY_A = 65;
    private static final int KEY_B = 66;
    private static final int KEY_C = 67;
    private static final int KEY_D = 68;
    private static final int KEY_E = 69;
    private static final int KEY_F = 70;
    private static final int KEY_G = 71;
    private static final int KEY_H = 72;
    private static final int KEY_I = 73;
    private static final int KEY_J = 74;
    private static final int KEY_K = 75;
    private static final int KEY_L = 76;
    private static final int KEY_M = 77;
    private static final int KEY_N = 78;
    private static final int KEY_O = 79;
    private static final int KEY_P = 80;
    private static final int KEY_Q = 81;
    private static final int KEY_R = 82;
    private static final int KEY_S = 83;
    private static final int KEY_T = 84;
    private static final int KEY_U = 85;
    private static final int KEY_V = 86;
    private static final int KEY_W = 87;
    private static final int KEY_X = 88;
    private static final int KEY_Y = 89;
    private static final int KEY_Z = 90;
    private static final int KEY_LEFT_WINDOW_KEY = 91;
    private static final int KEY_RIGHT_WINDOW_KEY = 92;
    private static final int KEY_SELECT_KEY = 93;
    private static final int KEY_NUMPAD0 = 96;
    private static final int KEY_NUMPAD1 = 97;
    private static final int KEY_NUMPAD2 = 98;
    private static final int KEY_NUMPAD3 = 99;
    private static final int KEY_NUMPAD4 = 100;
    private static final int KEY_NUMPAD5 = 101;
    private static final int KEY_NUMPAD6 = 102;
    private static final int KEY_NUMPAD7 = 103;
    private static final int KEY_NUMPAD8 = 104;
    private static final int KEY_NUMPAD9 = 105;
    private static final int KEY_MULTIPLY = 106;
    private static final int KEY_ADD = 107;
    private static final int KEY_SUBTRACT = 109;
    private static final int KEY_DECIMAL_POINT_KEY = 110;
    private static final int KEY_DIVIDE = 111;
    private static final int KEY_F1 = 112;
    private static final int KEY_F2 = 113;
    private static final int KEY_F3 = 114;
    private static final int KEY_F4 = 115;
    private static final int KEY_F5 = 116;
    private static final int KEY_F6 = 117;
    private static final int KEY_F7 = 118;
    private static final int KEY_F8 = 119;
    private static final int KEY_F9 = 120;
    private static final int KEY_F10 = 121;
    private static final int KEY_F11 = 122;
    private static final int KEY_F12 = 123;
    private static final int KEY_NUM_LOCK = 144;
    private static final int KEY_SCROLL_LOCK = 145;
    private static final int KEY_SEMICOLON = 186;
    private static final int KEY_EQUALS = 187;
    private static final int KEY_COMMA = 188;
    private static final int KEY_DASH = 189;
    private static final int KEY_PERIOD = 190;
    private static final int KEY_FORWARD_SLASH = 191;
    private static final int KEY_GRAVE_ACCENT = 192;
    private static final int KEY_OPEN_BRACKET = 219;
    private static final int KEY_BACKSLASH = 220;
    private static final int KEY_CLOSE_BRACKET = 221;
    private static final int KEY_SINGLE_QUOTE = 222;
    final CanvasElement canvas;
    boolean justTouched = false;
    IntSet pressedButtons = new IntSet();
    int pressedKeyCount = 0;
    IntSet pressedKeySet = new IntSet();
    Bits pressedKeys = new Bits(256);
    boolean keyJustPressed = false;
    Bits justPressedKeys = new Bits(256);
    InputProcessor processor;
    char lastKeyCharPressed;
    float keyRepeatTimer;
    long currentEventTimeStamp;
    boolean hasFocus = true;
    private IntMap<Integer> touchMap = new IntMap<Integer>(20);
    private boolean[] touched = new boolean[MAX_TOUCHES];
    private int[] touchX = new int[MAX_TOUCHES];
    private int[] touchY = new int[MAX_TOUCHES];
    private int[] deltaX = new int[MAX_TOUCHES];
    private int[] deltaY = new int[MAX_TOUCHES];
    public GwtInput(CanvasElement canvas){
        this.canvas = canvas;
        hookEvents();
    }

    private static native boolean isTouchScreen() /*-{
		return (('ontouchstart' in window) || (navigator.msMaxTouchPoints > 0));
	}-*/;

    // kindly borrowed from our dear playn friends...
    static native void addEventListener(JavaScriptObject target, String name, GwtInput handler, boolean capture) /*-{
		target
				.addEventListener(
						name,
						function(e) {
							handler.@io.anuke.arc.backends.gwt.GwtInput::handleEvent(Lcom/google/gwt/dom/client/NativeEvent;)(e);
						}, capture);
	}-*/;

    private static native float getMouseWheelVelocity(NativeEvent evt) /*-{
		var delta = 0.0;
		var agentInfo = @io.anuke.arc.backends.gwt.GwtApplication::agentInfo()();

		if (agentInfo.isFirefox) {
			if (agentInfo.isMacOS) {
				delta = 1.0 * evt.detail;
			} else {
				delta = 1.0 * evt.detail / 3;
			}
		} else if (agentInfo.isOpera) {
			if (agentInfo.isLinux) {
				delta = -1.0 * evt.wheelDelta / 80;
			} else {
				// on mac
				delta = -1.0 * evt.wheelDelta / 40;
			}
		} else if (agentInfo.isChrome || agentInfo.isSafari || agentInfo.isIE) {
			delta = -1.0 * evt.wheelDelta / 120;
			// handle touchpad for chrome
			if (Math.abs(delta) < 1) {
				if (agentInfo.isWindows) {
					delta = -1.0 * evt.wheelDelta;
				} else if (agentInfo.isMacOS) {
					delta = -1.0 * evt.wheelDelta / 3;
				}
			}
		}
		return delta;
	}-*/;

    /** Kindly borrowed from PlayN. **/
    protected static native String getMouseWheelEvent() /*-{
		if (navigator.userAgent.toLowerCase().indexOf('firefox') != -1) {
			return "DOMMouseScroll";
		} else {
			return "mousewheel";
		}
	}-*/;

    private static native JavaScriptObject getWindow() /*-{
		return $wnd;
	}-*/;

    /** borrowed from PlayN, thanks guys **/
    private static KeyCode keyForCode(int keyCode){
        switch(keyCode){
            case KeyCodes.KEY_ALT:
                return KeyCode.ALT_LEFT;
            case KeyCodes.KEY_BACKSPACE:
                return KeyCode.BACKSPACE;
            case KeyCodes.KEY_CTRL:
                return KeyCode.CONTROL_LEFT;
            case KeyCodes.KEY_DELETE:
                return KeyCode.DEL;
            case KeyCodes.KEY_DOWN:
                return KeyCode.DOWN;
            case KeyCodes.KEY_END:
                return KeyCode.END;
            case KeyCodes.KEY_ENTER:
                return KeyCode.ENTER;
            case KeyCodes.KEY_ESCAPE:
                return KeyCode.ESCAPE;
            case KeyCodes.KEY_HOME:
                return KeyCode.HOME;
            case KeyCodes.KEY_LEFT:
                return KeyCode.LEFT;
            case KeyCodes.KEY_PAGEDOWN:
                return KeyCode.PAGE_DOWN;
            case KeyCodes.KEY_PAGEUP:
                return KeyCode.PAGE_UP;
            case KeyCodes.KEY_RIGHT:
                return KeyCode.RIGHT;
            case KeyCodes.KEY_SHIFT:
                return KeyCode.SHIFT_LEFT;
            case KeyCodes.KEY_TAB:
                return KeyCode.TAB;
            case KeyCodes.KEY_UP:
                return KeyCode.UP;

            case KEY_PAUSE:
                return KeyCode.UNKNOWN; // FIXME
            case KEY_CAPS_LOCK:
                return KeyCode.UNKNOWN; // FIXME
            case KEY_SPACE:
                return KeyCode.SPACE;
            case KEY_INSERT:
                return KeyCode.INSERT;
            case KEY_0:
                return KeyCode.NUM_0;
            case KEY_1:
                return KeyCode.NUM_1;
            case KEY_2:
                return KeyCode.NUM_2;
            case KEY_3:
                return KeyCode.NUM_3;
            case KEY_4:
                return KeyCode.NUM_4;
            case KEY_5:
                return KeyCode.NUM_5;
            case KEY_6:
                return KeyCode.NUM_6;
            case KEY_7:
                return KeyCode.NUM_7;
            case KEY_8:
                return KeyCode.NUM_8;
            case KEY_9:
                return KeyCode.NUM_9;
            case KEY_A:
                return KeyCode.A;
            case KEY_B:
                return KeyCode.B;
            case KEY_C:
                return KeyCode.C;
            case KEY_D:
                return KeyCode.D;
            case KEY_E:
                return KeyCode.E;
            case KEY_F:
                return KeyCode.F;
            case KEY_G:
                return KeyCode.G;
            case KEY_H:
                return KeyCode.H;
            case KEY_I:
                return KeyCode.I;
            case KEY_J:
                return KeyCode.J;
            case KEY_K:
                return KeyCode.K;
            case KEY_L:
                return KeyCode.L;
            case KEY_M:
                return KeyCode.M;
            case KEY_N:
                return KeyCode.N;
            case KEY_O:
                return KeyCode.O;
            case KEY_P:
                return KeyCode.P;
            case KEY_Q:
                return KeyCode.Q;
            case KEY_R:
                return KeyCode.R;
            case KEY_S:
                return KeyCode.S;
            case KEY_T:
                return KeyCode.T;
            case KEY_U:
                return KeyCode.U;
            case KEY_V:
                return KeyCode.V;
            case KEY_W:
                return KeyCode.W;
            case KEY_X:
                return KeyCode.X;
            case KEY_Y:
                return KeyCode.Y;
            case KEY_Z:
                return KeyCode.Z;
            case KEY_LEFT_WINDOW_KEY:
                return KeyCode.UNKNOWN; // FIXME
            case KEY_RIGHT_WINDOW_KEY:
                return KeyCode.UNKNOWN; // FIXME
            // case KEY_SELECT_KEY: return KeyCode.SELECT_KEY;
            case KEY_NUMPAD0:
                return KeyCode.NUMPAD_0;
            case KEY_NUMPAD1:
                return KeyCode.NUMPAD_1;
            case KEY_NUMPAD2:
                return KeyCode.NUMPAD_2;
            case KEY_NUMPAD3:
                return KeyCode.NUMPAD_3;
            case KEY_NUMPAD4:
                return KeyCode.NUMPAD_4;
            case KEY_NUMPAD5:
                return KeyCode.NUMPAD_5;
            case KEY_NUMPAD6:
                return KeyCode.NUMPAD_6;
            case KEY_NUMPAD7:
                return KeyCode.NUMPAD_7;
            case KEY_NUMPAD8:
                return KeyCode.NUMPAD_8;
            case KEY_NUMPAD9:
                return KeyCode.NUMPAD_9;
            case KEY_MULTIPLY:
                return KeyCode.UNKNOWN; // FIXME
            case KEY_ADD:
                return KeyCode.PLUS;
            case KEY_SUBTRACT:
                return KeyCode.MINUS;
            case KEY_DECIMAL_POINT_KEY:
                return KeyCode.PERIOD;
            case KEY_DIVIDE:
                return KeyCode.UNKNOWN; // FIXME
            case KEY_F1:
                return KeyCode.F1;
            case KEY_F2:
                return KeyCode.F2;
            case KEY_F3:
                return KeyCode.F3;
            case KEY_F4:
                return KeyCode.F4;
            case KEY_F5:
                return KeyCode.F5;
            case KEY_F6:
                return KeyCode.F6;
            case KEY_F7:
                return KeyCode.F7;
            case KEY_F8:
                return KeyCode.F8;
            case KEY_F9:
                return KeyCode.F9;
            case KEY_F10:
                return KeyCode.F10;
            case KEY_F11:
                return KeyCode.F11;
            case KEY_F12:
                return KeyCode.F12;
            case KEY_NUM_LOCK:
                return KeyCode.NUM;
            case KEY_SCROLL_LOCK:
                return KeyCode.UNKNOWN; // FIXME
            case KEY_SEMICOLON:
                return KeyCode.SEMICOLON;
            case KEY_EQUALS:
                return KeyCode.EQUALS;
            case KEY_COMMA:
                return KeyCode.COMMA;
            case KEY_DASH:
                return KeyCode.MINUS;
            case KEY_PERIOD:
                return KeyCode.PERIOD;
            case KEY_FORWARD_SLASH:
                return KeyCode.SLASH;
            case KEY_GRAVE_ACCENT:
                return KeyCode.UNKNOWN; // FIXME
            case KEY_OPEN_BRACKET:
                return KeyCode.LEFT_BRACKET;
            case KEY_BACKSLASH:
                return KeyCode.BACKSLASH;
            case KEY_CLOSE_BRACKET:
                return KeyCode.RIGHT_BRACKET;
            case KEY_SINGLE_QUOTE:
                return KeyCode.APOSTROPHE;
            default:
                return KeyCode.UNKNOWN;
        }
    }

    void reset(){
        justTouched = false;
        if(keyJustPressed){
            keyJustPressed = false;
            justPressedKeys.clear();
        }
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
            if(touched[pointer]){
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
        return touched[pointer];
    }

    @Override
    public long getCurrentEventTime(){
        return currentEventTimeStamp;
    }

    @Override
    public boolean isPeripheralAvailable(Peripheral peripheral){
        if(peripheral == Peripheral.Accelerometer) return false;
        if(peripheral == Peripheral.Compass) return false;
        if(peripheral == Peripheral.HardwareKeyboard) return true;
        if(peripheral == Peripheral.MultitouchScreen) return isTouchScreen();
        if(peripheral == Peripheral.OnscreenKeyboard) return false;
        if(peripheral == Peripheral.Vibrator) return false;
        return false;
    }

    @Override
    public int getRotation(){
        return 0;
    }

    @Override
    public Orientation getNativeOrientation(){
        return Orientation.Landscape;
    }

    /**
     * from https://github.com/toji/game-shim/blob/master/game-shim.js
     * @return is Cursor catched
     */
    private native boolean isCursorCatchedJSNI() /*-{
		if (!navigator.pointer) {
			navigator.pointer = navigator.webkitPointer || navigator.mozPointer;
		}
		if (navigator.pointer) {
			if (typeof (navigator.pointer.isLocked) === "boolean") {
				// Chrome initially launched with this interface
				return navigator.pointer.isLocked;
			} else if (typeof (navigator.pointer.isLocked) === "function") {
				// Some older builds might provide isLocked as a function
				return navigator.pointer.isLocked();
			} else if (typeof (navigator.pointer.islocked) === "function") {
				// For compatibility with early Firefox build
				return navigator.pointer.islocked();
			}
		}
		return false;
	}-*/;

    /**
     * from https://github.com/toji/game-shim/blob/master/game-shim.js
     * @param element Canvas
     */
    private native void setCursorCatchedJSNI(CanvasElement element) /*-{
		// Navigator pointer is not the right interface according to spec.
		// Here for backwards compatibility only
		if (!navigator.pointer) {
			navigator.pointer = navigator.webkitPointer || navigator.mozPointer;
		}
		// element.requestPointerLock
		if (!element.requestPointerLock) {
			element.requestPointerLock = (function() {
				return element.webkitRequestPointerLock
						|| element.mozRequestPointerLock || function() {
							if (navigator.pointer) {
								navigator.pointer.lock(element);
							}
						};
			})();
		}
		element.requestPointerLock();
	}-*/;

    /** from https://github.com/toji/game-shim/blob/master/game-shim.js */
    private native void exitCursorCatchedJSNI() /*-{
		if (!$doc.exitPointerLock) {
			$doc.exitPointerLock = (function() {
				return $doc.webkitExitPointerLock || $doc.mozExitPointerLock
						|| function() {
							if (navigator.pointer) {
								var elem = this;
								navigator.pointer.unlock();
							}
						};
			})();
		}
	}-*/;

    /**
     * from https://github.com/toji/game-shim/blob/master/game-shim.js
     * @param event JavaScript Mouse Event
     * @return movement in x direction
     */
    private native float getMovementXJSNI(NativeEvent event) /*-{
		return event.movementX || event.webkitMovementX || 0;
	}-*/;

    /**
     * from https://github.com/toji/game-shim/blob/master/game-shim.js
     * @param event JavaScript Mouse Event
     * @return movement in y direction
     */
    private native float getMovementYJSNI(NativeEvent event) /*-{
		return event.movementY || event.webkitMovementY || 0;
	}-*/;

    @Override
    public boolean isCursorCatched(){
        return isCursorCatchedJSNI();
    }

    /**
     * works only for Chrome > Version 18 with enabled Mouse Lock enable in about:flags or start Chrome with the
     * --enable-pointer-lock flag
     */
    @Override
    public void setCursorCatched(boolean catched){
        if(catched)
            setCursorCatchedJSNI(canvas);
        else
            exitCursorCatchedJSNI();
    }

    @Override
    public void setCursorPosition(int x, int y){
        // FIXME??
    }

    /** Kindly borrowed from PlayN. **/
    protected int getRelativeX(NativeEvent e, CanvasElement target){
        float xScaleRatio = target.getWidth() * 1f / target.getClientWidth(); // Correct for canvas CSS scaling
        return Math.round(xScaleRatio
        * (e.getClientX() - target.getAbsoluteLeft() + target.getScrollLeft() + target.getOwnerDocument().getScrollLeft()));
    }

    /** Kindly borrowed from PlayN. **/
    protected int getRelativeY(NativeEvent e, CanvasElement target){
        float yScaleRatio = target.getHeight() * 1f / target.getClientHeight(); // Correct for canvas CSS scaling
        return Math.round(yScaleRatio
        * (e.getClientY() - target.getAbsoluteTop() + target.getScrollTop() + target.getOwnerDocument().getScrollTop()));
    }

    protected int getRelativeX(Touch touch, CanvasElement target){
        float xScaleRatio = target.getWidth() * 1f / target.getClientWidth(); // Correct for canvas CSS scaling
        return Math.round(xScaleRatio * touch.getRelativeX(target));
    }

    protected int getRelativeY(Touch touch, CanvasElement target){
        float yScaleRatio = target.getHeight() * 1f / target.getClientHeight(); // Correct for canvas CSS scaling
        return Math.round(yScaleRatio * touch.getRelativeY(target));
    }

    private void hookEvents(){
        addEventListener(canvas, "mousedown", this, true);
        addEventListener(Document.get(), "mousedown", this, true);
        addEventListener(canvas, "mouseup", this, true);
        addEventListener(Document.get(), "mouseup", this, true);
        addEventListener(canvas, "mousemove", this, true);
        addEventListener(Document.get(), "mousemove", this, true);
        addEventListener(canvas, getMouseWheelEvent(), this, true);
        addEventListener(Document.get(), "keydown", this, false);
        addEventListener(Document.get(), "keyup", this, false);
        addEventListener(Document.get(), "keypress", this, false);
        addEventListener(getWindow(), "blur", this, false);

        addEventListener(canvas, "touchstart", this, true);
        addEventListener(canvas, "touchmove", this, true);
        addEventListener(canvas, "touchcancel", this, true);
        addEventListener(canvas, "touchend", this, true);

    }

    private KeyCode getButton(int button){
        if(button == NativeEvent.BUTTON_LEFT) return KeyCode.MOUSE_LEFT;
        if(button == NativeEvent.BUTTON_RIGHT) return KeyCode.MOUSE_RIGHT;
        if(button == NativeEvent.BUTTON_MIDDLE) return KeyCode.MOUSE_MIDDLE;
        return KeyCode.MOUSE_LEFT;
    }

    private void handleEvent(NativeEvent e){
        if(e.getType().equals("mousedown")){
            if(!e.getEventTarget().equals(canvas) || pressedButtons.contains(getButton(e.getButton()).ordinal())){
                float mouseX = getRelativeX(e, canvas);
                float mouseY = getRelativeY(e, canvas);
                if(mouseX < 0 || mouseX > Core.graphics.getWidth() || mouseY < 0 || mouseY > Core.graphics.getHeight()){
                    hasFocus = false;
                }
                return;
            }
            hasFocus = true;
            this.justTouched = true;
            this.touched[0] = true;
            this.pressedButtons.add(getButton(e.getButton()).ordinal());
            this.deltaX[0] = 0;
            this.deltaY[0] = 0;
            if(isCursorCatched()){
                this.touchX[0] += getMovementXJSNI(e);
                this.touchY[0] += getMovementYJSNI(e);
            }else{
                this.touchX[0] = getRelativeX(e, canvas);
                this.touchY[0] = getRelativeY(e, canvas);
            }
            this.currentEventTimeStamp = Time.nanoTime();
            if(processor != null) processor.touchDown(touchX[0], touchY[0], 0, getButton(e.getButton()));
        }

        if(e.getType().equals("mousemove")){
            if(isCursorCatched()){
                this.deltaX[0] = (int)getMovementXJSNI(e);
                this.deltaY[0] = (int)getMovementYJSNI(e);
                this.touchX[0] += getMovementXJSNI(e);
                this.touchY[0] += getMovementYJSNI(e);
            }else{
                this.deltaX[0] = getRelativeX(e, canvas) - touchX[0];
                this.deltaY[0] = getRelativeY(e, canvas) - touchY[0];
                this.touchX[0] = getRelativeX(e, canvas);
                this.touchY[0] = getRelativeY(e, canvas);
            }
            this.currentEventTimeStamp = Time.nanoTime();
            if(processor != null){
                if(touched[0])
                    processor.touchDragged(touchX[0], touchY[0], 0);
                else
                    processor.mouseMoved(touchX[0], touchY[0]);
            }
        }

        if(e.getType().equals("mouseup")){
            if(!pressedButtons.contains(getButton(e.getButton()).ordinal())) return;
            this.pressedButtons.remove(getButton(e.getButton()).ordinal());
            this.touched[0] = pressedButtons.size > 0;
            if(isCursorCatched()){
                this.deltaX[0] = (int)getMovementXJSNI(e);
                this.deltaY[0] = (int)getMovementYJSNI(e);
                this.touchX[0] += getMovementXJSNI(e);
                this.touchY[0] += getMovementYJSNI(e);
            }else{
                this.deltaX[0] = getRelativeX(e, canvas) - touchX[0];
                this.deltaY[0] = getRelativeY(e, canvas) - touchY[0];
                this.touchX[0] = getRelativeX(e, canvas);
                this.touchY[0] = getRelativeY(e, canvas);
            }
            this.currentEventTimeStamp = Time.nanoTime();
            this.touched[0] = false;
            if(processor != null) processor.touchUp(touchX[0], touchY[0], 0, getButton(e.getButton()));
        }
        if(e.getType().equals(getMouseWheelEvent())){
            if(processor != null){
                processor.scrolled(0, (int)getMouseWheelVelocity(e));
            }
            this.currentEventTimeStamp = Time.nanoTime();
            e.preventDefault();
        }

        if(hasFocus && !e.getType().equals("blur")){
            if(e.getType().equals("keydown")){
                // Gdx.app.log("GwtInput", "keydown");
                KeyCode code = keyForCode(e.getKeyCode());
                if(code == KeyCode.BACKSPACE){
                    e.preventDefault();
                    if(processor != null){
                        processor.keyDown(code);
                        processor.keyTyped('\b');
                    }
                }else{
                    if(!pressedKeys.get(code.ordinal())){
                        pressedKeySet.add(code.ordinal());
                        pressedKeyCount++;
                        pressedKeys.set(code.ordinal());
                        keyJustPressed = true;
                        justPressedKeys.set(code.ordinal());
                        if(processor != null){
                            processor.keyDown(code);
                        }
                    }
                }
            }

            if(e.getType().equals("keypress")){
                // Gdx.app.log("GwtInput", "keypress");
                char c = (char)e.getCharCode();
                if(processor != null) processor.keyTyped(c);
            }

            if(e.getType().equals("keyup")){
                // Gdx.app.log("GwtInput", "keyup");
                KeyCode code = keyForCode(e.getKeyCode());
                if(pressedKeys.get(code.ordinal())){
                    pressedKeySet.remove(code.ordinal());
                    pressedKeyCount--;
                    pressedKeys.clear(code.ordinal());
                }
                if(processor != null){
                    processor.keyUp(code);
                }
            }
        }else if(pressedKeyCount > 0){
            IntSetIterator iterator = pressedKeySet.iterator();

            while(iterator.hasNext){
                int code = iterator.next();

                if(pressedKeys.get(code)){
                    pressedKeySet.remove(code);
                    pressedKeyCount--;
                    pressedKeys.clear(code);
                }
                if(processor != null){
                    processor.keyUp(KeyCode.byOrdinal(code));
                }
            }
        }

        if(e.getType().equals("touchstart")){
            this.justTouched = true;
            JsArray<Touch> touches = e.getChangedTouches();
            for(int i = 0, j = touches.length(); i < j; i++){
                Touch touch = touches.get(i);
                int real = touch.getIdentifier();
                int touchId;
                touchMap.put(real, touchId = getAvailablePointer());
                touched[touchId] = true;
                touchX[touchId] = getRelativeX(touch, canvas);
                touchY[touchId] = getRelativeY(touch, canvas);
                deltaX[touchId] = 0;
                deltaY[touchId] = 0;
                if(processor != null){
                    processor.touchDown(touchX[touchId], touchY[touchId], touchId, KeyCode.MOUSE_LEFT);
                }
            }
            this.currentEventTimeStamp = Time.nanoTime();
            e.preventDefault();
        }
        if(e.getType().equals("touchmove")){
            JsArray<Touch> touches = e.getChangedTouches();
            for(int i = 0, j = touches.length(); i < j; i++){
                Touch touch = touches.get(i);
                int real = touch.getIdentifier();
                int touchId = touchMap.get(real);
                deltaX[touchId] = getRelativeX(touch, canvas) - touchX[touchId];
                deltaY[touchId] = getRelativeY(touch, canvas) - touchY[touchId];
                touchX[touchId] = getRelativeX(touch, canvas);
                touchY[touchId] = getRelativeY(touch, canvas);
                if(processor != null){
                    processor.touchDragged(touchX[touchId], touchY[touchId], touchId);
                }
            }
            this.currentEventTimeStamp = Time.nanoTime();
            e.preventDefault();
        }
        if(e.getType().equals("touchcancel")){
            JsArray<Touch> touches = e.getChangedTouches();
            for(int i = 0, j = touches.length(); i < j; i++){
                Touch touch = touches.get(i);
                int real = touch.getIdentifier();
                int touchId = touchMap.get(real);
                touchMap.remove(real);
                touched[touchId] = false;
                deltaX[touchId] = getRelativeX(touch, canvas) - touchX[touchId];
                deltaY[touchId] = getRelativeY(touch, canvas) - touchY[touchId];
                touchX[touchId] = getRelativeX(touch, canvas);
                touchY[touchId] = getRelativeY(touch, canvas);
                if(processor != null){
                    processor.touchUp(touchX[touchId], touchY[touchId], touchId, KeyCode.MOUSE_LEFT);
                }
            }
            this.currentEventTimeStamp = Time.nanoTime();
            e.preventDefault();
        }
        if(e.getType().equals("touchend")){
            JsArray<Touch> touches = e.getChangedTouches();
            for(int i = 0, j = touches.length(); i < j; i++){
                Touch touch = touches.get(i);
                int real = touch.getIdentifier();
                int touchId = touchMap.get(real);
                touchMap.remove(real);
                touched[touchId] = false;
                deltaX[touchId] = getRelativeX(touch, canvas) - touchX[touchId];
                deltaY[touchId] = getRelativeY(touch, canvas) - touchY[touchId];
                touchX[touchId] = getRelativeX(touch, canvas);
                touchY[touchId] = getRelativeY(touch, canvas);
                if(processor != null){
                    processor.touchUp(touchX[touchId], touchY[touchId], touchId, KeyCode.MOUSE_LEFT);
                }
            }
            this.currentEventTimeStamp = Time.nanoTime();
            e.preventDefault();
        }
// if(hasFocus) e.preventDefault();
    }

    private int getAvailablePointer(){
        for(int i = 0; i < MAX_TOUCHES; i++){
            if(!touchMap.containsValue(i, false)) return i;
        }
        return -1;
    }

}
