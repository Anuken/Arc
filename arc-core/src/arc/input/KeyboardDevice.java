package arc.input;

import arc.struct.*;

public class KeyboardDevice extends InputDevice implements InputProcessor{
    private final IntSet pressed = new IntSet();
    private final IntSet lastFramePressed = new IntSet();
    private final IntSet justPressed = new IntSet();
    private final IntFloatMap axes = new IntFloatMap();

    @Override
    public void postUpdate(){
        lastFramePressed.clear();
        lastFramePressed.addAll(pressed);
        justPressed.clear();
        axes.clear();
    }

    @Override
    public boolean isPressed(KeyCode key){
        if(key == KeyCode.anyKey) return pressed.size > 0;

        return pressed.contains(key.ordinal());
    }

    @Override
    public boolean isTapped(KeyCode key){
        return justPressed.contains(key.ordinal());
    }

    @Override
    public boolean isReleased(KeyCode key){
        return !isPressed(key) && lastFramePressed.contains(key.ordinal());
    }

    @Override
    public float getAxis(KeyCode keyCode){
        return axes.get(keyCode.ordinal(), 0);
    }

    @Override
    public boolean keyDown(KeyCode key){
        pressed.add(key.ordinal());
        justPressed.add(key.ordinal());
        return false;
    }

    @Override
    public boolean keyUp(KeyCode key){
        pressed.remove(key.ordinal());
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
        axes.put(KeyCode.scroll.ordinal(), -amountY);
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
