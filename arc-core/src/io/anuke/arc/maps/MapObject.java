package io.anuke.arc.maps;

import io.anuke.arc.graphics.Color;

/** Generic Map entity with basic attributes like name, opacity, color */
public class MapObject{
    public String name = "";
    public float opacity = 1.0f;
    public boolean visible = true;
    public MapProperties properties = new MapProperties();
    public Color color = Color.white.cpy();
}
