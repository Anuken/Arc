package io.anuke.arc.backends.gwt.widgets;

import com.google.gwt.user.client.ui.TextBox;

public class PlaceholderTextBox extends TextBox{

    String placeholder = "";

    /** Creates an empty text box. */
    public PlaceholderTextBox(){
    }

    /**
     * Gets the current placeholder text for the text box.
     * @return the current placeholder text
     */
    public String getPlaceholder(){
        return placeholder;
    }

    /**
     * Sets the placeholder text displayed in the text box.
     * @param text the placeholder text
     */
    public void setPlaceholder(String text){
        placeholder = (text != null ? text : "");
        getElement().setPropertyString("placeholder", placeholder);
    }
}
