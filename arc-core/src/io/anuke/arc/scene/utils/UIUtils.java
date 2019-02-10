package io.anuke.arc.scene.utils;

import io.anuke.arc.Core;
import io.anuke.arc.input.KeyCode;
import io.anuke.arc.scene.Element;
import io.anuke.arc.util.OS;

//TODO remove this class?
public class UIUtils{

    public static boolean isDisabled(Element element){
        return element != null && (((element instanceof Disableable && ((Disableable)element).isDisabled()) || !element.isVisible()) || isDisabled(element.getParent()));
    }

    public static boolean portrait(){
        return Core.graphics.getHeight() > Core.graphics.getWidth();
    }

    public static boolean left(){
        return Core.input.keyDown(KeyCode.MOUSE_LEFT);
    }

    public static boolean left(KeyCode button){
        return button == KeyCode.MOUSE_LEFT;
    }

    public static boolean right(){
        return Core.input.keyDown(KeyCode.MOUSE_RIGHT);
    }

    public static boolean right(KeyCode button){
        return button == KeyCode.MOUSE_RIGHT;
    }

    public static boolean middle(){
        return Core.input.keyDown(KeyCode.MOUSE_MIDDLE);
    }

    public static boolean middle(KeyCode button){
        return button == KeyCode.MOUSE_MIDDLE;
    }

    public static boolean shift(){
        return Core.input.keyDown(KeyCode.SHIFT_LEFT) || Core.input.keyDown(KeyCode.SHIFT_RIGHT);
    }

    public static boolean shift(KeyCode keycode){
        return keycode == KeyCode.SHIFT_LEFT || keycode == KeyCode.SHIFT_RIGHT;
    }

    public static boolean ctrl(){
        if(OS.isMac)
            return Core.input.keyDown(KeyCode.SYM);
        else
            return Core.input.keyDown(KeyCode.CONTROL_LEFT) || Core.input.keyDown(KeyCode.CONTROL_RIGHT);
    }

    public static boolean ctrl(KeyCode keycode){
        if(OS.isMac)
            return keycode == KeyCode.SYM;
        else
            return keycode == KeyCode.CONTROL_LEFT || keycode == KeyCode.CONTROL_RIGHT;
    }

    public static boolean alt(){
        return Core.input.keyDown(KeyCode.ALT_LEFT) || Core.input.keyDown(KeyCode.ALT_RIGHT);
    }

    public static boolean alt(KeyCode keycode){
        return keycode == KeyCode.ALT_LEFT || keycode == KeyCode.ALT_RIGHT;
    }
}
