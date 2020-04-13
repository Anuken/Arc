package arc.graphics.vector;

import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.pooling.Pool.*;
import arc.util.pooling.*;

import java.util.*;

public class Paint implements Poolable{
    final float[] imageExtent = new float[2];
    final AffineTransform imageTransform = AffineTransform.obtain();
    final Color solidColor = new Color();
    final Vec2 gradientPoint1 = new Vec2();
    final Vec2 gradientPoint2 = new Vec2();
    final AffineTransform gradientTransform = AffineTransform.obtain();
    Image image;
    GradientType gradientType = GradientType.none;
    Gradient gradient;
    GradientSpread gradientSpread = GradientSpread.pad;
    float gradientRadius;

    public static Paint obtain(){
        return Pools.obtain(Paint.class, Paint::new);
    }

    public Paint set(Paint other){
        image = other.image;

        System.arraycopy(imageExtent, 0, other.imageExtent, 0, 2);
        imageTransform.set(other.imageTransform);

        solidColor.set(other.solidColor);

        gradient = other.gradient;
        gradientType = other.gradientType;
        gradientSpread = other.gradientSpread;
        gradientRadius = other.gradientRadius;
        gradientPoint1.set(other.gradientPoint1);
        gradientPoint2.set(other.gradientPoint2);
        gradientTransform.set(other.gradientTransform);
        return this;
    }

    public AffineTransform getImageTransform(AffineTransform out){
        return out.set(imageTransform);
    }

    public AffineTransform getGradientTransform(AffineTransform out){
        return out.set(gradientTransform);
    }

    // TODO getters

    public Paint setToColor(int r, int g, int b, int a){
        return reset(r / 255f, g / 255f, b / 255f, a / 255f);
    }

    public Paint setColor(int r, int g, int b, int a){
        solidColor.set(r / 255f, g / 255f, b / 255f, a / 255f);
        return this;
    }

    public Paint setToColor(float r, float g, float b, float a){
        return reset(r, g, b, a);
    }

    public Paint setColor(float r, float g, float b, float a){
        solidColor.set(r, g, b, a);
        return this;
    }

    public Paint setToColor(int rgba){
        reset();
        return setColor(rgba);
    }

    public Paint setColor(int rgba){
        solidColor.set(rgba);
        return this;
    }

    public Paint setToColor(Color color){
        reset();
        return setColor(color);
    }

    public Paint setColor(Color color){
        solidColor.set(color);
        return this;
    }

    public Paint mulAlpha(float a){
        solidColor.a *= a;
        return this;
    }

    public Paint setToImage(float left, float top, float width, float height, float angleDegrees, float alpha, Image image){
        return setToImageRad(left, top, width, height, Mathf.degreesToRadians * angleDegrees, alpha, image);
    }

    public Paint setToImageRad(float left, float top, float width, float height, float angleRadians, float alpha, Image image){
        reset(1f, 1f, 1f, 1f);
        return setImageRad(left, top, width, height, angleRadians, alpha, image);
    }

    //TODO add to canvas
    public Paint setToImage(float width, float height, AffineTransform xform, float alpha, Image image){
        reset(1f, 1f, 1f, 1f);
        return setImage(width, height, xform, alpha, image);
    }

    public Paint setImage(float left, float top, float width, float height, float angleDegrees, float alpha, Image image){
        return setImageRad(left, top, width, height, Mathf.degreesToRadians * angleDegrees, alpha, image);
    }

    public Paint setImageRad(float left, float top, float width, float height, float angleRadians, float alpha, Image image){
        this.image = image;
        imageTransform.setToRotationRad(angleRadians);
        imageTransform.values[4] = left;
        imageTransform.values[5] = top;
        imageExtent[0] = width;
        imageExtent[1] = height;
        solidColor.a *= alpha;
        return this;
    }

    //TODO add to canvas
    public Paint setImage(float width, float height, AffineTransform xform, float alpha, Image image){
        this.image = image;
        imageTransform.set(xform);
        imageExtent[0] = width;
        imageExtent[1] = height;
        solidColor.a *= alpha;
        return this;
    }

