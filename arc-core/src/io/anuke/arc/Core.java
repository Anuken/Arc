package io.anuke.arc;

import io.anuke.arc.assets.AssetManager;
import io.anuke.arc.graphics.Camera;
import io.anuke.arc.graphics.GL20;
import io.anuke.arc.graphics.GL30;
import io.anuke.arc.graphics.g2d.SpriteBatch;
import io.anuke.arc.graphics.g2d.TextureAtlas;
import io.anuke.arc.scene.Scene;
import io.anuke.arc.util.I18NBundle;

/**
 * Note that all resources are automatically disposed of in Application#dispose.
 * There is no need to dispose of them manually.*/
public class Core{
    public static Application app__;
    public static Graphics graphics;
    public static Audio audio;
    public static Input input;
    public static Files files;
    public static Settings settings;
    public static KeyBinds keybinds = new KeyBinds();
    public static Net net;

    public static I18NBundle bundle = I18NBundle.createEmptyBundle();
    public static Camera camera;
    public static SpriteBatch batch;
    public static Scene scene;
    public static AssetManager assets;
    public static TextureAtlas atlas;

    public static GL20 gl;
    public static GL20 gl20;
    public static GL30 gl30;
}
