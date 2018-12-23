package io.anuke.arc.graphics;

import io.anuke.arc.Graphics;
import io.anuke.arc.util.Disposable;

/**
 * <p>
 * Represents a mouse cursor. Create a cursor via
 * {@link Graphics#newCursor(Pixmap, int, int)}. To
 * set the cursor use {@link Graphics#setCursor(Cursor)}.
 * To use one of the system cursors, call Graphics#setSystemCursor
 * </p>
 **/
public interface Cursor extends Disposable{

    enum SystemCursor{
        arrow,
        ibeam,
        crosshair,
        hand,
        horizontalResize,
        verticalResize
    }
}
