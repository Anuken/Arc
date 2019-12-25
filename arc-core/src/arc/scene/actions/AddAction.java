package arc.scene.actions;

import arc.scene.Action;

/**
 * Adds an action to an actor.
 * @author Nathan Sweet
 */
public class AddAction extends Action{
    private Action action;

    public boolean act(float delta){
        target.addAction(action);
        return true;
    }

    public Action getAction(){
        return action;
    }

    public void setAction(Action action){
        this.action = action;
    }

    public void restart(){
        if(action != null) action.restart();
    }

    public void reset(){
        super.reset();
        action = null;
    }
}
