package io.anuke.arc.entities.impl;

import io.anuke.arc.entities.trait.SolidTrait;
import io.anuke.arc.math.geom.Vector2;

public abstract class SolidEntity extends BaseEntity implements SolidTrait{
    protected transient Vector2 velocity = new Vector2(0f, 0.0001f);
    private transient Vector2 lastPosition = new Vector2();

    @Override
    public Vector2 lastPosition(){
        return lastPosition;
    }

    @Override
    public Vector2 velocity(){
        return velocity;
    }
}