    public Paint setToLinearGradient(float startX, float startY, float endX, float endY, GradientSpread spread, Gradient gradient){
        reset(1f, 1f, 1f, 1f);
        return setLinearGradient(startX, startY, endX, endY, spread, gradient);
    }

    public Paint setLinearGradient(float startX, float startY, float endX, float endY, GradientSpread spread, Gradient gradient){
        gradientType = GradientType.linear;
        gradientRadius = 0;
        gradientPoint1.set(startX, startY);
        gradientPoint2.set(endX, endY);
        gradientSpread = spread == null ? GradientSpread.pad : spread;
        this.gradient = gradient;
        return this;
    }

    public Paint setToLinearGradient(float startX, float startY, float endX, float endY, GradientSpread spread, AffineTransform xform, Gradient gradient){
        reset(1f, 1f, 1f, 1f);
        return setLinearGradient(startX, startY, endX, endY, spread, xform, gradient);
    }

    public Paint setLinearGradient(float startX, float startY, float endX, float endY, GradientSpread spread, AffineTransform xform, Gradient gradient){
        gradientType = GradientType.linear;
        gradientRadius = 0;
        gradientPoint1.set(startX, startY);
        gradientPoint2.set(endX, endY);
        gradientSpread = spread == null ? GradientSpread.pad : spread;
        gradientTransform.set(xform);
        this.gradient = gradient;
        return this;
    }

    public Paint setToRadialGradient(float centerX, float centerY, float focusX, float focusY, float gradientRadius, GradientSpread spread, Gradient gradient){
        reset(1f, 1f, 1f, 1f);
        return setRadialGradient(centerX, centerY, focusX, focusY, gradientRadius, spread, gradient);
    }

    public Paint setRadialGradient(float centerX, float centerY, float focusX, float focusY, float gradientRadius, GradientSpread spread, Gradient gradient){
        this.gradientType = GradientType.radial;
        this.gradientRadius = gradientRadius;
        this.gradientPoint1.set(centerX, centerY);
        this.gradientPoint2.set(focusX, focusY);
        this.gradientSpread = spread == null ? GradientSpread.pad : spread;
        this.gradient = gradient;
        return this;
    }

    public Paint setToRadialGradient(float centerX, float centerY, float focusX, float focusY, float gradientRadius, GradientSpread spread, AffineTransform xform, Gradient gradient){
        reset(1f, 1f, 1f, 1f);
        return setRadialGradient(centerX, centerY, focusX, focusY, gradientRadius, spread, xform, gradient);
    }

    public Paint setRadialGradient(float centerX, float centerY, float focusX, float focusY, float gradientRadius, GradientSpread spread, AffineTransform xform, Gradient gradient){
        this.gradientType = GradientType.radial;
        this.gradientRadius = gradientRadius;
        this.gradientPoint1.set(centerX, centerY);
        this.gradientPoint2.set(focusX, focusY);
        this.gradientSpread = spread == null ? GradientSpread.pad : spread;
        this.gradientTransform.set(xform);
        this.gradient = gradient;
        return this;
    }

    public Paint setToBoxGradient(float x, float y, float width, float heigth, float gradientRadius, GradientSpread spread, Gradient gradient){
        reset(1f, 1f, 1f, 1f);
        return setBoxGradient(x, y, width, heigth, gradientRadius, spread, gradient);
    }

    public Paint setBoxGradient(float x, float y, float width, float heigth, float gradientRadius, GradientSpread spread, Gradient gradient){
        this.gradientType = GradientType.box;
        this.gradientRadius = gradientRadius;
        this.gradientPoint1.set(x, y);
        this.gradientPoint2.set(width, heigth);
        this.gradientSpread = spread == null ? GradientSpread.pad : spread;
        this.gradient = gradient;
        return this;
    }

    public Paint setToBoxGradient(float x, float y, float width, float heigth, float gradientRadius, GradientSpread spread, AffineTransform xform, Gradient gradient){
        reset(1f, 1f, 1f, 1f);
        return setBoxGradient(x, y, width, heigth, gradientRadius, spread, xform, gradient);
    }

