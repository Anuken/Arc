package arc.graphics.vector;

import arc.graphics.*;
import arc.graphics.Pixmap.*;
import arc.math.geom.*;
import arc.util.pooling.Pool.*;

import java.nio.*;
import java.util.*;

class GlUniforms implements Poolable{
    public static final int UNIFORMARRAY_SIZE = 15;
    public static final GlUniforms stencilInstance;

    static{
        stencilInstance = new GlUniforms();
        stencilInstance.strokeThr = -1.0f;
        stencilInstance.uniformType = UniformType.stencil;
    }

    final float[] scissorExtent = new float[2];
    final float[] scissorScale = new float[2];
    final float[] scissorMatrix = new float[12];
    final float[] imageExtent = new float[2];
    final float[] imageMatrix = new float[12];
    final Vec2 gradientPoint1 = new Vec2();
    final Vec2 gradientPoint2 = new Vec2();
    final float[] gradientMatrix = new float[12];
    final Color solidColor = new Color();
    int imageHandle;
    ImageType imageType = ImageType.none;
    boolean texturedVertices = false;
    Gradient gradient;
    float gradientRadius;
    GradientType gradientType = GradientType.none;
    GradientSpread gradientSpread = GradientSpread.pad;
    float strokeMult;
    float strokeThr;
    UniformType uniformType = UniformType.fill;

    private AffineTransform tempXform = new AffineTransform();

    private static void xformToMat3x4(float[] matrix3, AffineTransform transform){
        matrix3[0] = transform.values[0];
        matrix3[1] = transform.values[1];
        matrix3[2] = 0.0f;
        matrix3[3] = 0.0f;
        matrix3[4] = transform.values[2];
        matrix3[5] = transform.values[3];
        matrix3[6] = 0.0f;
        matrix3[7] = 0.0f;
        matrix3[8] = transform.values[4];
        matrix3[9] = transform.values[5];
        matrix3[10] = 1.0f;
        matrix3[11] = 0.0f;
    }

    void setScissorMatrix(AffineTransform transform){
        xformToMat3x4(scissorMatrix, transform);
    }

    void setImageMatrix(AffineTransform transform){
        xformToMat3x4(imageMatrix, transform);
    }

    void setGradientMatrix(AffineTransform transform){
        xformToMat3x4(gradientMatrix, transform);
    }

    GlUniforms init(AffineTransform globalXform, float globalAlpha, Paint paint, Scissor scissor, float width,
                    float fringe, float strokeThr, boolean texturedVertices){
        uniformType = UniformType.fill;
        strokeMult = (width * 0.5f + fringe * 0.5f) / fringe;
        this.strokeThr = strokeThr;

        initScissor(scissor, fringe);
        initSolidColor(globalAlpha, paint);
        initImage(globalXform, paint, texturedVertices);
        initGradient(globalXform, paint);

        return this;
    }

    private void initScissor(Scissor scissor, float fringe){
        if(scissor.extent[0] < -0.5f || scissor.extent[1] < -0.5f){
            Arrays.fill(scissorMatrix, 0);
            scissorExtent[0] = 1.0f;
            scissorExtent[1] = 1.0f;
            scissorScale[0] = 1.0f;
            scissorScale[1] = 1.0f;
        }else{
            scissor.xform.inv(tempXform);
            setScissorMatrix(tempXform);
            scissorExtent[0] = scissor.extent[0];
            scissorExtent[1] = scissor.extent[1];
            float[] xform = scissor.xform.values;
            scissorScale[0] = (float)(Math.sqrt(xform[0] * xform[0] + xform[2] * xform[2]) / fringe);
            scissorScale[1] = (float)(Math.sqrt(xform[1] * xform[1] + xform[3] * xform[3]) / fringe);
        }
    }

    private void initSolidColor(float globalAlpha, Paint paint){
        solidColor.set(paint.solidColor);
        solidColor.a *= globalAlpha;
        solidColor.premultiplyAlpha();
    }

