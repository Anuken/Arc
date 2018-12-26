package io.anuke.arc.entities.trait;

import io.anuke.arc.entities.Entities;
import io.anuke.arc.entities.EntityGroup;
import io.anuke.arc.math.geom.Position;

public interface Entity extends MoveTrait, Position{

    int getID();

    void resetID(int id);

    default void update(){
    }

    default void removed(){
    }

    default void added(){
    }

    default EntityGroup targetGroup(){
        return Entities.defaultGroup();
    }

    default void add(){
        targetGroup().add(this);
    }

    default void remove(){
        if(getGroup() != null){
            getGroup().remove(this);
        }

        setGroup(null);
    }

    EntityGroup getGroup();

    void setGroup(EntityGroup group);

    default boolean isAdded(){
        return getGroup() != null;
    }
}