    public Paint setBoxGradient(float x, float y, float width, float heigth, float gradientRadius, GradientSpread spread, AffineTransform xform, Gradient gradient){
        this.gradientType = GradientType.box;
        this.gradientRadius = gradientRadius;
        this.gradientPoint1.set(x, y);
        this.gradientPoint2.set(width, heigth);
        this.gradientSpread = spread == null ? GradientSpread.pad : spread;
        this.gradientTransform.set(xform);
        this.gradient = gradient;
        return this;
    }

    public Paint setToConicalGradient(float centerX, float centerY, Gradient gradient){
        reset(1f, 1f, 1f, 1f);
        return setConicalGradient(centerX, centerY, gradient);
    }

    public Paint setConicalGradient(float centerX, float centerY, Gradient gradient){
        this.gradientType = GradientType.conical;
        this.gradientPoint1.set(centerX, centerY);
        this.gradient = gradient;
        return this;
    }

    public Paint setToConicalGradient(float centerX, float centerY, AffineTransform xform, Gradient gradient){
        reset(1f, 1f, 1f, 1f);
        return setConicalGradient(centerX, centerY, xform, gradient);
    }

    public Paint setConicalGradient(float centerX, float centerY, AffineTransform xform, Gradient gradient){
        this.gradientType = GradientType.conical;
        this.gradientPoint1.set(centerX, centerY);
        this.gradientTransform.set(xform);
        this.gradient = gradient;
        return this;
    }

    @Override
    public void reset(){
        reset(0, 0, 0, 0);
    }

    private Paint reset(float r, float g, float b, float a){
        image = null;
        imageTransform.idt();
        Arrays.fill(imageExtent, 0);

        solidColor.set(r, g, b, a);

        gradient = null;
        gradientType = GradientType.none;
        gradientSpread = GradientSpread.pad;
        gradientRadius = 0;
        gradientPoint1.setZero();
        gradientPoint2.setZero();
        gradientTransform.idt();
        return this;
    }

    public void free(){
        Pools.free(this);
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        Paint other = (Paint)obj;
        if(gradient == null){
            if(other.gradient != null){
                return false;
            }
        }else if(!gradient.equals(other.gradient)){
            return false;
        }
        if(!gradientPoint1.equals(other.gradientPoint1)){
            return false;
        }
        if(!gradientPoint2.equals(other.gradientPoint2)){
            return false;
        }
        if(gradientRadius != other.gradientRadius){
            return false;
        }
        if(gradientSpread != other.gradientSpread){
            return false;
        }
        if(!gradientTransform.equals(other.gradientTransform)){
            return false;
        }
        if(gradientType != other.gradientType){
            return false;
        }
        if(image == null){
            if(other.image != null){
                return false;
            }
        }else if(!image.equals(other.image)){
            return false;
        }
        if(!Arrays.equals(imageExtent, other.imageExtent)){
            return false;
        }
        if(!imageTransform.equals(other.imageTransform)){
            return false;
        }
        return solidColor.equals(other.solidColor);
    }

    @Override
    public int hashCode(){
        final int prime = 31;
        int result = 1;
        result = prime * result + ((gradient == null) ? 0 : gradient.hashCode());
        result = prime * result + gradientPoint1.hashCode();
        result = prime * result + gradientPoint2.hashCode();
        result = prime * result + Float.floatToIntBits(gradientRadius);
        result = prime * result + gradientSpread.hashCode();
        result = prime * result + gradientTransform.hashCode();
        result = prime * result + gradientType.hashCode();
        result = prime * result + ((image == null) ? 0 : image.hashCode());
        result = prime * result + Arrays.hashCode(imageExtent);
        result = prime * result + imageTransform.hashCode();
        result = prime * result + solidColor.hashCode();
        return result;
    }

    @Override
    public String toString(){
        return "Paint [image=" + image + ", imageExtent=" + Arrays.toString(imageExtent) + ", imageTransform=" + imageTransform + ", solidColor="
        + solidColor + ", gradientType=" + gradientType + ", gradient=" + gradient + ", gradientSpread=" + gradientSpread
        + ", gradientRadius=" + gradientRadius + ", gradientPoint1=" + gradientPoint1 + ", gradientPoint2=" + gradientPoint2
        + ", gradientTransform=" + gradientTransform + "]";
    }
}
