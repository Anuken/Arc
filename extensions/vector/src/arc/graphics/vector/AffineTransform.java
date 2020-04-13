package arc.graphics.vector;

import arc.math.*;
import arc.math.geom.*;
import arc.util.pooling.Pool.*;
import arc.util.pooling.*;

import java.util.*;

/**
 * <pre>
 *          [   0    2    4  ]
 *          [   1    3    5  ]
 *          [   0    0    1  ]
 * </pre>
 */
public class AffineTransform implements Poolable{
    public float[] values = new float[6];
    private float[] tmp = new float[6];

    public AffineTransform(){
        idt();
    }

    public AffineTransform(AffineTransform other){
        set(other);
    }

    public static AffineTransform obtain(){
        return Pools.obtain(AffineTransform.class, AffineTransform::new);
    }

    private static void mul(float[] transformA, float[] transformB){
        float t0 = transformA[0] * transformB[0] + transformA[1] * transformB[2];
        float t2 = transformA[2] * transformB[0] + transformA[3] * transformB[2];
        float t4 = transformA[4] * transformB[0] + transformA[5] * transformB[2] + transformB[4];
        transformA[1] = transformA[0] * transformB[1] + transformA[1] * transformB[3];
        transformA[3] = transformA[2] * transformB[1] + transformA[3] * transformB[3];
        transformA[5] = transformA[4] * transformB[1] + transformA[5] * transformB[3] + transformB[5];
        transformA[0] = t0;
        transformA[2] = t2;
        transformA[4] = t4;
    }

    private static void mulLeft(float[] transformA, float[] transformB){
        float t0 = transformB[0] * transformA[0] + transformB[1] * transformA[2];
        float t1 = transformB[0] * transformA[1] + transformB[1] * transformA[3];
        float t2 = transformB[2] * transformA[0] + transformB[3] * transformA[2];
        float t3 = transformB[2] * transformA[1] + transformB[3] * transformA[3];
        float t4 = transformB[4] * transformA[0] + transformB[5] * transformA[2] + transformA[4];
        float t5 = transformB[4] * transformA[1] + transformB[5] * transformA[3] + transformA[5];
        transformA[0] = t0;
        transformA[1] = t1;
        transformA[2] = t2;
        transformA[3] = t3;
        transformA[4] = t4;
        transformA[5] = t5;
    }

    public AffineTransform set(AffineTransform other){
        values[0] = other.values[0];
        values[1] = other.values[1];
        values[2] = other.values[2];
        values[3] = other.values[3];
        values[4] = other.values[4];
        values[5] = other.values[5];
        return this;
    }

    public AffineTransform set(float a, float b, float c, float d, float e, float f){
        values[0] = a;
        values[1] = b;
        values[2] = c;
        values[3] = d;
        values[4] = e;
        values[5] = f;
        return this;
    }

    public AffineTransform idt(){
        values[0] = 1.0f;
        values[1] = 0.0f;
        values[2] = 0.0f;
        values[3] = 1.0f;
        values[4] = 0.0f;
        values[5] = 0.0f;
        return this;
    }

    public boolean isIdentity(){
        return values[0] == 1.0f && values[1] == 0.0f && values[2] == 0.0f && values[3] == 1.0f && values[4] == 0.0f
        && values[5] == 0.0f;
    }

    public AffineTransform setToTranslation(float tx, float ty){
        values[0] = 1.0f;
        values[1] = 0.0f;
        values[2] = 0.0f;
        values[3] = 1.0f;
        values[4] = tx;
        values[5] = ty;
        return this;
    }

    public AffineTransform setToScaling(float sx, float sy){
        values[0] = sx;
        values[1] = 0.0f;
        values[2] = 0.0f;
        values[3] = sy;
        values[4] = 0.0f;
        values[5] = 0.0f;
        return this;
    }

    public AffineTransform setToRotation(float degrees){
        return setToRotationRad(Mathf.degreesToRadians * degrees);
    }

    public AffineTransform setToRotationRad(float radians){
        if(radians == 0){
            return this;
        }
        float cs = Mathf.cos(radians), sn = Mathf.sin(radians);
        values[0] = cs;
        values[1] = sn;
        values[2] = -sn;
        values[3] = cs;
        values[4] = 0.0f;
        values[5] = 0.0f;
        return this;
    }

