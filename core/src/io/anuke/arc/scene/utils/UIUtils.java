package io.anuke.arc.scene.utils;

import io.anuke.arc.Core;
import io.anuke.arc.input.KeyCode;
import io.anuke.arc.scene.Element;
import io.anuke.arc.utils.OS;

public class UIUtils{

    static public boolean isDisabled(Element element){
        return element != null && ((element instanceof Disableable && ((Disableable)element).isDisabled()) || !element.isVisible());
    }

    static public boolean portrait(){
        return Core.graphics.getHeight() > Core.graphics.getWidth();
    }

    static public boolean left(){
        return Core.input.keyPress(KeyCode.MOUSE_LEFT);
    }

    static public boolean left(KeyCode button){
        return button == KeyCode.MOUSE_LEFT;
    }

    static public boolean right(){
        return Core.input.keyPress(KeyCode.MOUSE_RIGHT);
    }

    static public boolean right(KeyCode button){
        return button == KeyCode.MOUSE_RIGHT;
    }

    static public boolean middle(){
        return Core.input.keyPress(KeyCode.MOUSE_MIDDLE);
    }

    static public boolean middle(KeyCode button){
        return button == KeyCode.MOUSE_MIDDLE;
    }

    static public boolean shift(){
        return Core.input.keyPress(KeyCode.SHIFT_LEFT) || Core.input.keyPress(KeyCode.SHIFT_RIGHT);
    }

    static public boolean shift(KeyCode keycode){
        return keycode == KeyCode.SHIFT_LEFT || keycode == KeyCode.SHIFT_RIGHT;
    }

    static public boolean ctrl(){
        if(OS.isMac)
            return Core.input.keyPress(KeyCode.SYM);
        else
            return Core.input.keyPress(KeyCode.CONTROL_LEFT) || Core.input.keyPress(KeyCode.CONTROL_RIGHT);
    }

    static public boolean ctrl(KeyCode keycode){
        if(OS.isMac)
            return keycode == KeyCode.SYM;
        else
            return keycode == KeyCode.CONTROL_LEFT || keycode == KeyCode.CONTROL_RIGHT;
    }

    static public boolean alt(){
        return Core.input.keyPress(KeyCode.ALT_LEFT) || Core.input.keyPress(KeyCode.ALT_RIGHT);
    }

    static public boolean alt(KeyCode keycode){
        return keycode == KeyCode.ALT_LEFT || keycode == KeyCode.ALT_RIGHT;
    }
}
