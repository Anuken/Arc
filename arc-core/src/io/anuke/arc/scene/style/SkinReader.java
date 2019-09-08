package io.anuke.arc.scene.style;

import io.anuke.arc.Core;
import io.anuke.arc.collection.Array;
import io.anuke.arc.collection.ObjectMap;
import io.anuke.arc.files.FileHandle;
import io.anuke.arc.function.Supplier;
import io.anuke.arc.graphics.Color;
import io.anuke.arc.graphics.g2d.BitmapFont;
import io.anuke.arc.graphics.g2d.BitmapFont.BitmapFontData;
import io.anuke.arc.graphics.g2d.TextureRegion;
import io.anuke.arc.scene.Skin;
import io.anuke.arc.scene.ui.Button.ButtonStyle;
import io.anuke.arc.scene.ui.CheckBox.CheckBoxStyle;
import io.anuke.arc.scene.ui.ImageButton.ImageButtonStyle;
import io.anuke.arc.scene.ui.KeybindDialog.KeybindDialogStyle;
import io.anuke.arc.scene.ui.Label.LabelStyle;
import io.anuke.arc.scene.ui.ScrollPane.ScrollPaneStyle;
import io.anuke.arc.scene.ui.Slider.SliderStyle;
import io.anuke.arc.scene.ui.TextButton.TextButtonStyle;
import io.anuke.arc.scene.ui.TextField.TextFieldStyle;
import io.anuke.arc.scene.ui.Dialog.WindowStyle;
import io.anuke.arc.util.serialization.JsonValue;
import io.anuke.arc.util.serialization.SerializationException;

public class SkinReader{
    private static final ObjectMap<String, ValueReader<?>> readers = new ObjectMap<String, ValueReader<?>>(){{
        put("Font", (skin, value) -> {
            String path = value.getString("file");
            boolean flip = value.getBoolean("flip", false);
            boolean markup = value.getBoolean("markup", false);
            float scale = value.getFloat("scale", -1f);

            FileHandle fontFile = skin.getFile().parent().child(path);
            if(!fontFile.exists()) fontFile = Core.files.internal(path);
            if(!fontFile.exists()) throw new SerializationException("Font file not found: " + fontFile);

            // Use a region with the same name as the font, else use a PNG file in the same directory as the FNT file.
            String regionName = fontFile.nameWithoutExtension();
            BitmapFont font;
            Array<TextureRegion> regions = skin.getRegions(regionName);
            if(regions != null)
                font = new BitmapFont(new BitmapFontData(fontFile, flip), regions, true);
            else{
                TextureRegion region = skin.optional(regionName, TextureRegion.class);
                if(region != null)
                    font = new BitmapFont(fontFile, region, flip);
                else{
                    FileHandle imageFile = fontFile.parent().child(regionName + ".png");
                    if(imageFile.exists())
                        font = new BitmapFont(fontFile, imageFile, flip);
                    else
                        font = new BitmapFont(fontFile, flip);
                }
            }
            font.getData().markupEnabled = markup;
            // Scaled size is the desired cap height to scale the font to.
            if(scale > 0) font.getData().setScale(scale);
            return font;
        });

        put("Color", (skin, value) -> {
            if(value.isString()) return Color.valueOf(value.asString());
            String hex = value.getString("hex", null);
            if(hex != null) return Color.valueOf(hex);
            float r = value.getFloat("r", 0f);
            float g = value.getFloat("g", 0f);
            float b = value.getFloat("b", 0f);
            float a = value.getFloat("a", 0f);
            return new Color(r, g, b, a);
        });

        put("TintedDrawable", (skin, value) -> {
            String name = value.getString("name");
            Color color = (Color)get("Color").read(skin, value.get("color"));
            Drawable drawable = skin.newDrawable(name, color);
            if(drawable instanceof BaseDrawable){
                BaseDrawable named = (BaseDrawable)drawable;
                named.setName(value.name + " (" + name + ", " + color + ")");
            }
            return drawable;
        });

        put("ButtonStyle", style(ButtonStyle::new));
        put("TextButtonStyle", style(TextButtonStyle::new));
        put("ImageButtonStyle", style(ImageButtonStyle::new));
        put("ScrollPaneStyle", style(ScrollPaneStyle::new));
        put("KeybindDialogStyle", style(KeybindDialogStyle::new));
        put("SliderStyle", style(SliderStyle::new));
        put("LabelStyle", style(LabelStyle::new));
        put("TextFieldStyle", style(TextFieldStyle::new));
        put("CheckBoxStyle", style(CheckBoxStyle::new));
        put("WindowStyle", style(WindowStyle::new));
    }};

    private static <T extends Style> ValueReader<T> style(Supplier<T> sup){
        return (skin, value) -> {
            T t = sup.get();
            t.read(new ReadContext(skin, value));
            return t;
        };
    }

    public static ValueReader<?> getReader(String name){
        return readers.get(name);
    }

    public interface ValueReader<T>{
        T read(Skin skin, JsonValue value);
    }

    public static class ReadContext{
        private final Skin skin;
        private final JsonValue value;

        public ReadContext(Skin skin, JsonValue value){
            this.skin = skin;
            this.value = value;
        }

        public Color color(String name){
            return value.has(name) ? skin.optional(value.getString(name), Color.class) : null;
        }

        public Color rcolor(String name){
            if(!value.has(name)) throw new SerializationException("No value found: " + name);
            return skin.get(value.getString(name), Color.class);
        }

        public Drawable draw(String name){
            if(!value.has(name)) return null;
            return skin.getDrawable(value.getString(name));
        }

        public Drawable rdraw(String name){
            if(!value.has(name)) throw new SerializationException("No value found: " + name);
            return skin.get(value.getString(name), Drawable.class);
        }

        public BitmapFont font(String name){
            return value.has(name) ? skin.optional(value.getString(name), BitmapFont.class) : null;
        }

        public BitmapFont rfont(String name){
            if(!value.has(name)) throw new SerializationException("No value found: " + name);
            return skin.get(value.getString(name), BitmapFont.class);
        }

        public String str(String name, String def){
            return value.getString(name, def);
        }
    }
}
