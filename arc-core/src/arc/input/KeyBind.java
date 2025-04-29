package arc.input;

import arc.struct.*;
import arc.util.*;

import static arc.Core.*;

public class KeyBind{
    public static final Seq<KeyBind> all = new Seq<>();

    public final String name;
    public final KeybindValue defaultValue;
    public final @Nullable String category;
    public Axis value;

    /**
     * Registers a new key binding.
     * @param category Name of the category in the list of keybinds. This should be your mod name. In a bundle, it uses the key `category.{name}.name`.
     * @param name Unique name of the keybind.
     * @param defaultValue The default value for this key; can be an Axis or a KeyCode.
     */
    public static KeyBind add(String name, KeybindValue defaultValue, String category){
        return new KeyBind(name, defaultValue, category);
    }

    /**
     * Registers a new key binding without a category. Not for use in mods. Use the constructor with a category.
     * @param name Unique name of the keybind.
     * @param defaultValue The default value for this key; can be an Axis or a KeyCode.
     */
    public static KeyBind add(String name, KeybindValue defaultValue){
        return new KeyBind(name, defaultValue, null);
    }

    public static void saveAll(){
        for(KeyBind bind : all){
            String name = bind.settingsKey();
            settings.put(name + "-single", bind.value.key != null);

            if(bind.value.key != null){
                settings.put(name + "-key", bind.value.key.ordinal());
            }else{
                settings.put(name + "-min", bind.value.min.ordinal());
                settings.put(name + "-max", bind.value.max.ordinal());
            }
        }
    }

    /** Loads the internal keybind data from settings' map. Does not call settings.load(). */
    public static void loadAll(){

        for(KeyBind bind : all){
            Axis loaded;
            String name = bind.settingsKey();
            if(settings.getBool(name + "-single", true)){
                KeyCode key = KeyCode.byOrdinal(settings.getInt(name + "-key", KeyCode.unset.ordinal()));
                loaded = key == KeyCode.unset ? null : new Axis(key);
            }else{
                KeyCode min = KeyCode.byOrdinal(settings.getInt(name + "-min", KeyCode.unset.ordinal()));
                KeyCode max = KeyCode.byOrdinal(settings.getInt(name + "-max", KeyCode.unset.ordinal()));
                loaded = min == KeyCode.unset || max == KeyCode.unset ? null : new Axis(min, max);
            }

            if(loaded != null){
                bind.value = loaded;
            }
        }
    }

    public static void resetAll(){
        for(KeyBind def : all){
            def.resetToDefault();
        }
    }

    protected KeyBind(String name, KeybindValue defaultValue, String category){
        this.name = name;
        this.defaultValue = defaultValue;
        this.category = category;
        this.value = defaultValue instanceof Axis ? (Axis)defaultValue : new Axis((KeyCode)defaultValue);

        all.add(this);
    }

    public void resetToDefault(){
        String name = settingsKey();
        settings.remove(name + "-single");
        settings.remove(name + "-key");
        settings.remove(name + "-min");
        settings.remove(name + "-max");
    }

    public boolean isDefault(){
        if(defaultValue instanceof Axis){
            return ((Axis)defaultValue).max == value.max && ((Axis)defaultValue).min == value.min;
        }else{
            return defaultValue == value.key;
        }
    }

    String settingsKey(){
        return "keybind-default-keyboard-" + name;
    }

    /** Represents an Axis or a KeyCode. */
    public interface KeybindValue{}

    public static class Axis implements KeybindValue{
        public @Nullable KeyCode min, max;
        public @Nullable KeyCode key;

        /** Cosntructor for axis-type keys only. */
        public Axis(KeyCode key){
            this.key = key;
            this.min = max = null;
        }

        /** Constructor for keyboards/mice, or multiple buttons on a controller. */
        public Axis(KeyCode min, KeyCode max){
            this.min = min;
            this.max = max;
            this.key = null;
        }
    }
}
