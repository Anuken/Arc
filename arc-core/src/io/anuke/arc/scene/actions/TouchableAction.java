package io.anuke.arc.scene.actions;

import io.anuke.arc.scene.Action;
import io.anuke.arc.scene.event.Touchable;

/**
 * Sets the actor's {@link Element#touchable(Touchable) touchability}.
 * @author Nathan Sweet
 */
public class TouchableAction extends Action{
    private Touchable touchable;

    public boolean act(float delta){
        target.touchable(touchable);
        return true;
    }

    public Touchable getTouchable(){
        return touchable;
    }

    public void touchable(Touchable touchable){
        this.touchable = touchable;
    }
}
