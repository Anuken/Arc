package io.anuke.arc.scene.ui.layout;

import io.anuke.arc.Application.ApplicationType;
import io.anuke.arc.Core;

public enum UnitScl{
    px{
        @Override
        public float scl(float amount){
            return amount;
        }
    },
    dp{
        float scl = -1;

        @Override
        public float scl(float amount){
            if(scl < 0f){
                //calculate scaling value if it hasn't been set yet
                if(Core.app.getType() == ApplicationType.Desktop){
                    scl = 1f * product;
                }else if(Core.app.getType() == ApplicationType.WebGL){
                    scl = 1f * product;
                }else{
                    //mobile scaling
                    scl = Math.max(Math.round((Core.graphics.getDensity() / 1.5f + addition) / 0.5) * 0.5f, 1f) * product;
                }
            }
            return amount * scl;
        }

        @Override
        public void setProduct(float product){
            this.product = product;
            scl = -1;
        }
    };
    public float addition = 0f;
    protected float product = 1f;

    public abstract float scl(float amount);

    public void setProduct(float product){
        this.product = product;
    }
}
