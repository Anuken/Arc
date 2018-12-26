package io.anuke.arc.scene.event;

import io.anuke.arc.Core;
import io.anuke.arc.function.BooleanProvider;
import io.anuke.arc.Graphics.Cursor.SystemCursor;
import io.anuke.arc.scene.Element;
import io.anuke.arc.scene.utils.UIUtils;

public class HandCursorListener extends ClickListener{
    private BooleanProvider enabled = () -> true;
    private boolean set;

    public void setEnabled(BooleanProvider vis){
        this.enabled = vis;
    }

    @Override
    public void enter(InputEvent event, float x, float y, int pointer, Element fromActor){
        super.enter(event, x, y, pointer, fromActor);

        if(!enabled.get() || UIUtils.isDisabled(event.targetActor) || UIUtils.isDisabled(fromActor) || pointer != -1){
            return;
        }

        Core.graphics.cursor(SystemCursor.hand);
        set = true;
    }

    @Override
    public void exit(InputEvent event, float x, float y, int pointer, Element toActor){
        super.exit(event, x, y, pointer, toActor);

        if(!enabled.get() || !set) return;

        if(pointer == -1){
            Core.graphics.restoreCursor();
        }
        set = false;
    }
}