    public AffineTransform setToSkewX(float degrees){
        return setToSkewXRad(Mathf.degreesToRadians * degrees);
    }

    public AffineTransform setToSkewXRad(float radians){
        if(radians == 0){
            return this;
        }
        values[0] = 1.0f;
        values[1] = 0.0f;
        values[2] = CanvasUtils.tanf(radians);
        values[3] = 1.0f;
        values[4] = 0.0f;
        values[5] = 0.0f;
        return this;
    }

    public AffineTransform setToSkewY(float degrees){
        return setToSkewYRad(Mathf.degreesToRadians * degrees);
    }

    public AffineTransform setToSkewYRad(float radians){
        if(radians == 0){
            return this;
        }
        values[0] = 1.0f;
        values[1] = CanvasUtils.tanf(radians);
        values[2] = 0.0f;
        values[3] = 1.0f;
        values[4] = 0.0f;
        values[5] = 0.0f;
        return this;
    }

    public AffineTransform setToSkew(float degreesX, float degreesY){
        return setToSkewRad(Mathf.degreesToRadians * degreesX, Mathf.degreesToRadians * degreesY);
    }

    public AffineTransform setToSkewRad(float radiansX, float radiansY){
        if(radiansX == 0 && radiansY == 0)
            return this;
        values[0] = 1.0f;
        values[1] = CanvasUtils.tanf(radiansY);
        values[2] = CanvasUtils.tanf(radiansX);
        values[3] = 1.0f;
        values[4] = 0.0f;
        values[5] = 0.0f;
        return this;
    }

    public AffineTransform mul(AffineTransform other){
        if(other.isIdentity()){
            return this;
        }

        float t0 = values[0] * other.values[0] + values[1] * other.values[2];
        float t2 = values[2] * other.values[0] + values[3] * other.values[2];
        float t4 = values[4] * other.values[0] + values[5] * other.values[2] + other.values[4];
        values[1] = values[0] * other.values[1] + values[1] * other.values[3];
        values[3] = values[2] * other.values[1] + values[3] * other.values[3];
        values[5] = values[4] * other.values[1] + values[5] * other.values[3] + other.values[5];
        values[0] = t0;
        values[2] = t2;
        values[4] = t4;
        return this;
    }

    public AffineTransform mulLeft(AffineTransform other){
        if(other.isIdentity()){
            return this;
        }

        float t0 = other.values[0] * values[0] + other.values[1] * values[2];
        float t2 = other.values[2] * values[0] + other.values[3] * values[2];
        float t4 = other.values[4] * values[0] + other.values[5] * values[2] + values[4];
        values[1] = other.values[0] * values[1] + other.values[1] * values[3];
        values[3] = other.values[2] * values[1] + other.values[3] * values[3];
        values[5] = other.values[4] * values[1] + other.values[5] * values[3] + values[5];
        values[0] = t0;
        values[2] = t2;
        values[4] = t4;
        return this;
    }

    public boolean inv(AffineTransform inv){
        double det = (double)values[0] * values[3] - (double)values[2] * values[1];

        if(det > -1e-6 && det < 1e-6){
            inv.idt();
            return false;
        }

        double invdet = 1.0 / det;
        inv.values[0] = (float)(values[3] * invdet);
        inv.values[2] = (float)(-values[2] * invdet);
        inv.values[4] = (float)(((double)values[2] * values[5] - (double)values[3] * values[4]) * invdet);
        inv.values[1] = (float)(-values[1] * invdet);
        inv.values[3] = (float)(values[0] * invdet);
        inv.values[5] = (float)(((double)values[1] * values[4] - (double)values[0] * values[5]) * invdet);

        return true;
    }

    public boolean inv(){
        double det = (double)values[0] * values[3] - (double)values[2] * values[1];

        if(det > -1e-6 && det < 1e-6){
            idt();
            return false;
        }

        double invdet = 1.0 / det;
        float inv0 = (float)(values[3] * invdet);
        float inv2 = (float)(-values[2] * invdet);
        float inv4 = (float)(((double)values[2] * values[5] - (double)values[3] * values[4]) * invdet);
        float inv1 = (float)(-values[1] * invdet);
        float inv3 = (float)(values[0] * invdet);
        float inv5 = (float)(((double)values[1] * values[4] - (double)values[0] * values[5]) * invdet);

        values[0] = inv0;
        values[2] = inv2;
        values[4] = inv4;
        values[1] = inv1;
        values[3] = inv3;
        values[5] = inv5;

        return true;
    }

