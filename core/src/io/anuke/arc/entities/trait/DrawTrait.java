package io.anuke.arc.entities.trait;

public interface DrawTrait extends Entity{

    default float drawSize(){
        return 20f;
    }

    void draw();
}
