package io.anuke.arc.scene.ui.layout;

import io.anuke.arc.graphics.Color;
import io.anuke.arc.graphics.g2d.Draw;
import io.anuke.arc.scene.Element;
import io.anuke.arc.scene.event.Touchable;
import io.anuke.arc.scene.style.Drawable;
import io.anuke.arc.scene.utils.Layout;
import io.anuke.arc.util.Align;

import static io.anuke.arc.Core.scene;

/**
 * A group with a single child that sizes and positions the child using constraints. This provides layout similar to a
 * {@link Table} with a single cell but is more lightweight.
 * @author Nathan Sweet
 */
public class Container<T extends Element> extends WidgetGroup{
    private T actor;
    private Value minWidth = Value.minWidth, minHeight = Value.minHeight;
    private Value prefWidth = Value.prefWidth, prefHeight = Value.prefHeight;
    private Value maxWidth = Value.zero, maxHeight = Value.zero;
    private Value marginTop = Value.zero, marginLeft = Value.zero, marginBottom = Value.zero, marginRight = Value.zero;
    private float fillX, fillY;
    private int align;
    private Drawable background;
    private boolean clip;
    private boolean round = true;

    /** Creates a container with no actor. */
    public Container(){
        touchable(Touchable.childrenOnly);
        setTransform(false);
    }

    public Container(T actor){
        this();
        setActor(actor);
    }

    public void draw(){
        validate();
        if(isTransform()){
            applyTransform(computeTransform());
            drawBackground(0, 0);
            if(clip){
                Draw.flush();
                float marginLeft = this.marginLeft.get(this), marginBottom = this.marginBottom.get(this);
                if(clipBegin(marginLeft, marginBottom, getWidth() - marginLeft - marginRight.get(this),
                getHeight() - marginBottom - marginTop.get(this))){
                    drawChildren();
                    Draw.flush();
                    clipEnd();
                }
            }else
                drawChildren();
            resetTransform();
        }else{
            drawBackground(getX(), getY());
            super.draw();
        }
    }

    /**
     * Called to draw the background, before clipping is applied (if enabled). Default implementation draws the background
     * drawable.
     */
    protected void drawBackground(float x, float y){
        if(background == null) return;
        Color color = getColor();
        Draw.color(color.r, color.g, color.b, color.a * parentAlpha);
        background.draw(x, y, getWidth(), getHeight());
    }

    /**
     * Sets the background drawable and, if adjustPadding is true, sets the container's marginding to
     * {@link Drawable#getBottomHeight()} , {@link Drawable#getTopHeight()}, {@link Drawable#getLeftWidth()}, and
     * {@link Drawable#getRightWidth()}.
     * @param background If null, the background will be cleared and marginding removed.
     */
    public void setBackground(Drawable background, boolean adjustPadding){
        if(this.background == background) return;
        this.background = background;
        if(adjustPadding){
            if(background == null)
                margin(Value.zero);
            else
                margin(background.getTopHeight(), background.getLeftWidth(), background.getBottomHeight(), background.getRightWidth());
            invalidate();
        }
    }

    /** @see #setBackground(Drawable) */
    public Container<T> background(Drawable background){
        setBackground(background);
        return this;
    }

    public Container<T> background(String background){
        setBackground(scene.skin.getDrawable(background));
        return this;
    }

    public Drawable getBackground(){
        return background;
    }

    /**
     * Sets the background drawable and adjusts the container's marginding to match the background.
     * @see #setBackground(Drawable, boolean)
     */
    public void setBackground(Drawable background){
        setBackground(background, true);
    }

