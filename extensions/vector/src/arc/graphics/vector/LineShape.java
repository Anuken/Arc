package arc.graphics.vector;

public class LineShape extends Shape{
    private float startX;
    private float startY;
    private float endX;
    private float endY;

    private LineShape(float startX, float startY, float endX, float endY){
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    public float getStartX(){
        return startX;
    }

    public void setStartX(float startX){
        if(this.startX != startX){
            this.startX = startX;
            markDataChanged();
        }
    }

    public float getStartY(){
        return startY;
    }

    public void setStartY(float startY){
        if(this.startY != startY){
            this.startY = startY;
            markDataChanged();
        }
    }

    public float getEndX(){
        return endX;
    }

    public void setEndX(float endX){
        if(this.endX != endX){
            this.endX = endX;
            markDataChanged();
        }
    }

    public float getEndY(){
        return endY;
    }

    public void setEndY(float endY){
        if(this.endY != endY){
            this.endY = endY;
            markDataChanged();
        }
    }

    @Override
    protected void initPath(Path path){
        path.moveTo(startX, startY).lineTo(endX, endY);
    }
}
