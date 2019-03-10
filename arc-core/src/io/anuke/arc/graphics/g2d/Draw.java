package io.anuke.arc.graphics.g2d;

import io.anuke.arc.Core;
import io.anuke.arc.graphics.Blending;
import io.anuke.arc.graphics.Color;
import io.anuke.arc.graphics.Texture;
import io.anuke.arc.graphics.glutils.Shader;
import io.anuke.arc.math.Mathf;
import io.anuke.arc.math.Matrix3;
import io.anuke.arc.util.Tmp;

public class Draw{
    private static Color[] carr = new Color[3];
    public static float scl = 1f;

    public static Shader getShader(){
        return Core.batch.getShader();
    }

    public static void shader(Shader shader){
        shader(shader, true);
    }

    public static void shader(Shader shader, boolean apply){
        Core.batch.setShader(shader, apply);
    }

    public static void shader(){
        Core.batch.setShader(null);
    }

    public static Color getColor(){
        return Core.batch.getColor();
    }

    public static Color getMixColor(){
        return Core.batch.getMixColor();
    }

    public static void mixcol(Color color, float a){
        Core.batch.setMixColor(color.r, color.g, color.b, Mathf.clamp(a));
    }

    public static void mixcol(){
        Core.batch.setPackedMixColor(Color.CLEAR_FLOAT_BITS);
    }

    public static void tint(Color a, Color b, float s){
        Tmp.c1.set(a).lerp(b, s);
        Core.batch.setColor(Tmp.c1.r, Tmp.c1.g, Tmp.c1.b, Core.batch.getColor().a);
    }

    public static void tint(Color color){
        Core.batch.setColor(color.r, color.g, color.b, Core.batch.getColor().a);
    }

    public static void color(Color color){
        Core.batch.setColor(color);
    }

    public static void color(Color color, float alpha){
        Core.batch.setColor(color.r, color.g, color.b, alpha);
    }

    public static void color(float color){
        Core.batch.setPackedColor(color);
    }

    public static void color(Color a, Color b, Color c, float progress){
        carr[0] = a;
        carr[1] = b;
        carr[2] = c;
        color(Tmp.c1.lerp(carr, progress));
    }

    /** Automatically mixes colors. */
    public static void color(Color a, Color b, float s){
        Core.batch.setColor(Tmp.c1.set(a).lerp(b, s));
    }

    public static void color(){
        Core.batch.setColor(Color.WHITE);
    }

    public static void color(float r, float g, float b){
        Core.batch.setColor(r, g, b, 1f);
    }

    public static void color(float r, float g, float b, float a){
        Core.batch.setColor(r, g, b, a);
    }

    /** Lightness color. */
    public static void colorl(float l){
        color(l, l, l);
    }

    /** Lightness color, alpha. */
    public static void colorl(float l, float a){
        color(l, l, l, a);
    }

    public static void blend(Blending blending){
        Core.batch.setBlending(blending);
    }

    public static void blend(){
        blend(Blending.normal);
    }

    public static void reset(){
        color();
        mixcol();
        Lines.stroke(1f);
    }

    public static void alpha(float alpha){
        Core.batch.setColor(Core.batch.getColor().r, Core.batch.getColor().g, Core.batch.getColor().b, alpha);
    }

    public static void drawable(String name, float x, float y, float w, float h){
        Core.scene.skin.getDrawable(name).draw(x, y, w, h);
    }

    public static void rect(String region, float x, float y, float w, float h){
        rect(Core.atlas.find(region), x, y, w, h);
    }

    public static void rect(TextureRegion region, float x, float y, float w, float h){
        Core.batch.draw(region, x - w /2f, y - h /2f, w, h);
    }

    public static void rect(TextureRegion region, float x, float y){
        rect(region, x, y, region.getWidth() * scl, region.getHeight() * scl);
    }

    public static void rect(String region, float x, float y){
        rect(Core.atlas.find(region), x, y);
    }

    public static void rect(TextureRegion region, float x, float y, float w, float h, float originX, float originY, float rotation){
        Core.batch.draw(region, x - w /2f, y - h /2f, originX, originY, w, h, rotation);
    }

    public static void rect(TextureRegion region, float x, float y, float w, float h, float rotation){
        rect(region, x, y, w, h, w/2f, h/2f, rotation);
    }

    public static void rect(String region, float x, float y, float w, float h, float rotation){
        rect(Core.atlas.find(region), x, y, w, h, w/2f, h/2f, rotation);
    }

    public static void rect(TextureRegion region, float x, float y, float rotation){
        rect(region, x, y, region.getWidth() * scl, region.getHeight() * scl, rotation);
    }

    public static void rect(String region, float x, float y, float rotation){
        rect(Core.atlas.find(region), x, y, rotation);
    }

    public static void vert(Texture texture, float[] vertices, int offset, int length){
        Core.batch.draw(texture, vertices, offset, length);
    }

    public static void vert(float[] vertices){
        vert(Core.atlas.texture(), vertices, 0, vertices.length);
    }

    public static void flush(){
        Core.batch.flush();
    }

    public static void proj(Matrix3 proj){
        Core.batch.setProjection(proj);
    }

    public static Matrix3 proj(){
        return Core.batch.getProjection();
    }

    public static void trans(Matrix3 trans){
        Core.batch.setTransform(trans);
    }

    public static Matrix3 trans(){
        return Core.batch.getTransform();
    }

    public static TextureRegion wrap(Texture texture){
        Tmp.tr2.set(texture);
        return Tmp.tr2;
    }
}