    public void layout(){
        if(actor == null) return;

        float marginLeft = this.marginLeft.get(this), marginBottom = this.marginBottom.get(this);
        float containerWidth = getWidth() - marginLeft - marginRight.get(this);
        float containerHeight = getHeight() - marginBottom - marginTop.get(this);
        float minWidth = this.minWidth.get(actor), minHeight = this.minHeight.get(actor);
        float prefWidth = this.prefWidth.get(actor), prefHeight = this.prefHeight.get(actor);
        float maxWidth = this.maxWidth.get(actor), maxHeight = this.maxHeight.get(actor);

        float width;
        if(fillX > 0)
            width = containerWidth * fillX;
        else
            width = Math.min(prefWidth, containerWidth);
        if(width < minWidth) width = minWidth;
        if(maxWidth > 0 && width > maxWidth) width = maxWidth;

        float height;
        if(fillY > 0)
            height = containerHeight * fillY;
        else
            height = Math.min(prefHeight, containerHeight);
        if(height < minHeight) height = minHeight;
        if(maxHeight > 0 && height > maxHeight) height = maxHeight;

        float x = marginLeft;
        if((align & Align.right) != 0)
            x += containerWidth - width;
        else if((align & Align.left) == 0) // center
            x += (containerWidth - width) / 2;

        float y = marginBottom;
        if((align & Align.top) != 0)
            y += containerHeight - height;
        else if((align & Align.bottom) == 0) // center
            y += (containerHeight - height) / 2;

        if(round){
            x = Math.round(x);
            y = Math.round(y);
            width = Math.round(width);
            height = Math.round(height);
        }

        actor.setBounds(x, y, width, height);
        if(actor instanceof Layout) actor.validate();
    }

    /** @return May be null. */
    public T getActor(){
        return actor;
    }

    /** @param actor May be null. */
    public void setActor(T actor){
        if(actor == this) throw new IllegalArgumentException("actor cannot be the Container.");
        if(actor == this.actor) return;
        if(this.actor != null) super.removeChild(this.actor);
        this.actor = actor;
        if(actor != null) super.addChild(actor);
    }

    public boolean removeChild(Element actor){
        if(actor == null) throw new IllegalArgumentException("actor cannot be null.");
        if(actor != this.actor) return false;
        setActor(null);
        return true;
    }

    public boolean removeChild(Element actor, boolean unfocus){
        if(actor == null) throw new IllegalArgumentException("actor cannot be null.");
        if(actor != this.actor) return false;
        this.actor = null;
        return super.removeChild(actor, unfocus);
    }

    /** Sets the minWidth, prefWidth, maxWidth, minHeight, prefHeight, and maxHeight to the specified value. */
    public Container<T> size(Value size){
        if(size == null) throw new IllegalArgumentException("size cannot be null.");
        minWidth = size;
        minHeight = size;
        prefWidth = size;
        prefHeight = size;
        maxWidth = size;
        maxHeight = size;
        return this;
    }

    /** Sets the minWidth, prefWidth, maxWidth, minHeight, prefHeight, and maxHeight to the specified values. */
    public Container<T> size(Value width, Value height){
        if(width == null) throw new IllegalArgumentException("width cannot be null.");
        if(height == null) throw new IllegalArgumentException("height cannot be null.");
        minWidth = width;
        minHeight = height;
        prefWidth = width;
        prefHeight = height;
        maxWidth = width;
        maxHeight = height;
        return this;
    }

    /** Sets the minWidth, prefWidth, maxWidth, minHeight, prefHeight, and maxHeight to the specified value. */
    public Container<T> size(float size){
        size(e -> (size));
        return this;
    }

    /** Sets the minWidth, prefWidth, maxWidth, minHeight, prefHeight, and maxHeight to the specified values. */
    public Container<T> size(float width, float height){
        size(e -> (width), e -> (height));
        return this;
    }

    /** Sets the minWidth, prefWidth, and maxWidth to the specified value. */
    public Container<T> width(Value width){
        if(width == null) throw new IllegalArgumentException("width cannot be null.");
        minWidth = width;
        prefWidth = width;
        maxWidth = width;
        return this;
    }

    /** Sets the minWidth, prefWidth, and maxWidth to the specified value. */
    public Container<T> width(float width){
        width(e -> (width));
        return this;
    }

    /** Sets the minHeight, prefHeight, and maxHeight to the specified value. */
    public Container<T> height(Value height){
        if(height == null) throw new IllegalArgumentException("height cannot be null.");
        minHeight = height;
        prefHeight = height;
        maxHeight = height;
        return this;
    }

    /** Sets the minHeight, prefHeight, and maxHeight to the specified value. */
    public Container<T> height(float height){
        height(e -> (height));
        return this;
    }

