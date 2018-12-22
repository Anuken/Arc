package io.anuke.arc;

import io.anuke.arc.assets.AssetManager;
import io.anuke.arc.graphics.Camera;
import io.anuke.arc.graphics.GL20;
import io.anuke.arc.graphics.GL30;
import io.anuke.arc.graphics.g2d.TextureAtlas;
import io.anuke.arc.scene.Scene;
import io.anuke.arc.util.I18NBundle;

/**
 * Environment class holding references to the {@link Application}, {@link Graphics}, {@link Audio}, {@link Files} and
 * {@link Input} instances. The references are held in public static fields which allows static access to all sub systems. Do not
 * use Graphics in a thread that is not the rendering thread.
 * <p>
 * This is normally a design faux pas but in this case is better than the alternatives.
 * @author mzechner
 */
public class Core{
    public static Application app;
    public static Graphics graphics;
    public static Audio audio;
    public static Input input;
    public static Files files;
    public static Settings settings;
    public static KeyBinds keybinds = new KeyBinds();
    public static Net net;

    public static I18NBundle bundle = I18NBundle.createEmptyBundle();
    public static Camera camera;
    public static Scene scene;
    public static AssetManager assets;
    public static TextureAtlas atlas;

    public static GL20 gl;
    public static GL20 gl20;
    public static GL30 gl30;
}
