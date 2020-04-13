package arc.graphics.vector;

import arc.util.pooling.Pool.*;
import arc.util.pooling.*;

public class Vertex implements Poolable{
    float x, y, u, v;

    public Vertex(){
    }

    public static Vertex obtain(){
        return Pools.obtain(Vertex.class, Vertex::new);
    }

    public static Vertex obtain(float x, float y, float u, float v){
        return obtain().set(x, y, u, v);
    }

    public static Vertex obtain(float x, float y, float u, float v, AffineTransform xform){
        return obtain().set(x, y, u, v).transform(xform);
    }

    public static Vertex obtain(Vertex other){
        return obtain().set(other);
    }

    public static Vertex obtain(Vertex other, AffineTransform xform){
        return obtain().set(other).transform(xform);
    }

    public Vertex set(Vertex vert){
        this.x = vert.x;
        this.y = vert.y;
        this.u = vert.u;
        this.v = vert.v;
        return this;
    }

    public Vertex set(float x, float y, float u, float v){
        this.x = x;
        this.y = y;
        this.u = u;
        this.v = v;
        return this;
    }

    public Vertex transform(AffineTransform xform){
        if(!xform.isIdentity()){
            float[] xformValues = xform.values;
            float tempX = x * xformValues[0] + y * xformValues[2] + xformValues[4];
            y = x * xformValues[1] + y * xformValues[3] + xformValues[5];
            x = tempX;
        }
        return this;
    }

    @Override
    public void reset(){
        x = 0;
        y = 0;
        u = 0;
        v = 0;
    }

    public void free(){
        Pools.free(this);
    }

    @Override
    public int hashCode(){
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(u);
        result = prime * result + Float.floatToIntBits(v);
        result = prime * result + Float.floatToIntBits(x);
        result = prime * result + Float.floatToIntBits(y);
        return result;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        Vertex other = (Vertex)obj;
        return x == other.x && y == other.y && u == other.u && v == other.v;
    }

    @Override
    public String toString(){
        return "Vertex[x= " + x + ", y=" + y + ", u=" + u + ", v=" + v + "]";
    }
}
