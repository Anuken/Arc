package io.anuke.arc.input;

import io.anuke.arc.collection.IntSet;

public class KeyboardDevice extends InputDevice implements InputProcessor{
    public final IntSet pressed = new IntSet();
    public final IntSet lastFramePressed = new IntSet();
    public final float[] axes = new float[KeyCode.values().length];

    @Override
    public void update(){
        lastFramePressed.clear();
        lastFramePressed.addAll(pressed);
    }

    @Override
    public boolean isKeyPressed(KeyCode key){
        if(key == KeyCode.ANY_KEY){
            return pressed.size > 0;
        }
        return pressed.contains(key.ordinal());
    }

    @Override
    public boolean isKeyTapped(KeyCode key){
        return isKeyPressed(key) && !lastFramePressed.contains(key.ordinal());
    }

    @Override
    public boolean isKeyReleased(KeyCode key){
        return !isKeyPressed(key) && lastFramePressed.contains(key.ordinal());
    }

    @Override
    public float getAxis(KeyCode keyCode){
        return axes[keyCode.ordinal()];
    }

    @Override
    public boolean keyDown(KeyCode keycode){
        pressed.add(keycode.ordinal());
        return false;
    }

    @Override
    public boolean keyUp(KeyCode keycode){
        pressed.remove(keycode.ordinal());
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, KeyCode button){
        keyDown(button);
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, KeyCode button){
        if(pointer == 0) keyUp(button);
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY){
        axes[KeyCode.SCROLL.ordinal()] = amountY;
        return false;
    }

    @Override
    public String name(){
        return "Keyboard";
    }

    @Override
    public DeviceType type(){
        return DeviceType.keyboard;
    }
}
