package io.anuke.arc.graphics.g2d;

import io.anuke.arc.Core;
import io.anuke.arc.graphics.Color;
import io.anuke.arc.graphics.g2d.SpriteBatch.BatchRect;
import io.anuke.arc.util.Tmp;

public class Draw{
    public static float scl = 1f;

    public static void tint(Color a, Color b, float s){
        Tmp.c1.set(a).lerp(b, s);
        Core.graphics.batch().getColor().set(Tmp.c1.r, Tmp.c1.g, Tmp.c1.b);
    }

    public static void tint(Color color){
        Core.graphics.batch().getColor().set(color.r, color.g, color.b);
    }

    public static void color(Color color){
        Core.graphics.batch().setColor(color);
    }

    /** Automatically mixes colors. */
    public static void color(Color a, Color b, float s){
        Core.graphics.batch().setColor(Tmp.c1.set(a).lerp(b, s));
    }

    public static void color(){
        Core.graphics.batch().setColor(Color.WHITE);
    }

    public static void color(float r, float g, float b){
        Core.graphics.batch().setColor(r, g, b, 1f);
    }

    public static void color(float r, float g, float b, float a){
        Core.graphics.batch().setColor(r, g, b, a);
    }

    /** Lightness color. */
    public static void colorl(float l){
        color(l, l, l);
    }

    /** Lightness color, alpha. */
    public static void colorl(float l, float a){
        color(l, l, l, a);
    }

    //TODO replace/remove?
    public static void reset(){
        color();
    }

    public static void alpha(float alpha){
        Core.graphics.batch().getColor().a = alpha;
    }

    public static BatchRect rect(String region, float x, float y, float w, float h){
        return rect(region, x, y, w, h);
    }

    public static BatchRect rect(TextureRegion region, float x, float y, float w, float h){
        return Core.graphics.batch().draw().tex(region)
        .set(x - w /2f * scl, y - h /2f * scl, w, h);
    }

    public static BatchRect rect(TextureRegion region, float x, float y){
        return Core.graphics.batch().draw().tex(region)
            .set(x - region.width /2f * scl, y - region.height /2f * scl, region.width * scl, region.height * scl);
    }

    public static BatchRect rect(String region, float x, float y){
        return rect(Core.atlas.find(region), x, y);
    }

    public static void flush(){
        Core.graphics.batch().flush();
    }
}
