package io.anuke.arc.utils;

import io.anuke.arc.graphics.Pixmap;

/**
 * A very simple clipboard interface for text and image content.
 * @author mzechner
 */
public interface Clipboard{
    /**
     * gets the current content of the clipboard if it contains text
     * @return the clipboard content or null
     */
    String getContents();

    /**
     * Sets the content of the system clipboard.
     * @param content the content as text
     */
    void setContents(String content);

    /** Sets the content of the clipboard as an image. */
    default void setContents(Pixmap pixmap){
        //not yet implemented
    }
}
