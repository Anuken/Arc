package io.anuke.arc.scene.utils;

import io.anuke.arc.function.BooleanConsumer;
import io.anuke.arc.function.Consumer;
import io.anuke.arc.graphics.Color;
import io.anuke.arc.scene.ui.CheckBox;
import io.anuke.arc.scene.ui.ImageButton;
import io.anuke.arc.scene.ui.TextButton;
import io.anuke.arc.scene.ui.TextField;

import static io.anuke.arc.Core.scene;

public class Elements{

    public static CheckBox newCheck(String text, BooleanConsumer listener){
        CheckBox button = new CheckBox(text);
        if(listener != null)
            button.changed(() -> listener.accept(button.isChecked()));
        return button;
    }

    public static TextButton newButton(String text, Runnable listener){
        TextButton button = new TextButton(text);
        if(listener != null)
            button.changed(listener);

        return button;
    }

    public static TextButton newButton(String text, String style, Runnable listener){
        TextButton button = new TextButton(text, style);
        if(listener != null)
            button.changed(listener);

        return button;
    }

    public static ImageButton newImageButton(String icon, Runnable listener){
        ImageButton button = new ImageButton(scene.skin.getDrawable(icon));
        if(listener != null)
            button.changed(listener);
        return button;
    }

    public static ImageButton newImageButton(String icon, float size, Runnable listener){
        ImageButton button = new ImageButton(scene.skin.getDrawable(icon));
        button.resizeImage(size);
        if(listener != null)
            button.changed(listener);
        return button;
    }

    public static ImageButton newImageButton(String style, String icon, float size, Runnable listener){
        ImageButton button = new ImageButton(icon, style);
        button.resizeImage(size);
        if(listener != null)
            button.changed(listener);
        return button;
    }

    public static ImageButton newImageButton(String icon, float size, Color color, Runnable listener){
        ImageButton button = new ImageButton(scene.skin.getDrawable(icon));
        button.resizeImage(size);
        button.getImage().setColor(color);
        if(listener != null)
            button.changed(listener);
        return button;
    }

    public static ImageButton newToggleImageButton(String icon, float size, boolean on, BooleanConsumer listener){
        ImageButton button = new ImageButton(icon, "toggle");
        button.setChecked(on);
        button.resizeImage(size);
        button.clicked(() -> listener.accept(button.isChecked()));
        return button;
    }

    public static TextField newField(String text, Consumer<String> listener){
        TextField field = new TextField(text);
        if(listener != null)
            field.changed(() -> listener.accept(field.getText()));

        return field;
    }
}
