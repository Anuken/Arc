package arc.input;

import arc.struct.IntFloatMap;
import arc.struct.IntSet;

public class KeyboardDevice extends InputDevice implements InputProcessor{
    private final IntSet pressed = new IntSet();
    private final IntSet lastFramePressed = new IntSet();
    private final IntFloatMap axes = new IntFloatMap();

    @Override
    public void update(){
        lastFramePressed.clear();
        lastFramePressed.addAll(pressed);
        axes.clear();
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
        return axes.get(keyCode.ordinal(), 0);
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
        axes.put(KeyCode.SCROLL.ordinal(), -amountY);
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
