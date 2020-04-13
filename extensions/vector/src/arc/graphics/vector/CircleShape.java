package arc.graphics.vector;

public class CircleShape extends Shape{
    private float centerX;
    private float centerY;
    private float radius;

    public CircleShape(float centerX, float centerY, float radius){
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
    }

    public float getCenterX(){
        return centerX;
    }

    public void setCenterX(float centerX){
        if(this.centerX != centerX){
            this.centerX = centerX;
            markDataChanged();
        }
    }

    public float getCenterY(){
        return centerY;
    }

    public void setCenterY(float centerY){
        if(this.centerY != centerY){
            this.centerY = centerY;
            markDataChanged();
        }
    }

    public float getRadius(){
        return radius;
    }

    public void setRadius(float radius){
        if(this.radius != radius){
            this.radius = radius;
            markDataChanged();
        }
    }

    @Override
    protected void initPath(Path path){
        path.circle(centerX, centerY, radius);
    }
}
