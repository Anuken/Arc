package arc.graphics;

import arc.struct.OrderedMap;

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
        map.put("CLEAR", Color.clear);
        map.put("BLACK", Color.black);

        map.put("WHITE", Color.white);
        map.put("LIGHT_GRAY", Color.lightGray);
        map.put("GRAY", Color.gray);
        map.put("DARK_GRAY", Color.darkGray);

        map.put("BLUE", Color.blue);
        map.put("NAVY", Color.navy);
        map.put("ROYAL", Color.royal);
        map.put("SLATE", Color.slate);
        map.put("SKY", Color.sky);
        map.put("CYAN", Color.cyan);
        map.put("TEAL", Color.teal);

        map.put("GREEN", Color.green);
        map.put("ACID", Color.acid);
        map.put("LIME", Color.lime);
        map.put("FOREST", Color.forest);
        map.put("OLIVE", Color.olive);

        map.put("YELLOW", Color.yellow);
        map.put("GOLD", Color.gold);
        map.put("GOLDENROD", Color.goldenrod);
        map.put("ORANGE", Color.orange);

        map.put("BROWN", Color.brown);
        map.put("TAN", Color.tan);
        map.put("BRICK", Color.brick);

        map.put("RED", Color.red);
        map.put("SCARLET", Color.scarlet);
        map.put("CORAL", Color.coral);
        map.put("SALMON", Color.salmon);
        map.put("PINK", Color.pink);
        map.put("MAGENTA", Color.magenta);

        map.put("PURPLE", Color.purple);
        map.put("VIOLET", Color.violet);
        map.put("MAROON", Color.maroon);

        //lowercase versions too!

        map.put("clear", Color.clear);
        map.put("black", Color.black);

        map.put("white", Color.white);
        map.put("lightgray", Color.lightGray);
        map.put("gray", Color.gray);
        map.put("darkgray", Color.darkGray);

        map.put("blue", Color.blue);
        map.put("navy", Color.navy);
        map.put("royal", Color.royal);
        map.put("slate", Color.slate);
        map.put("sky", Color.sky);
        map.put("cyan", Color.cyan);
        map.put("teal", Color.teal);

        map.put("green", Color.green);
        map.put("acid", Color.acid);
        map.put("lime", Color.lime);
        map.put("forest", Color.forest);
        map.put("olive", Color.olive);

        map.put("yellow", Color.yellow);
        map.put("gold", Color.gold);
        map.put("goldenrod", Color.goldenrod);
        map.put("orange", Color.orange);

        map.put("brown", Color.brown);
        map.put("tan", Color.tan);
        map.put("brick", Color.brick);

        map.put("red", Color.red);
        map.put("scarlet", Color.scarlet);
        map.put("coral", Color.coral);
        map.put("salmon", Color.salmon);
        map.put("pink", Color.pink);
        map.put("magenta", Color.magenta);

        map.put("purple", Color.purple);
        map.put("violet", Color.violet);
        map.put("maroon", Color.maroon);
        map.put("crimson", Color.scarlet);
        map.put("scarlet", Color.scarlet);
    }

}
