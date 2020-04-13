package arc.graphics.vector;

public class EllipseShape extends Shape{
    private float centerX;
    private float centerY;
    private float radiusX;
    private float radiusY;

    public EllipseShape(float centerX, float centerY, float radiusX, float radiusY){
        this.centerX = centerX;
        this.centerY = centerY;
        this.radiusX = radiusX;
        this.radiusY = radiusY;
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

    public float getRadiusX(){
        return radiusX;
    }

    public void setRadiusX(float radiusX){
        if(this.radiusX != radiusX){
            this.radiusX = radiusX;
            markDataChanged();
        }
    }

    public float getRadiusY(){
        return radiusY;
    }

    public void setRadiusY(float radiusY){
        if(this.radiusY != radiusY){
            this.radiusY = radiusY;
            markDataChanged();
        }
    }

    @Override
    protected void initPath(Path path){
        path.ellipse(centerX, centerY, radiusX, radiusY);
    }
}
