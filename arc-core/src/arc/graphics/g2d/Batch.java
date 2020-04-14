package arc.graphics.g2d;

import arc.graphics.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.util.*;

/**
 * Draws batched quads using indices.
 * @author mzechner
 * @author Nathan Sweet
 */
public abstract class Batch implements Disposable{
    protected Mesh mesh;

    protected float z;
    protected int idx = 0;
    protected Texture lastTexture = null;
    protected float invTexWidth = 0, invTexHeight = 0;

    protected boolean apply;

    protected final Mat transformMatrix = new Mat();
    protected final Mat projectionMatrix = new Mat();
    protected final Mat combinedMatrix = new Mat();

    protected Blending blending = Blending.normal;

    protected Shader shader, customShader = null;
    protected boolean ownsShader;

    protected final Color color = new Color(1, 1, 1, 1);
    protected float colorPacked = Color.whiteFloatBits;

    protected final Color mixColor = Color.clear;
    protected float mixColorPacked = Color.clearFloatBits;

    protected void z(float z){
        this.z = z;
    }

    protected void setColor(Color tint){
        color.set(tint);
        colorPacked = tint.toFloatBits();
    }

    protected void setColor(float r, float g, float b, float a){
        color.set(r, g, b, a);
        colorPacked = color.toFloatBits();
    }

    protected Color getColor(){
        return color;
    }

    protected void setPackedColor(float packedColor){
        this.color.abgr8888(packedColor);
        this.colorPacked = packedColor;
    }

    protected float getPackedColor(){
        return colorPacked;
    }

    protected void setMixColor(Color tint){
        mixColor.set(tint);
        mixColorPacked = tint.toFloatBits();
    }

    protected void setMixColor(float r, float g, float b, float a){
        mixColor.set(r, g, b, a);
        mixColorPacked = mixColor.toFloatBits();
    }

    protected Color getMixColor(){
        return mixColor;
    }

    protected void setPackedMixColor(float packedColor){
        this.mixColor.abgr8888(packedColor);
        this.mixColorPacked = packedColor;
    }

    protected float getPackedMixColor(){
        return mixColorPacked;
    }

    protected abstract void draw(Texture texture, float[] spriteVertices, int offset, int count);

    protected void draw(TextureRegion region, float x, float y){
        draw(region, x, y, region.getWidth(), region.getHeight());
    }

    protected void draw(TextureRegion region, float x, float y, float width, float height){
        draw(region, x, y, 0, 0, width, height, 0);
    }

    protected abstract void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float rotation);

    protected abstract void flush();

    protected void setBlending(Blending blending){
        flush();
        this.blending = blending;
    }

    @Override
    public void dispose(){
        if(mesh != null){
            mesh.dispose();
        }
        if(ownsShader && shader != null) shader.dispose();
    }

    protected Mat getProjection(){
        return projectionMatrix;
    }

    protected Mat getTransform(){
        return transformMatrix;
    }

    protected void setProjection(Mat projection){
        flush();
        projectionMatrix.set(projection);
    }

    protected void setTransform(Mat transform){
        flush();
        transformMatrix.set(transform);
    }

    protected void setupMatrices(){
        combinedMatrix.set(projectionMatrix).mul(transformMatrix);
        getShader().setUniformMatrix4("u_projTrans", BatchShader.copyTransform(combinedMatrix));
        getShader().setUniformi("u_texture", 0);
    }

    protected void switchTexture(Texture texture){
        flush();
        lastTexture = texture;
        invTexWidth = 1.0f / texture.getWidth();
        invTexHeight = 1.0f / texture.getHeight();
    }

    protected void setShader(Shader shader){
        setShader(shader, true);
    }

    protected void setShader(Shader shader, boolean apply){
        flush();
        customShader = shader;
        this.apply = apply;
    }

    protected Shader getShader(){
        return customShader == null ? shader : customShader;
    }
}