    public Vec2 transform(Vec2 point){
        float sx = point.x;
        float sy = point.y;
        point.x = sx * values[0] + sy * values[2] + values[4];
        point.y = sx * values[1] + sy * values[3] + values[5];
        return point;
    }

    public Vertex transform(Vertex vertex){
        float sx = vertex.x;
        float sy = vertex.y;
        vertex.x = sx * values[0] + sy * values[2] + values[4];
        vertex.y = sx * values[1] + sy * values[3] + values[5];
        return vertex;
    }

    public AffineTransform trn(float tx, float ty){
        values[4] += tx;
        values[5] += ty;
        return this;
    }

    public AffineTransform translate(float tx, float ty){
        tmp[0] = 1.0f;
        tmp[1] = 0.0f;
        tmp[2] = 0.0f;
        tmp[3] = 1.0f;
        tmp[4] = tx;
        tmp[5] = ty;
        mul(values, tmp);
        return this;
    }

    public AffineTransform preTranslate(float tx, float ty){
        tmp[0] = 1.0f;
        tmp[1] = 0.0f;
        tmp[2] = 0.0f;
        tmp[3] = 1.0f;
        tmp[4] = tx;
        tmp[5] = ty;
        mulLeft(values, tmp);
        return this;
    }

    public AffineTransform rotate(float degrees){
        return rotateRad(Mathf.degreesToRadians * degrees);
    }

    public AffineTransform rotateRad(float radians){
        if(radians == 0){
            return this;
        }

        float cs = Mathf.cos(radians);
        float sn = Mathf.sin(radians);

        tmp[0] = cs;
        tmp[1] = sn;
        tmp[2] = -sn;
        tmp[3] = cs;
        tmp[4] = 0.0f;
        tmp[5] = 0.0f;
        mul(values, tmp);
        return this;
    }

    public AffineTransform preRotate(float degrees){
        return preRotateRad(Mathf.degreesToRadians * degrees);
    }

    public AffineTransform preRotateRad(float radians){
        if(radians == 0)
            return this;
        float cs = Mathf.cos(radians), sn = Mathf.sin(radians);

        tmp[0] = cs;
        tmp[1] = sn;
        tmp[2] = -sn;
        tmp[3] = cs;
        tmp[4] = 0.0f;
        tmp[5] = 0.0f;
        mulLeft(values, tmp);
        return this;
    }

    public AffineTransform scale(float scaleX, float scaleY){
        tmp[0] = scaleX;
        tmp[1] = 0.0f;
        tmp[2] = 0.0f;
        tmp[3] = scaleY;
        tmp[4] = 0.0f;
        tmp[5] = 0.0f;
        mul(values, tmp);
        return this;
    }

    public AffineTransform preScale(float scaleX, float scaleY){
        tmp[0] = scaleX;
        tmp[1] = 0.0f;
        tmp[2] = 0.0f;
        tmp[3] = scaleY;
        tmp[4] = 0.0f;
        tmp[5] = 0.0f;
        mulLeft(values, tmp);
        return this;
    }

    public AffineTransform skewX(float degrees){
        return skewXRad(Mathf.degreesToRadians * degrees);
    }

    public AffineTransform skewXRad(float radians){
        if(radians == 0){
            return this;
        }
        tmp[0] = 1.0f;
        tmp[1] = 0.0f;
        tmp[2] = CanvasUtils.tanf(radians);
        tmp[3] = 1.0f;
        tmp[4] = 0.0f;
        tmp[5] = 0.0f;
        mul(values, tmp);
        return this;
    }

    public AffineTransform preSkewX(float degrees){
        return preSkewXRad(Mathf.degreesToRadians * degrees);
    }

    public AffineTransform preSkewXRad(float radians){
        if(radians == 0){
            return this;
        }
        tmp[0] = 1.0f;
        tmp[1] = 0.0f;
        tmp[2] = CanvasUtils.tanf(radians);
        tmp[3] = 1.0f;
        tmp[4] = 0.0f;
        tmp[5] = 0.0f;
        mulLeft(values, tmp);
        return this;
    }