    /** Sets the minWidth and minHeight to the specified value. */
    public Container<T> minSize(Value size){
        if(size == null) throw new IllegalArgumentException("size cannot be null.");
        minWidth = size;
        minHeight = size;
        return this;
    }

    /** Sets the minWidth and minHeight to the specified values. */
    public Container<T> minSize(Value width, Value height){
        if(width == null) throw new IllegalArgumentException("width cannot be null.");
        if(height == null) throw new IllegalArgumentException("height cannot be null.");
        minWidth = width;
        minHeight = height;
        return this;
    }

    public Container<T> minWidth(Value minWidth){
        if(minWidth == null) throw new IllegalArgumentException("minWidth cannot be null.");
        this.minWidth = minWidth;
        return this;
    }

    public Container<T> minHeight(Value minHeight){
        if(minHeight == null) throw new IllegalArgumentException("minHeight cannot be null.");
        this.minHeight = minHeight;
        return this;
    }

    /** Sets the minWidth and minHeight to the specified value. */
    public Container<T> minSize(float size){
        minSize(e -> (size));
        return this;
    }

    /** Sets the minWidth and minHeight to the specified values. */
    public Container<T> minSize(float width, float height){
        minSize(e -> (width), e -> (height));
        return this;
    }

    public Container<T> minWidth(float minWidth){
        this.minWidth = e -> (minWidth);
        return this;
    }

    public Container<T> minHeight(float minHeight){
        this.minHeight = e -> (minHeight);
        return this;
    }

    /** Sets the prefWidth and prefHeight to the specified value. */
    public Container<T> prefSize(Value size){
        if(size == null) throw new IllegalArgumentException("size cannot be null.");
        prefWidth = size;
        prefHeight = size;
        return this;
    }

    /** Sets the prefWidth and prefHeight to the specified values. */
    public Container<T> prefSize(Value width, Value height){
        if(width == null) throw new IllegalArgumentException("width cannot be null.");
        if(height == null) throw new IllegalArgumentException("height cannot be null.");
        prefWidth = width;
        prefHeight = height;
        return this;
    }

    public Container<T> prefWidth(Value prefWidth){
        if(prefWidth == null) throw new IllegalArgumentException("prefWidth cannot be null.");
        this.prefWidth = prefWidth;
        return this;
    }

    public Container<T> prefHeight(Value prefHeight){
        if(prefHeight == null) throw new IllegalArgumentException("prefHeight cannot be null.");
        this.prefHeight = prefHeight;
        return this;
    }

    /** Sets the prefWidth and prefHeight to the specified value. */
    public Container<T> prefSize(float width, float height){
        prefSize(e -> (width), e -> (height));
        return this;
    }

    /** Sets the prefWidth and prefHeight to the specified values. */
    public Container<T> prefSize(float size){
        prefSize(e -> (size));
        return this;
    }

    public Container<T> prefWidth(float prefWidth){
        this.prefWidth = e -> (prefWidth);
        return this;
    }

    public Container<T> prefHeight(float prefHeight){
        this.prefHeight = e -> (prefHeight);
        return this;
    }

    /** Sets the maxWidth and maxHeight to the specified value. */
    public Container<T> maxSize(Value size){
        if(size == null) throw new IllegalArgumentException("size cannot be null.");
        maxWidth = size;
        maxHeight = size;
        return this;
    }

    /** Sets the maxWidth and maxHeight to the specified values. */
    public Container<T> maxSize(Value width, Value height){
        if(width == null) throw new IllegalArgumentException("width cannot be null.");
        if(height == null) throw new IllegalArgumentException("height cannot be null.");
        maxWidth = width;
        maxHeight = height;
        return this;
    }

    public Container<T> maxWidth(Value maxWidth){
        if(maxWidth == null) throw new IllegalArgumentException("maxWidth cannot be null.");
        this.maxWidth = maxWidth;
        return this;
    }

    public Container<T> maxHeight(Value maxHeight){
        if(maxHeight == null) throw new IllegalArgumentException("maxHeight cannot be null.");
        this.maxHeight = maxHeight;
        return this;
    }

