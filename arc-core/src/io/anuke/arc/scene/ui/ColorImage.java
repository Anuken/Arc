package io.anuke.arc.scene.ui;

import io.anuke.arc.graphics.Color;

public class ColorImage extends Image{
    private Color set;

    public ColorImage(Color set){
        this.set = set;
    }

    @Override
    public void draw(){
        setColor(set);
        super.draw();
    }
}
