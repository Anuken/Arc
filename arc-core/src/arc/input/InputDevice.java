package arc.input;

public abstract class InputDevice{

    /** Called at the end of the update loop. */
    public void postUpdate(){

    }

    /** Called at the start of the update loop. */
    public void preUpdate(){

    }

    public abstract String name();

    public abstract DeviceType type();

    /** @return whether the button is currently pressed. */
    public abstract boolean isPressed(KeyCode key);

    /** @return whether button was pressed down this frame. */
    public abstract boolean isTapped(KeyCode key);

    /** @return whether this button was released this frame. */
    public abstract boolean isReleased(KeyCode key);

    /** @return an axis tilt value, usually -1 to 1; 0 for non-axes. */
    public abstract float getAxis(KeyCode keyCode);

    public enum DeviceType{
        keyboard, controller
    }
}
