package io.anuke.arc.backends.gwt.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;

public class TextInputDialogBox extends DialogBox{
    TextInputDialogListener listener;
    private PlaceholderTextBox textBox;

    public TextInputDialogBox(String title, String text, String placeholder){
        // Set the dialog box's caption.
        setText(title);

        VerticalPanel vPanel = new VerticalPanel();
        HorizontalPanel hPanel = new HorizontalPanel();

        // Enable animation.
        setAnimationEnabled(true);

        // Enable glass background.
        setGlassEnabled(true);

        // Center this bad boy.
        center();

        vPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

        Button ok = new Button("OK");
        ok.addClickHandler(new ClickHandler(){
            public void onClick(ClickEvent event){
                TextInputDialogBox.this.onPositive();
            }
        });

        Button cancel = new Button("Cancel");
        cancel.addClickHandler(new ClickHandler(){
            public void onClick(ClickEvent event){
                TextInputDialogBox.this.onNegative();
            }
        });

        hPanel.add(ok);
        hPanel.add(cancel);

        textBox = new PlaceholderTextBox();
        textBox.setPlaceholder(placeholder);
        textBox.setWidth("97%");
        textBox.setText(text);
        vPanel.add(textBox);
        vPanel.add(hPanel);

        setWidget(vPanel);
    }

    protected void onPositive(){
        if(listener != null){
            listener.onPositive(textBox.getText());
        }
        this.hide();
    }

    protected void onNegative(){
        if(listener != null){
            listener.onNegative();
        }
        this.hide();
    }

    public void setListener(TextInputDialogListener listener){
        this.listener = listener;
    }

    public interface TextInputDialogListener{
        void onPositive(String text);

        void onNegative();
    }
}
