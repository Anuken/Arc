package arc.scene.actions;

import arc.graphics.Color;

/**
 * Sets the alpha for an actor's color (or a specified color), from the current alpha to the new alpha. Note this action
 * transitions from the alpha at the time the action starts to the specified alpha.
 * @author Nathan Sweet
 */
public class AlphaAction extends TemporalAction{
    private float start, end;
    private Color color;

    protected void begin(){
        if(color == null) color = target.getColor();
        start = color.a;
    }

    protected void update(float percent){
        color.a = start + (end - start) * percent;
    }

    public void reset(){
        super.reset();
        color = null;
    }

    public Color getColor(){
        return color;
    }

    /**
     * Sets the color to modify. If null (the default), the {@link #getActor() actor's} {@link Element#getColor() color} will be
     * used.
     */
    public void setColor(Color color){
        this.color = color;
    }

    public float getAlpha(){
        return end;
    }

    public void setAlpha(float alpha){
        this.end = alpha;
    }
}
