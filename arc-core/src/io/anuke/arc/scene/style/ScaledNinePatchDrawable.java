package io.anuke.arc.scene.style;

import io.anuke.arc.graphics.g2d.NinePatch;
import io.anuke.arc.scene.ui.layout.Unit;

public class ScaledNinePatchDrawable extends NinePatchDrawable{
    private float scale = Unit.dp.scl(1f);

    public ScaledNinePatchDrawable(NinePatch patch){
        super(patch);
    }

    public ScaledNinePatchDrawable(NinePatchDrawable drawable){
        super(drawable);
    }

    @Override
    public void draw(float x, float y, float width, float height){
        getPatch().draw(x, y, 0, 0, width / scale, height / scale, scale, scale, 0);
    }

    @Override
    public void setPatch(NinePatch patch){
        super.setPatch(patch);

        setMinWidth(patch.getTotalWidth() * scale);
        setMinHeight(patch.getTotalHeight() * scale);
    }

    public float getLeftWidth(){
        return patch.getPadLeft() * scale;
    }

    public float getRightWidth(){
        return patch.getPadRight() * scale;
    }

    public float getTopHeight(){
        return patch.getPadTop() * scale;
    }

    public float getBottomHeight(){
        return patch.getPadBottom() * scale;
    }

}
