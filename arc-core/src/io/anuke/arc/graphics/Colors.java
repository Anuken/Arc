package io.anuke.arc.graphics;

import io.anuke.arc.collection.OrderedMap;

/**
 * A general purpose class containing named colors that can be changed at will. For example, the markup language defined by the
 * {@code BitmapFontCache} class uses this class to retrieve colors. Custom colors can be defined here.
 * @author davebaol
 */
public final class Colors{

    private static final OrderedMap<String, Color> map = new OrderedMap<>();

    static{
        reset();
    }

    private Colors(){

    }

    /** Returns the color map. */
    public static OrderedMap<String, Color> getColors(){
        return map;
    }

    /**
     * Convenience method to lookup a color by {@code name}. The invocation of this method is equivalent to the expression
     * {@code Colors.getColors().get(name)}
     * @param name the name of the color
     * @return the color to which the specified {@code name} is mapped, or {@code null} if there was no mapping for {@code name}
     * .
     */
    public static Color get(String name){
        return map.get(name);
    }

    /**
     * Convenience method to add a {@code color} with its {@code name}. The invocation of this method is equivalent to the
     * expression {@code Colors.getColors().put(name, color)}
     * @param name the name of the color
     * @param color the color
     * @return the previous {@code color} associated with {@code name}, or {@code null} if there was no mapping for {@code name}
     * .
     */
    public static Color put(String name, Color color){
        return map.put(name, color);
    }

    /** Resets the color map to the predefined colors. */
    public static void reset(){
        map.clear();
        map.put("CLEAR", Color.CLEAR);
        map.put("BLACK", Color.BLACK);

        map.put("WHITE", Color.WHITE);
        map.put("LIGHT_GRAY", Color.LIGHT_GRAY);
        map.put("GRAY", Color.GRAY);
        map.put("DARK_GRAY", Color.DARK_GRAY);

        map.put("BLUE", Color.BLUE);
        map.put("NAVY", Color.NAVY);
        map.put("ROYAL", Color.ROYAL);
        map.put("SLATE", Color.SLATE);
        map.put("SKY", Color.SKY);
        map.put("CYAN", Color.CYAN);
        map.put("TEAL", Color.TEAL);

        map.put("GREEN", Color.GREEN);
        map.put("CHARTREUSE", Color.CHARTREUSE);
        map.put("LIME", Color.LIME);
        map.put("FOREST", Color.FOREST);
        map.put("OLIVE", Color.OLIVE);

        map.put("YELLOW", Color.YELLOW);
        map.put("GOLD", Color.GOLD);
        map.put("GOLDENROD", Color.GOLDENROD);
        map.put("ORANGE", Color.ORANGE);

        map.put("BROWN", Color.BROWN);
        map.put("TAN", Color.TAN);
        map.put("FIREBRICK", Color.FIREBRICK);

        map.put("RED", Color.RED);
        map.put("SCARLET", Color.SCARLET);
        map.put("CORAL", Color.CORAL);
        map.put("SALMON", Color.SALMON);
        map.put("PINK", Color.PINK);
        map.put("MAGENTA", Color.MAGENTA);

        map.put("PURPLE", Color.PURPLE);
        map.put("VIOLET", Color.VIOLET);
        map.put("MAROON", Color.MAROON);

        //lowercase versions too!

        map.put("clear", Color.CLEAR);
        map.put("black", Color.BLACK);

        map.put("white", Color.WHITE);
        map.put("lightgray", Color.LIGHT_GRAY);
        map.put("gray", Color.GRAY);
        map.put("darkgray", Color.DARK_GRAY);

        map.put("blue", Color.BLUE);
        map.put("navy", Color.NAVY);
        map.put("royal", Color.ROYAL);
        map.put("slate", Color.SLATE);
        map.put("sky", Color.SKY);
        map.put("cyan", Color.CYAN);
        map.put("teal", Color.TEAL);

        map.put("green", Color.GREEN);
        map.put("charteuse", Color.CHARTREUSE);
        map.put("lime", Color.LIME);
        map.put("forest", Color.FOREST);
        map.put("olive", Color.OLIVE);

        map.put("yellow", Color.YELLOW);
        map.put("gold", Color.GOLD);
        map.put("goldenrod", Color.GOLDENROD);
        map.put("orange", Color.ORANGE);

        map.put("brown", Color.BROWN);
        map.put("tan", Color.TAN);
        map.put("firebrick", Color.FIREBRICK);

        map.put("red", Color.RED);
        map.put("scarlet", Color.SCARLET);
        map.put("coral", Color.CORAL);
        map.put("salmon", Color.SALMON);
        map.put("pink", Color.PINK);
        map.put("magneta", Color.MAGENTA);

        map.put("purple", Color.PURPLE);
        map.put("violet", Color.VIOLET);
        map.put("maroon", Color.MAROON);
        map.put("crimson", Color.SCARLET);
        map.put("scarlet", Color.SCARLET);
    }

}