    /** Sets the maxWidth and maxHeight to the specified value. */
    public Container<T> maxSize(float size){
        maxSize(e -> (size));
        return this;
    }

    /** Sets the maxWidth and maxHeight to the specified values. */
    public Container<T> maxSize(float width, float height){
        maxSize(e -> (width), e -> (height));
        return this;
    }

    public Container<T> maxWidth(float maxWidth){
        this.maxWidth = e -> (maxWidth);
        return this;
    }

    public Container<T> maxHeight(float maxHeight){
        this.maxHeight = e -> (maxHeight);
        return this;
    }

    /** Sets the marginTop, marginLeft, marginBottom, and marginRight to the specified value. */
    public Container<T> margin(Value margin){
        if(margin == null) throw new IllegalArgumentException("margin cannot be null.");
        marginTop = margin;
        marginLeft = margin;
        marginBottom = margin;
        marginRight = margin;
        return this;
    }

    public Container<T> margin(Value top, Value left, Value bottom, Value right){
        if(top == null) throw new IllegalArgumentException("top cannot be null.");
        if(left == null) throw new IllegalArgumentException("left cannot be null.");
        if(bottom == null) throw new IllegalArgumentException("bottom cannot be null.");
        if(right == null) throw new IllegalArgumentException("right cannot be null.");
        marginTop = top;
        marginLeft = left;
        marginBottom = bottom;
        marginRight = right;
        return this;
    }

    public Container<T> marginTop(Value marginTop){
        if(marginTop == null) throw new IllegalArgumentException("marginTop cannot be null.");
        this.marginTop = marginTop;
        return this;
    }

    public Container<T> marginLeft(Value marginLeft){
        if(marginLeft == null) throw new IllegalArgumentException("marginLeft cannot be null.");
        this.marginLeft = marginLeft;
        return this;
    }

    public Container<T> marginBottom(Value marginBottom){
        if(marginBottom == null) throw new IllegalArgumentException("marginBottom cannot be null.");
        this.marginBottom = marginBottom;
        return this;
    }

    public Container<T> marginRight(Value marginRight){
        if(marginRight == null) throw new IllegalArgumentException("marginRight cannot be null.");
        this.marginRight = marginRight;
        return this;
    }

    /** Sets the marginTop, marginLeft, marginBottom, and marginRight to the specified value. */
    public Container<T> margin(float margin){
        Value value = e -> (margin);
        marginTop = value;
        marginLeft = value;
        marginBottom = value;
        marginRight = value;
        return this;
    }

    public Container<T> margin(float top, float left, float bottom, float right){
        marginTop = e -> (top);
        marginLeft = e -> (left);
        marginBottom = e -> (bottom);
        marginRight = e -> (right);
        return this;
    }

    public Container<T> marginTop(float marginTop){
        this.marginTop = e -> (marginTop);
        return this;
    }

    public Container<T> marginLeft(float marginLeft){
        this.marginLeft = e -> (marginLeft);
        return this;
    }

    public Container<T> marginBottom(float marginBottom){
        this.marginBottom = e -> (marginBottom);
        return this;
    }

    public Container<T> marginRight(float marginRight){
        this.marginRight = e -> (marginRight);
        return this;
    }

    /** Sets fillX and fillY to 1. */
    public Container<T> fill(){
        fillX = 1f;
        fillY = 1f;
        return this;
    }

    /** Sets fillX to 1. */
    public Container<T> fillX(){
        fillX = 1f;
        return this;
    }

    /** Sets fillY to 1. */
    public Container<T> fillY(){
        fillY = 1f;
        return this;
    }

    public Container<T> fill(float x, float y){
        fillX = x;
        fillY = y;
        return this;
    }

    /** Sets fillX and fillY to 1 if true, 0 if false. */
    public Container<T> fill(boolean x, boolean y){
        fillX = x ? 1f : 0;
        fillY = y ? 1f : 0;
        return this;
    }

    /** Sets fillX and fillY to 1 if true, 0 if false. */
    public Container<T> fill(boolean fill){
        fillX = fill ? 1f : 0;
        fillY = fill ? 1f : 0;
        return this;
    }

