package io.anuke.arc.scene.actions;

import io.anuke.arc.collection.Array;
import io.anuke.arc.scene.Action;
import io.anuke.arc.scene.Element;

/**
 * Executes an action only after all other actions on the actor at the time this action's target was set have finished.
 * @author Nathan Sweet
 */
public class AfterAction extends DelegateAction{
    private Array<Action> waitForActions = new Array<>(false, 4);

    public void setTarget(Element target){
        if(target != null) waitForActions.addAll(target.getActions());
        super.setTarget(target);
    }

    public void restart(){
        super.restart();
        waitForActions.clear();
    }

    protected boolean delegate(float delta){
        Array<Action> currentActions = target.getActions();
        if(currentActions.size == 1) waitForActions.clear();
        for(int i = waitForActions.size - 1; i >= 0; i--){
            Action action = waitForActions.get(i);
            int index = currentActions.indexOf(action, true);
            if(index == -1) waitForActions.removeAt(i);
        }
        return waitForActions.size <= 0 && action.act(delta);
    }
}
