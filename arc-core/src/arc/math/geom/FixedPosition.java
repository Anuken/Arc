package arc.math.geom;

public class FixedPosition implements Position{
    private final float x, y;

    public FixedPosition(float x, float y){
        this.x = x;
        this.y = y;
    }

    @Override
    public float getX(){
        return x;
    }

    @Override
    public float getY(){
        return y;
    }
}
