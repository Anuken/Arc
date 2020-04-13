package arc.graphics.vector;

public enum DrawingStyle{
    fill, stroke, fillAndStroke;

    public static DrawingStyle valueOf(boolean hasFill, boolean hasStroke){
        return hasFill ? hasStroke ? fillAndStroke : fill : hasStroke ? stroke : null;
    }

    public boolean drawFill(){
        return this != stroke;
    }

    public boolean drawStroke(){
        return this != fill;
    }
}
