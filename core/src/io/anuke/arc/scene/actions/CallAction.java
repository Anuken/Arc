package io.anuke.arc.scene.actions;

import io.anuke.arc.scene.Action;

public class CallAction extends Action{
    public Runnable call;
    public boolean called = false;

    @Override
    public boolean act(float delta){
        if(!called) call.run();
        called = true;
        return true;
    }

    @Override
    public void reset(){
        called = false;
    }
}
