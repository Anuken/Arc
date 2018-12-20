/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package io.anuke.arc.scene.ui;

import io.anuke.arc.Core;
import io.anuke.arc.collection.Array;
import io.anuke.arc.function.BooleanProvider;
import io.anuke.arc.math.geom.Vector2;
import io.anuke.arc.scene.Element;
import io.anuke.arc.scene.Scene;
import io.anuke.arc.scene.Skin;
import io.anuke.arc.scene.event.ChangeListener.ChangeEvent;
import io.anuke.arc.scene.event.ClickListener;
import io.anuke.arc.scene.event.HandCursorListener;
import io.anuke.arc.scene.event.InputEvent;
import io.anuke.arc.scene.event.Touchable;
import io.anuke.arc.scene.style.Drawable;
import io.anuke.arc.scene.style.SkinReader.ReadContext;
import io.anuke.arc.scene.style.Style;
import io.anuke.arc.scene.ui.layout.Table;
import io.anuke.arc.scene.utils.Disableable;
import io.anuke.arc.utils.pooling.Pools;

import static io.anuke.arc.Core.input;
import static io.anuke.arc.Core.scene;

/**
 * A button is a {@link Table} with a checked state and additional {@link ButtonStyle style} fields for pressed, unpressed, and
 * checked. Each time a button is clicked, the checked state is toggled. Being a table, a button can contain any other actors.<br>
 * <br>
 * The button's padding is set to the background drawable's padding when the background changes, overwriting any padding set
 * manually. Padding can still be set on the button's table cells.
 * <p>
 * {@link ChangeEvent} is fired when the button is clicked. Cancelling the event will restore the checked button state to what is
 * was previously.
 * <p>
 * The preferred size of the button is determined by the background and the button contents.
 * @author Nathan Sweet
 */
public class Button extends Table implements Disableable{
    boolean isChecked, isDisabled;
    ButtonGroup buttonGroup;
    BooleanProvider disabledProvider;
    private ButtonStyle style;
    private ClickListener clickListener;
    private boolean programmaticChangeEvents;
    private float transitionTime;

    public Button(String styleName){
        initialize();
        setStyle(scene.skin.get(styleName, ButtonStyle.class));
        setSize(getPrefWidth(), getPrefHeight());
    }

    public Button(Element child, String styleName){
        this(child, scene.skin.get(styleName, ButtonStyle.class));
    }

    public Button(Element child, ButtonStyle style){
        initialize();
        add(child);
        setStyle(style);
        setSize(getPrefWidth(), getPrefHeight());
    }

    public Button(ButtonStyle style){
        initialize();
        setStyle(style);
        setSize(getPrefWidth(), getPrefHeight());
    }

    /** Creates a button without setting the style or size. At least a style must be set before using this button. */
    public Button(){
        initialize();
    }

    public Button(Drawable up){
        this(new ButtonStyle(up, null, null));
    }

    public Button(Drawable up, Drawable down){
        this(new ButtonStyle(up, down, null));
    }

    public Button(Drawable up, Drawable down, Drawable checked){
        this(new ButtonStyle(up, down, checked));
    }

    public Button(Element child, Skin skin){
        this(child, skin.get(ButtonStyle.class));
    }

    @Override
    public void act(float delta){
        super.act(delta);

        if(disabledProvider != null){
            setDisabled(disabledProvider.get());
        }
    }

