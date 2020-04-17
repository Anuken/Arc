package arc.input;

public abstract class InputDevice{

    public abstract void update();

    public abstract String name();

    public abstract DeviceType type();

    public abstract boolean isPressed(KeyCode key);

    public abstract boolean isTapped(KeyCode key);

    public abstract boolean isReleased(KeyCode key);

    public abstract float getAxis(KeyCode keyCode);

    public enum DeviceType{
        keyboard, controller
    }
}
