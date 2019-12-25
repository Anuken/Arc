package arc.input;

public abstract class InputDevice{

    public abstract void update();

    public abstract String name();

    public abstract DeviceType type();

    public abstract boolean isKeyPressed(KeyCode key);

    public abstract boolean isKeyTapped(KeyCode key);

    public abstract boolean isKeyReleased(KeyCode key);

    public abstract float getAxis(KeyCode keyCode);

    public enum DeviceType{
        keyboard, controller
    }
}
