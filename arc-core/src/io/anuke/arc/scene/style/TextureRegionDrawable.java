package io.anuke.arc.scene.style;

import io.anuke.arc.graphics.Color;
import io.anuke.arc.graphics.g2d.Draw;
import io.anuke.arc.graphics.g2d.TextureRegion;
import io.anuke.arc.scene.ui.layout.Unit;
import io.anuke.arc.util.Tmp;

/**
 * Drawable for a {@link TextureRegion}.
 * @author Nathan Sweet
 */
public class TextureRegionDrawable extends BaseDrawable implements TransformDrawable{
    private TextureRegion region;
    private Color tint = new Color(1f, 1f, 1f);

    /** Creates an uninitialized TextureRegionDrawable. The texture region must be set before use. */
    public TextureRegionDrawable(){
    }

    public TextureRegionDrawable(TextureRegion region){
        setRegion(region);
    }

    public TextureRegionDrawable(TextureRegionDrawable drawable){
        super(drawable);
        setRegion(drawable.region);
    }

    @Override
    public void draw(float x, float y, float width, float height){
        Draw.color(Tmp.c1.set(tint).mul(Draw.getColor()).toFloatBits());
        Draw.rect(region, x + width/2f, y + height/2f, width, height);
    }

    //TODO scaling is completely ignored
    @Override
    public void draw(float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation){
        Draw.color(Tmp.c1.set(tint).mul(Draw.getColor()).toFloatBits());
        Draw.rect(region, x + width/2f, y + height/2f, width, height, originX, originY, rotation);
    }

    public TextureRegion getRegion(){
        return region;
    }

    public void setRegion(TextureRegion region){
        this.region = region;
        setMinWidth(Unit.dp.scl(region.getWidth()));
        setMinHeight(Unit.dp.scl(region.getHeight()));
    }

    /** Creates a new drawable that renders the same as this drawable tinted the specified color. */
    public Drawable tint(Color tint){
        TextureRegionDrawable drawable = new TextureRegionDrawable(region);
        drawable.tint.set(tint);
        return drawable;
    }
}
