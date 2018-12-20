package io.anuke.arc.entities.trait;

import io.anuke.arc.math.geom.Vector2;
import io.anuke.arc.utils.Time;

public interface VelocityTrait extends MoveTrait{

    Vector2 getVelocity();

    default void applyImpulse(float x, float y){
        getVelocity().x += x / getMass();
        getVelocity().y += y / getMass();
    }

    default float getMaxVelocity(){
        return Float.MAX_VALUE;
    }

    default float getMass(){
        return 1f;
    }

    default float getDrag(){
        return 0f;
    }

    default void updateVelocity(){
        getVelocity().scl(1f - getDrag() * Time.delta());

        if(this instanceof SolidTrait){
            ((SolidTrait) this).move(getVelocity().x * Time.delta(), getVelocity().y * Time.delta());
        }else{
            moveBy(getVelocity().x * Time.delta(), getVelocity().y * Time.delta());
        }
    }
}