    private void initImage(AffineTransform globalXform, Paint paint, boolean texturedVertices){
        if(paint.image == null){
            imageType = ImageType.none;
        }else{
            Image image = paint.image;
            imageHandle = image.texture.getTextureObjectHandle();
            imageExtent[0] = paint.imageExtent[0];
            imageExtent[1] = paint.imageExtent[1];

            if(image.flipX || image.flipY){
                float xScale = image.flipX ? -1 : 1;
                float yScale = image.flipY ? -1 : 1;
                tempXform.setToScaling(xScale, yScale);
                tempXform.mul(paint.imageTransform);
                tempXform.mul(globalXform);
                tempXform.inv();
            }else{
                tempXform.set(paint.imageTransform);
                tempXform.mul(globalXform);
                tempXform.inv();
            }

            Format format = image.texture.getTextureData().getFormat();
            if(format == Format.alpha || format == Format.intensity || format == Format.luminanceAlpha){
                imageType = ImageType.alpha;
            }else{
                imageType = image.premultiplied ? ImageType.rgbaPremultiplied : ImageType.rgba;
            }

            setImageMatrix(tempXform);
            this.texturedVertices = texturedVertices;
        }
    }

    private void initGradient(AffineTransform globalXform, Paint paint){
        if(paint.gradientType != GradientType.none){
            gradient = paint.gradient;
            gradientType = paint.gradientType;
            gradientSpread = paint.gradientSpread;
            gradientRadius = paint.gradientRadius;
            gradientPoint1.set(paint.gradientPoint1);
            gradientPoint2.set(paint.gradientPoint2);
            tempXform.set(paint.gradientTransform);
            tempXform.mul(globalXform);
            tempXform.inv();
            setGradientMatrix(tempXform);
        }
    }

    GlUniforms initForFrameBuffer(){
        uniformType = UniformType.fill;
        Arrays.fill(scissorMatrix, 0);
        scissorExtent[0] = 1.0f;
        scissorExtent[1] = 1.0f;
        scissorScale[0] = 1.0f;
        scissorScale[1] = 1.0f;
        solidColor.set(1, 1, 1, 1);
        imageHandle = 0;
        setImageMatrix(tempXform.idt());
        texturedVertices = true;
        imageType = ImageType.rgbaPremultiplied;
        return this;
    }

    public FloatBuffer getUniformArray(FloatBuffer uniformArray){
        uniformArray.clear();

        uniformArray.put(scissorMatrix);
        uniformArray.put(imageMatrix);
        uniformArray.put(gradientMatrix);
        uniformArray.put(solidColor.r);
        uniformArray.put(solidColor.g);
        uniformArray.put(solidColor.b);
        uniformArray.put(solidColor.a);
        uniformArray.put(scissorExtent);
        uniformArray.put(scissorScale);
        uniformArray.put(imageExtent);
        uniformArray.put(gradientPoint1.x);
        uniformArray.put(gradientPoint1.y);
        uniformArray.put(gradientPoint2.x);
        uniformArray.put(gradientPoint2.y);
        uniformArray.put(imageType.ordinal());
        uniformArray.put(texturedVertices ? 1 : 0);
        uniformArray.put(gradient == null ? 0 : gradient.getLength());
        uniformArray.put(gradientRadius);
        uniformArray.put(gradientType.ordinal());
        uniformArray.put(gradientSpread.ordinal());
        uniformArray.put(strokeMult);
        uniformArray.put(strokeThr);
        uniformArray.put(uniformType.ordinal());

        uniformArray.rewind();

        return uniformArray;
    }

    @Override
    public void reset(){
        Arrays.fill(scissorExtent, 0);
        Arrays.fill(scissorScale, 0);
        Arrays.fill(scissorMatrix, 0);

        imageHandle = 0;
        imageType = ImageType.none;
        texturedVertices = false;
        Arrays.fill(imageExtent, 0);
        Arrays.fill(imageMatrix, 0);

        gradient = null;
        gradientRadius = 0;
        gradientType = GradientType.none;
        gradientSpread = GradientSpread.pad;
        gradientPoint1.setZero();
        gradientPoint2.setZero();
        Arrays.fill(gradientMatrix, 0);

        strokeMult = 0;
        strokeThr = 0;
        solidColor.set(0, 0, 0, 0);

        uniformType = UniformType.fill;
    }

    public enum UniformType{
        stencil, fill
    }

    public enum ImageType{
        none, rgbaPremultiplied, rgba, alpha
    }
}
