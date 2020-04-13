package arc.graphics.vector;

public class RectangleShape extends Shape{
    private float left;
    private float top;
    private float width;
    private float height;

    public RectangleShape(float left, float top, float width, float height){
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    public float getLeft(){
        return left;
    }

    public void setLeft(float left){
        if(this.left != left){
            this.left = left;
            markDataChanged();
        }
    }

    public float getTop(){
        return top;
    }

    public void setTop(float top){
        if(this.top != top){
            this.top = top;
            markDataChanged();
        }
    }

    public float getWidth(){
        return width;
    }

    public void setWidth(float width){
        if(this.width != width){
            this.width = width;
            markDataChanged();
        }
    }

    public float getHeight(){
        return height;
    }

    public void setHeight(float height){
        if(this.height != height){
            this.height = height;
            markDataChanged();
        }
    }

    @Override
    protected void initPath(Path path){
        path.rect(left, top, width, height);
    }
}