    public AffineTransform skewY(float degrees){
        return skewYRad(Mathf.degreesToRadians * degrees);
    }

    public AffineTransform skewYRad(float radians){
        if(radians == 0){
            return this;
        }
        tmp[0] = 1.0f;
        tmp[1] = CanvasUtils.tanf(radians);
        tmp[2] = 0.0f;
        tmp[3] = 1.0f;
        tmp[4] = 0.0f;
        tmp[5] = 0.0f;
        mul(values, tmp);
        return this;
    }

    public AffineTransform preSkewY(float degrees){
        return preSkewYRad(Mathf.degreesToRadians * degrees);
    }

    public AffineTransform preSkewYRad(float radians){
        if(radians == 0){
            return this;
        }
        tmp[0] = 1.0f;
        tmp[1] = CanvasUtils.tanf(radians);
        tmp[2] = 0.0f;
        tmp[3] = 1.0f;
        tmp[4] = 0.0f;
        tmp[5] = 0.0f;
        mulLeft(values, tmp);
        return this;
    }

    public AffineTransform skew(float degreesX, float degreesY){
        return skewRad(Mathf.degreesToRadians * degreesX, Mathf.degreesToRadians * degreesY);
    }

    public AffineTransform skewRad(float radiansX, float radiansY){
        if(radiansX == 0 && radiansY == 0){
            return this;
        }
        tmp[0] = 1.0f;
        tmp[1] = CanvasUtils.tanf(radiansY);
        tmp[2] = CanvasUtils.tanf(radiansX);
        tmp[3] = 1.0f;
        tmp[4] = 0.0f;
        tmp[5] = 0.0f;
        mul(values, tmp);
        return this;
    }

    public AffineTransform preSkew(float degreesX, float degreesY){
        return preSkewRad(Mathf.degreesToRadians * degreesX, Mathf.degreesToRadians * degreesY);
    }

    public AffineTransform preSkewRad(float radiansX, float radiansY){
        if(radiansX == 0 && radiansY == 0){
            return this;
        }
        tmp[0] = 1.0f;
        tmp[1] = CanvasUtils.tanf(radiansY);
        tmp[2] = CanvasUtils.tanf(radiansX);
        tmp[3] = 1.0f;
        tmp[4] = 0.0f;
        tmp[5] = 0.0f;
        mulLeft(values, tmp);
        return this;
    }

    public Vec2 getTranslation(Vec2 position){
        position.x = values[4];
        position.y = values[5];
        return position;
    }

    public float getAverageScale(){
        float sx = (float)Math.sqrt(values[0] * values[0] + values[2] * values[2]);
        float sy = (float)Math.sqrt(values[1] * values[1] + values[3] * values[3]);
        return (sx + sy) * 0.5f;
    }

    public float getScaleX(){
        return (float)Math.sqrt(values[0] * values[0] + values[2] * values[2]);
    }

    public float getScaleY(){
        return (float)Math.sqrt(values[1] * values[1] + values[3] * values[3]);
    }

    public Vec2 getScale(Vec2 scale){
        scale.x = (float)Math.sqrt(values[0] * values[0] + values[2] * values[2]);
        scale.y = (float)Math.sqrt(values[1] * values[1] + values[3] * values[3]);
        return scale;
    }

    public float getRotation(){
        return Mathf.radiansToDegrees * getRotationRad();
    }

    public float getRotationRad(){
        return (float)Math.atan2(values[1], values[0]);
    }

    public AffineTransform scl(float scale){
        values[0] *= scale;
        values[3] *= scale;
        return this;
    }

    @Override
    public void reset(){
        idt();
    }

    public void free(){
        Pools.free(this);
    }

    @Override
    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(obj.getClass() != getClass()){
            return false;
        }
        AffineTransform other = (AffineTransform)obj;
        return Arrays.equals(values, other.values);
    }

    public boolean equals(AffineTransform other){
        if(other == this){
            return true;
        }
        if(other == null){
            return false;
        }
        return Arrays.equals(values, other.values);
    }

    @Override
    public int hashCode(){
        return Arrays.hashCode(values);
    }

    @Override
    public String toString(){
        return getClass().getName() + "\n\t[" + values[0] + "|" + values[2] + "|" + values[4] + "]\n\t[" + values[1]
        + "|" + values[3] + "|" + values[5] + "]";
    }
}
