package io.anuke.arc.util;

/**
 * Provides bit flag constants for alignment.
 * @author Nathan Sweet
 */
public class Align{
    static public final int center = 1 << 0;
    static public final int top = 1 << 1;
    static public final int bottom = 1 << 2;
    static public final int left = 1 << 3;
    static public final int right = 1 << 4;

    static public final int topLeft = top | left;
    static public final int topRight = top | right;
    static public final int bottomLeft = bottom | left;
    static public final int bottomRight = bottom | right;

    static public boolean isLeft(int align){
        return (align & left) != 0;
    }

    static public boolean isRight(int align){
        return (align & right) != 0;
    }

    static public boolean isTop(int align){
        return (align & top) != 0;
    }

    static public boolean isBottom(int align){
        return (align & bottom) != 0;
    }

    static public boolean isCenterVertical(int align){
        return (align & top) == 0 && (align & bottom) == 0;
    }

    static public boolean isCenterHorizontal(int align){
        return (align & left) == 0 && (align & right) == 0;
    }

    static public String toString(int align){
        StringBuilder buffer = new StringBuilder(13);
        if((align & top) != 0)
            buffer.append("top,");
        else if((align & bottom) != 0)
            buffer.append("bottom,");
        else
            buffer.append("center,");
        if((align & left) != 0)
            buffer.append("left");
        else if((align & right) != 0)
            buffer.append("right");
        else
            buffer.append("center");
        return buffer.toString();
    }
}