    private void initialize(){
        touchable(Touchable.enabled);
        addListener(clickListener = new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if(isDisabled()) return;
                setChecked(!isChecked, true);
            }
        });
        addListener(new HandCursorListener());
    }

    @SuppressWarnings("unchecked")
    void setChecked(boolean isChecked, boolean fireEvent){
        if(this.isChecked == isChecked) return;
        if(buttonGroup != null && !buttonGroup.canCheck(this, isChecked)) return;
        this.isChecked = isChecked;

        if(fireEvent){
            ChangeEvent changeEvent = Pools.obtain(ChangeEvent.class, ChangeEvent::new);
            if(fire(changeEvent)) this.isChecked = !isChecked;
            Pools.free(changeEvent);
        }
    }

    /**
     * Toggles the checked state. This method changes the checked state, which fires a {@link ChangeEvent} (if programmatic change
     * events are enabled), so can be used to simulate a button click.
     */
    public void toggle(){
        setChecked(!isChecked);
    }

    public boolean isChecked(){
        return isChecked;
    }

    public void setChecked(boolean isChecked){
        setChecked(isChecked, programmaticChangeEvents);
    }

    public boolean isPressed(){
        return clickListener.isVisualPressed();
    }

    public boolean isOver(){
        return clickListener.isOver();
    }

    public ClickListener getClickListener(){
        return clickListener;
    }

    public boolean isDisabled(){
        return isDisabled;
    }

    public void setDisabled(BooleanProvider prov){
        this.disabledProvider = prov;
    }

    /** When true, the button will not toggle {@link #isChecked()} when clicked and will not fire a {@link ChangeEvent}. */
    public void setDisabled(boolean isDisabled){
        this.isDisabled = isDisabled;
    }

    public boolean childrenPressed(){
        boolean[] b = {false};
        Vector2 v = new Vector2();

        forEach(element -> {
            element.stageToLocalCoordinates(v.set(input.mouseX(), input.mouseY()));
            if(element instanceof Button && (((Button)element).getClickListener().isOver(element, v.x, v.y))){
                b[0] = true;
            }
        });

        return b[0];
    }

    /**
     * If false, {@link #setChecked(boolean)} and {@link #toggle()} will not fire {@link ChangeEvent}, event will be fired only
     * when user clicked the button
     */
    public void setProgrammaticChangeEvents(boolean programmaticChangeEvents){
        this.programmaticChangeEvents = programmaticChangeEvents;
    }

    /**
     * Returns the button's style. Modifying the returned style may not have an effect until {@link #setStyle(ButtonStyle)} is
     * called.
     */
    public ButtonStyle getStyle(){
        return style;
    }

    public void setStyle(ButtonStyle style){
        if(style == null) throw new IllegalArgumentException("style cannot be null.");
        this.style = style;

        Drawable background;
        if(isPressed() && !isDisabled()){
            background = style.down == null ? style.up : style.down;
        }else{
            if(isDisabled() && style.disabled != null)
                background = style.disabled;
            else if(isChecked && style.checked != null)
                background = (isOver() && style.checkedOver != null) ? style.checkedOver : style.checked;
            else if(isOver() && style.over != null)
                background = style.over;
            else
                background = style.up;
        }
        setBackground(background);
    }

    /** @return May be null. */
    public ButtonGroup getButtonGroup(){
        return buttonGroup;
    }

    public void draw(){
        validate();

        boolean isDisabled = isDisabled();
        boolean isPressed = isPressed();
        boolean isChecked = isChecked();
        boolean isOver = isOver();
        boolean drawOver = false;

        if(isOver){
            transitionTime += Core.graphics.getDeltaTime() * 60f;
        }else{
            transitionTime -= Core.graphics.getDeltaTime() * 60f;
            if(transitionTime < 0) transitionTime = 0;
        }

        Drawable background = null;
        if(isDisabled && style.disabled != null)
            background = style.disabled;
        else if(isPressed && style.down != null)
            background = style.down;
        else if(isChecked && style.checked != null)
            background = (style.checkedOver != null && isOver) ? style.checkedOver : style.checked;
        else if(isOver && style.over != null){
            if(transitionTime >= style.transition)
                background = style.over;
            else
                drawOver = true;
        }else if(style.up != null)
            background = style.up;

        if(drawOver)
            background = style.up;

        setBackground(background);

        float offsetX, offsetY;
        if(isPressed && !isDisabled){
            offsetX = style.pressedOffsetX;
            offsetY = style.pressedOffsetY;
        }else if(isChecked && !isDisabled){
            offsetX = style.checkedOffsetX;
            offsetY = style.checkedOffsetY;
        }else{
            offsetX = style.unpressedOffsetX;
            offsetY = style.unpressedOffsetY;
        }

        Array<Element> children = getChildren();
        for(int i = 0; i < children.size; i++)
            children.get(i).moveBy(offsetX, offsetY);
        super.draw();
        for(int i = 0; i < children.size; i++)
            children.get(i).moveBy(-offsetX, -offsetY);

        Scene stage = getScene();
        if(stage != null && stage.getActionsRequestRendering() && isPressed != clickListener.isPressed())
            Core.graphics.requestRendering();
    }

    public float getPrefWidth(){
        float width = super.getPrefWidth();
        if(style.up != null) width = Math.max(width, style.up.getMinWidth());
        if(style.down != null) width = Math.max(width, style.down.getMinWidth());
        if(style.checked != null) width = Math.max(width, style.checked.getMinWidth());
        return width;
    }

    public float getPrefHeight(){
        float height = super.getPrefHeight();
        if(style.up != null) height = Math.max(height, style.up.getMinHeight());
        if(style.down != null) height = Math.max(height, style.down.getMinHeight());
        if(style.checked != null) height = Math.max(height, style.checked.getMinHeight());
        return height;
    }

    public float getMinWidth(){
        return getPrefWidth();
    }

    public float getMinHeight(){
        return getPrefHeight();
    }

    /**
     * The style for a button, see {@link Button}.
     * @author mzechner
     */
    static public class ButtonStyle extends Style{
        /** Optional. */
        public Drawable up, down, over, checked, checkedOver, disabled;
        /** Optional. */
        public float pressedOffsetX, pressedOffsetY, unpressedOffsetX,
        unpressedOffsetY, checkedOffsetX, checkedOffsetY, transition = -1;

        public ButtonStyle(){
        }

        public ButtonStyle(Drawable up, Drawable down, Drawable checked){
            this.up = up;
            this.down = down;
            this.checked = checked;
        }

        public ButtonStyle(ButtonStyle style){
            this.up = style.up;
            this.down = style.down;
            this.over = style.over;
            this.checked = style.checked;
            this.checkedOver = style.checkedOver;
            this.disabled = style.disabled;
            this.pressedOffsetX = style.pressedOffsetX;
            this.pressedOffsetY = style.pressedOffsetY;
            this.unpressedOffsetX = style.unpressedOffsetX;
            this.unpressedOffsetY = style.unpressedOffsetY;
            this.checkedOffsetX = style.checkedOffsetX;
            this.checkedOffsetY = style.checkedOffsetY;
            this.transition = style.transition;
        }

        @Override
        public void read(ReadContext read){
            up = read.draw("up");
            down = read.draw("down");
            over = read.draw("over");
            checked = read.draw("checked");
            checkedOver = read.draw("checkedOver");
            disabled = read.draw("disabled");

            //TODO read offsets
        }
    }
}
