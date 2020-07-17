package arc.backend.teavm;

import arc.*;
import arc.input.*;
import arc.struct.*;
import org.teavm.jso.gamepad.*;

public class TeaController extends Controller{
    private static final KeyCode[] buttonToKey = new KeyCode[16];
    private static final float deadzone = 0.2f;

    public Gamepad gamepad;

    private final IntSet pressed = new IntSet(), lastFramePressed = new IntSet();

    static{
        for(KeyCode code : KeyCode.all){
            int button = toButton(code);
            if(button >= 0 && button < buttonToKey.length){
                buttonToKey[button] = code;
            }
        }
    }

    public TeaController(Gamepad gamepad) {
        this.gamepad = gamepad;
    }

    @Override
    public void preUpdate(){
        pressed.clear();

        for(int i = 0; i < gamepad.getButtons().length; i++){
            GamepadButton button = gamepad.getButtons()[i];
            KeyCode key = toKey(i);

            if(button.isPressed()){
                pressed.add(key.ordinal());
            }
        }

        InputMultiplexer plex = Core.input.getInputMultiplexer();

        //fire input events to processors
        for(KeyCode code : buttonToKey){
            if(isPressed(code)){
                plex.keyDown(code);
            }

            if(isReleased(code)){
                plex.keyUp(code);
            }
        }
    }

    @Override
    public void postUpdate(){
        lastFramePressed.clear();
        lastFramePressed.addAll(pressed);
    }

    @Override
    public int index() {
        return gamepad.getIndex();
    }

    @Override
    public String name(){
        return gamepad.getId();
    }

    @Override
    public boolean isPressed(KeyCode key){
        if(key == KeyCode.anyKey) return pressed.size > 0;

        return pressed.contains(key.ordinal());
    }

    @Override
    public boolean isTapped(KeyCode key){
        return isPressed(key) && !lastFramePressed.contains(key.ordinal());
    }

    @Override
    public boolean isReleased(KeyCode key){
        return !isPressed(key) && lastFramePressed.contains(key.ordinal());
    }

    @Override
    public float getAxis(KeyCode key){
        int index = -1;
        int sign = 1; //Y axis is flipped

        if(key == KeyCode.controllerLStickXAxis) index = 0;
        if(key == KeyCode.controllerLStickYAxis){
            index = 1;
            sign = -1;
        }
        if(key == KeyCode.controllerRStickXAxis) index = 2;
        if(key == KeyCode.controllerRStickYAxis){
            index = 3;
            sign = -1;
        }

        if(index == -1){
            int button = toButton(key);
            return button == -1 ? 0 : (float)gamepad.getButtons()[button].getValue();
        }

        return Math.abs((float)gamepad.getAxes()[index]) <= deadzone ? 0 : (float)gamepad.getAxes()[index] * sign;
    }

    public static KeyCode toKey(int button){
        if(button < 0 || button >= buttonToKey.length || buttonToKey[button] == null){
            return KeyCode.unknown;
        }
        return buttonToKey[button];
    }

    public static int toButton(KeyCode key){
        switch(key){
            case controllerA: return 0;
            case controllerB: return 1;
            case controllerX: return 2;
            case controllerY: return 3;

            case controllerStart: return 9;
            case controllerBack: return 8;
            case controllerGuide: return 16;

            case controllerLBumper: return 4;
            case controllerLTrigger: return 6;

            case controllerRBumper: return 5;
            case controllerRTrigger: return 7;

            case controllerdPadLeft: return 14;
            case controllerdPadRight: return 15;
            case controllerdPadUp: return 12;
            case controllerdPadDown: return 13;

            case controllerRStick: return 11;
            case controllerLStick: return 10;
            default: return -1;
        }
    }
}
