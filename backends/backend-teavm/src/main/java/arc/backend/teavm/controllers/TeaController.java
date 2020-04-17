package arc.backend.teavm.controllers;

import arc.input.*;
import org.teavm.jso.gamepad.*;

/**
 *
 * @author Alexey Andreev
 */
public class TeaController extends Controller{
    public final Gamepad gamepad;

    public TeaController(Gamepad gamepad) {
        this.gamepad = gamepad;
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
        return false;
    }

    @Override
    public boolean isTapped(KeyCode key){
        return false;
    }

    @Override
    public boolean isReleased(KeyCode key){
        return false;
    }

    @Override
    public float getAxis(KeyCode key){
        return 0;
    }
}
