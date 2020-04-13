package arc.graphics.vector;

public class ArcShape extends Shape{
    private float centerX;
    private float centerY;
    private float radiusX;
    private float radiusY;
    private float startAngleRad;
    private float endAngleRad;
    private Direction direction;
    private ArcType type;

    public ArcShape(float centerX, float centerY, float radius, float startAngleRad, float endAngleRad,
                    Direction direction, ArcType type){
        this(centerX, centerY, radius, radius, startAngleRad, endAngleRad, direction, type);
    }

    /*
     * TODO public ArcShape(float left, float top, float width, float height, float startAngleRad, float endAngleRad,
     * Direction direction, ArcType type) { this.centerX = centerX; this.centerY = centerY; this.radiusX = radiusX;
     * this.radiusY = radiusY; this.startAngleRad = startAngleRad; this.endAngleRad = endAngleRad; this.direction =
     * direction == null ? Direction.clockwise : direction; this.type = type == null ? ArcType.OPEN : type; }
     */

    public ArcShape(float centerX, float centerY, float radiusX, float radiusY, float startAngleRad, float endAngleRad,
                    Direction direction, ArcType type){
        this.centerX = centerX;
        this.centerY = centerY;
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.startAngleRad = startAngleRad;
        this.endAngleRad = endAngleRad;
        this.direction = direction == null ? Direction.clockwise : direction;
        this.type = type == null ? ArcType.OPEN : type;
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

    public float getStartAngleRad(){
        return startAngleRad;
    }

    public void setStartAngleRad(float startAngleRad){
        if(this.startAngleRad != startAngleRad){
            this.startAngleRad = startAngleRad;
            markDataChanged();
        }
    }

    public float getEndAngleRad(){
        return endAngleRad;
    }

    public void setEndAngleRad(float endAngleRad){
        if(this.endAngleRad != endAngleRad){
            this.endAngleRad = endAngleRad;
            markDataChanged();
        }
    }

    public Direction getDirection(){
        return direction;
    }

    public void setDirection(Direction direction){
        Direction resolvedDirection = direction == null ? Direction.clockwise : direction;
        if(this.direction != resolvedDirection){
            this.direction = resolvedDirection;
            markDataChanged();
        }
    }

    public ArcType getType(){
        return type;
    }

    public void setType(ArcType type){
        ArcType resolvedType = type == null ? ArcType.OPEN : type;
        if(this.type != resolvedType){
            this.type = resolvedType;
            markDataChanged();
        }
    }

    @Override
    protected void initPath(Path path){
        switch(type){
            case OPEN:
                path.arc(centerX, centerY, radiusX, radiusY, startAngleRad, endAngleRad, direction);
                break;
            case PIE:
                path.moveTo(centerX, centerY);
                path.arc(centerX, centerY, radiusX, radiusY, startAngleRad, endAngleRad, direction);
                path.lineTo(centerX, centerY);
                break;
            case CHORD:
                path.arc(centerX, centerY, radiusX, radiusY, startAngleRad, endAngleRad, direction);
                path.close();
                break;
        }
    }

    public enum ArcType{
        OPEN, CHORD, PIE
    }
}
