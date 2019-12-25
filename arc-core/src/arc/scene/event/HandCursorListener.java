package arc.scene.event;

import arc.Core;
import arc.Graphics.Cursor.SystemCursor;
import arc.func.Boolp;
import arc.scene.Element;
import arc.scene.utils.Disableable;

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
