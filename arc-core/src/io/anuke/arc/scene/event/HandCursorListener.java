package io.anuke.arc.scene.event;

import io.anuke.arc.Core;
import io.anuke.arc.Graphics.Cursor.SystemCursor;
import io.anuke.arc.func.Boolp;
import io.anuke.arc.scene.Element;
import io.anuke.arc.scene.utils.Disableable;

public class HandCursorListener extends ClickListener{
    private Boolp enabled = () -> true;

    public void setEnabled(Boolp vis){
        this.enabled = vis;
    }

    @Override
    public void enter(InputEvent event, float x, float y, int pointer, Element fromActor){
        super.enter(event, x, y, pointer, fromActor);

        if(pointer != -1 || !enabled.get() || isDisabled(event.targetActor) || isDisabled(fromActor)){
            return;
        }

        Core.graphics.cursor(SystemCursor.hand);
    }

    @Override
    public void exit(InputEvent event, float x, float y, int pointer, Element toActor){
        super.exit(event, x, y, pointer, toActor);

        if(pointer == -1){
            Core.graphics.restoreCursor();
        }
    }

    static boolean isDisabled(Element element){
        return element != null && (((element instanceof Disableable && ((Disableable)element).isDisabled()) || !element.isVisible()) || isDisabled(element.getParent()));
    }
}
