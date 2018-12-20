package io.anuke.arc.scene.actions;

import io.anuke.arc.scene.Action;
import io.anuke.arc.utils.Align;

public class OriginAction extends Action{

    @Override
    public boolean act(float delta){
        actor.setOrigin(Align.center);
        return true;
    }

}