    /**
     * Sets the alignment of the actor within the container. Set to {@link Align#center}, {@link Align#top}, {@link Align#bottom},
     * {@link Align#left}, {@link Align#right}, or any combination of those.
     */
    public Container<T> align(int align){
        this.align = align;
        return this;
    }

    /** Sets the alignment of the actor within the container to {@link Align#center}. This clears any other alignment. */
    public Container<T> center(){
        align = Align.center;
        return this;
    }

    /** Sets {@link Align#top} and clears {@link Align#bottom} for the alignment of the actor within the container. */
    public Container<T> top(){
        align |= Align.top;
        align &= ~Align.bottom;
        return this;
    }

    /** Sets {@link Align#left} and clears {@link Align#right} for the alignment of the actor within the container. */
    public Container<T> left(){
        align |= Align.left;
        align &= ~Align.right;
        return this;
    }

    /** Sets {@link Align#bottom} and clears {@link Align#top} for the alignment of the actor within the container. */
    public Container<T> bottom(){
        align |= Align.bottom;
        align &= ~Align.top;
        return this;
    }

    /** Sets {@link Align#right} and clears {@link Align#left} for the alignment of the actor within the container. */
    public Container<T> right(){
        align |= Align.right;
        align &= ~Align.left;
        return this;
    }

    public float getMinWidth(){
        return minWidth.get(actor) + marginLeft.get(this) + marginRight.get(this);
    }

    public Value getMinHeightValue(){
        return minHeight;
    }

    public float getMinHeight(){
        return minHeight.get(actor) + marginTop.get(this) + marginBottom.get(this);
    }

    public Value getPrefWidthValue(){
        return prefWidth;
    }

    public float getPrefWidth(){
        float v = prefWidth.get(actor);
        if(background != null) v = Math.max(v, background.getMinWidth());
        return Math.max(getMinWidth(), v + marginLeft.get(this) + marginRight.get(this));
    }

    public Value getPrefHeightValue(){
        return prefHeight;
    }

    public float getPrefHeight(){
        float v = prefHeight.get(actor);
        if(background != null) v = Math.max(v, background.getMinHeight());
        return Math.max(getMinHeight(), v + marginTop.get(this) + marginBottom.get(this));
    }

    /** @return May be null if this value is not set. */
    public Value getPadTopValue(){
        return marginTop;
    }

    public float getPadTop(){
        return marginTop.get(this);
    }

    /** @return May be null if this value is not set. */
    public Value getPadLeftValue(){
        return marginLeft;
    }

    public float getPadLeft(){
        return marginLeft.get(this);
    }

    /** @return May be null if this value is not set. */
    public Value getPadBottomValue(){
        return marginBottom;
    }

    public float getPadBottom(){
        return marginBottom.get(this);
    }

    /** @return May be null if this value is not set. */
    public Value getPadRightValue(){
        return marginRight;
    }

    public float getPadRight(){
        return marginRight.get(this);
    }

    /** Returns {@link #getPadLeft()} plus {@link #getPadRight()}. */
    public float getPadX(){
        return marginLeft.get(this) + marginRight.get(this);
    }

    /** Returns {@link #getPadTop()} plus {@link #getPadBottom()}. */
    public float getPadY(){
        return marginTop.get(this) + marginBottom.get(this);
    }

    public float getFillX(){
        return fillX;
    }

    public float getFillY(){
        return fillY;
    }

    public int getAlign(){
        return align;
    }

    /** If true (the default), positions and sizes are rounded to integers. */
    public void setRound(boolean round){
        this.round = round;
    }

    public boolean getClip(){
        return clip;
    }

    /**
     * Causes the contents to be clipped if they exceed the container bounds. Enabling clipping will set
     * {@link #setTransform(boolean)} to true.
     */
    public void setClip(boolean enabled){
        clip = enabled;
        setTransform(enabled);
        invalidate();
    }

    public Element hit(float x, float y, boolean touchable){
        if(clip){
            if(touchable && getTouchable() == Touchable.disabled) return null;
            if(x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) return null;
        }
        return super.hit(x, y, touchable);
    }
}
