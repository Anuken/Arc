package arc.scene.actions;

/**
 * Moves an actor from its current size to a specific size.
 * @author Nathan Sweet
 */
public class SizeToAction extends TemporalAction{
    private float startWidth, startHeight;
    private float endWidth, endHeight;

    protected void begin(){
        startWidth = target.getWidth();
        startHeight = target.getHeight();
    }

    protected void update(float percent){
        target.setSize(startWidth + (endWidth - startWidth) * percent, startHeight + (endHeight - startHeight) * percent);
    }

    public void setSize(float width, float height){
        endWidth = width;
        endHeight = height;
    }

    public float getWidth(){
        return endWidth;
    }

    public void setWidth(float width){
        endWidth = width;
    }

    public float getHeight(){
        return endHeight;
    }

    public void setHeight(float height){
        endHeight = height;
    }
}
