package io.anuke.arc.graphics.g2d;

import io.anuke.arc.Core;

public class Draw{
    private static TextureRegion blankRegion;
    private static float scl = 1f;

    public static void scale(float scaling){
        Draw.scl = scaling;
    }

    public static float scale(){
        return scl;
    }

    public static TextureRegion getBlankRegion(){
        if(blankRegion == null){
            if(Core.atlas != null && Core.atlas.has("blank")){
                blankRegion = Core.atlas.find("blank");
                return blankRegion;
            }else{
                throw new IllegalArgumentException("No atlas defined.");
            }
        }
        return blankRegion;
    }

    public static boolean hasRegion(String name){
        return Core.atlas.has(name);
    }

}
