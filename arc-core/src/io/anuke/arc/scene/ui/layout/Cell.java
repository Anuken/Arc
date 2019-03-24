package io.anuke.arc.scene.ui.layout;

import io.anuke.arc.function.BooleanProvider;
import io.anuke.arc.function.Consumer;
import io.anuke.arc.function.Predicate;
import io.anuke.arc.function.Supplier;
import io.anuke.arc.graphics.Color;
import io.anuke.arc.scene.Element;
import io.anuke.arc.scene.event.Touchable;
import io.anuke.arc.scene.ui.*;
import io.anuke.arc.scene.ui.TextField.TextFieldValidator;
import io.anuke.arc.scene.ui.layout.Value.Fixed;
import io.anuke.arc.util.Align;
import io.anuke.arc.util.pooling.Pool.Poolable;

/**
 * A cell for a {@link Table}.
 * @author Nathan Sweet
 */
public class Cell<T extends Element> implements Poolable{
    static private Cell defaults;
    static private boolean initialized;

    Value minWidth, minHeight;
    Value maxWidth, maxHeight;
    Value spaceTop, spaceLeft, spaceBottom, spaceRight;
    Value padTop, padLeft, padBottom, padRight;
    float fillX, fillY;
    int align;
    int expandX, expandY;
    int colspan;
    boolean uniformX, uniformY;

    Element element;
    float elementX, elementY;
    float elementWidth, elementHeight;
    boolean endRow;
    int column, row;
    int cellAboveIndex;
    float computedPadTop, computedPadLeft, computedPadBottom, computedPadRight;
    private Table table;

    public Cell(){
        reset();
    }

    /**
     * Returns the defaults to use for all cells. This can be used to avoid needing to set the same defaults for every table (eg,
     * for spacing).
     */
    public static Cell defaults(){
        if(!initialized){
            initialized = true;

            defaults = new Cell();
            defaults.minWidth = Value.minWidth;
            defaults.minHeight = Value.minHeight;
            defaults.maxWidth = Value.maxWidth;
            defaults.maxHeight = Value.maxHeight;
            defaults.spaceTop = Value.zero;
            defaults.spaceLeft = Value.zero;
            defaults.spaceBottom = Value.zero;
            defaults.spaceRight = Value.zero;
            defaults.padTop = Value.zero;
            defaults.padLeft = Value.zero;
            defaults.padBottom = Value.zero;
            defaults.padRight = Value.zero;
            defaults.fillX = 0;
            defaults.fillY = 0;
            defaults.align = 0;
            defaults.expandX = 0;
            defaults.expandY = 0;
            defaults.colspan = 1;
            defaults.uniformX = false;
            defaults.uniformY = false;
        }
        return defaults;
    }

    public void setLayout(Table table){
        this.table = table;
    }

    /** Sets the element in this cell and adds the element to the cell's table. If null, removes any current element. */
    public <A extends Element> Cell<A> setElement(A newElement){
        if(element != newElement){
            if(element != null) element.remove();
            element = newElement;
            if(newElement != null) table.addChild(newElement);
        }
        return (Cell<A>)this;
    }

    /** Removes the current element for the cell, if any. */
    public Cell<T> clearElement(){
        setElement(null);
        return this;
    }

    /** Returns the element for this cell, or null. */
    public T getElement(){
        return (T)element;
    }

    /** getElement shortcut */
    public T get(){
        return getElement();
    }

    /** Returns true if the cell's element is not null. */
    public boolean hasElement(){
        return element != null;
    }

    /** Sets the minWidth, prefWidth, maxWidth, minHeight, prefHeight, and maxHeight to the specified value. */
    public Cell<T> size(Value size){
        if(size == null) throw new IllegalArgumentException("size cannot be null.");
        minWidth = size;
        minHeight = size;
        maxWidth = size;
        maxHeight = size;
        return this;
    }

    /** Sets the minWidth, prefWidth, maxWidth, minHeight, prefHeight, and maxHeight to the specified values. */
    public Cell<T> size(Value width, Value height){
        if(width == null) throw new IllegalArgumentException("width cannot be null.");
        if(height == null) throw new IllegalArgumentException("height cannot be null.");
        minWidth = width;
        minHeight = height;
        maxWidth = width;
        maxHeight = height;
        return this;
    }

    public Cell<T> name(String name){
        getElement().setName(name);
        return this;
    }

    public Cell<T> update(Consumer<T> updater){
        T t = getElement();
        t.update(() -> updater.accept(t));
        return this;
    }

    public Cell<T> disabled(Predicate<T> vis){
        if(getElement() instanceof Button){
            T t = getElement();
            ((Button)getElement()).setDisabled(() -> vis.test(t));
        }
        return this;
    }

    public Cell<T> disabled(boolean disabled){
        if(getElement() instanceof Button){
            ((Button)getElement()).setDisabled(disabled);
        }
        return this;
    }

    public Cell<T> touchable(Touchable touchable){
        getElement().touchable(touchable);
        return this;
    }

    public Cell<T> touchable(Supplier<Touchable> touchable){
        getElement().touchable(touchable);
        return this;
    }

    public Cell<T> visible(BooleanProvider prov){
        getElement().visible(prov);
        return this;
    }

    public Cell<T> visible(boolean visible){
        getElement().visible(visible);
        return this;
    }

    public Cell<T> valid(TextFieldValidator val){
        if(getElement() instanceof TextField){
            ((TextField)getElement()).setValidator(val);
        }
        return this;
    }

    public Cell<T> wrap(){
        if(getElement() instanceof Label){
            ((Label)getElement()).setWrap(true);
        }else if(getElement() instanceof TextButton){
            ((TextButton)getElement()).getLabel().setWrap(true);
        }
        return this;
    }

    public Cell<T> labelAlign(int label, int line){
        if(getElement() instanceof Label){
            ((Label)getElement()).setAlignment(label, line);
        }
        return this;
    }

    public <N extends Button> Cell<T> group(ButtonGroup<N> group){
        if(getElement() instanceof Button){
            group.add((N)getElement());
        }
        return this;
    }

    public Cell<T> checked(boolean toggle){
        if(getElement() instanceof Button){
            ((Button)(getElement())).setChecked(toggle);
        }
        return this;
    }

    public Cell<T> checked(Predicate<T> toggle){
        T t = getElement();
        if(t instanceof Button){
            t.update(() -> ((Button)t).setChecked(toggle.test(t)));
        }
        return this;
    }

    public Cell<T> fontScale(float scale){
        if(element instanceof Label){
            ((Label)element).setFontScale(scale);
        }
        return this;
    }

    public Cell<T> color(Color color){
        getElement().setColor(color);
        return this;
    }

    public Cell<T> margin(float margin){
        if(getElement() instanceof Table){
            ((Table)getElement()).margin(margin);
        }
        return this;
    }

    public Cell<T> marginTop(float margin){
        if(getElement() instanceof Table){
            ((Table)getElement()).marginTop(margin);
        }
        return this;
    }

    public Cell<T> marginBottom(float margin){
        if(getElement() instanceof Table){
            ((Table)getElement()).marginBottom(margin);
        }
        return this;
    }

    public Cell<T> marginLeft(float margin){
        if(getElement() instanceof Table){
            ((Table)getElement()).marginLeft(margin);
        }
        return this;
    }

    public Cell<T> marginRight(float margin){
        if(getElement() instanceof Table){
            ((Table)getElement()).marginRight(margin);
        }
        return this;
    }

    /** Sets the minWidth, prefWidth, maxWidth, minHeight, prefHeight, and maxHeight to the specified value. */
    public Cell<T> size(float size){
        size(new Fixed(size));
        return this;
    }

    /** Sets the minWidth, prefWidth, maxWidth, minHeight, prefHeight, and maxHeight to the specified values. */
    public Cell<T> size(float width, float height){
        size(new Fixed(width), new Fixed(height));
        return this;
    }

    /** Sets the minWidth, prefWidth, and maxWidth to the specified value. */
    public Cell<T> width(Value width){
        if(width == null) throw new IllegalArgumentException("width cannot be null.");
        minWidth = width;
        maxWidth = width;
        return this;
    }

    /** Sets the minWidth, prefWidth, and maxWidth to the specified value. */
    public Cell<T> width(float width){
        width(new Fixed(width));
        return this;
    }

    /** Sets the minHeight, prefHeight, and maxHeight to the specified value. */
    public Cell<T> height(Value height){
        if(height == null) throw new IllegalArgumentException("height cannot be null.");
        minHeight = height;
        maxHeight = height;
        return this;
    }

    /** Sets the minHeight, prefHeight, and maxHeight to the specified value. */
    public Cell<T> height(float height){
        height(new Fixed(height));
        return this;
    }

    /** Sets the minWidth and minHeight to the specified value. */
    public Cell<T> minSize(Value size){
        if(size == null) throw new IllegalArgumentException("size cannot be null.");
        minWidth = size;
        minHeight = size;
        return this;
    }

    /** Sets the minWidth and minHeight to the specified values. */
    public Cell<T> minSize(Value width, Value height){
        if(width == null) throw new IllegalArgumentException("width cannot be null.");
        if(height == null) throw new IllegalArgumentException("height cannot be null.");
        minWidth = width;
        minHeight = height;
        return this;
    }

    public Cell<T> minWidth(Value minWidth){
        if(minWidth == null) throw new IllegalArgumentException("minWidth cannot be null.");
        this.minWidth = minWidth;
        return this;
    }

    public Cell<T> minHeight(Value minHeight){
        if(minHeight == null) throw new IllegalArgumentException("minHeight cannot be null.");
        this.minHeight = minHeight;
        return this;
    }

    /** Sets the minWidth and minHeight to the specified value. */
    public Cell<T> minSize(float size){
        minSize(new Fixed(size));
        return this;
    }

    /** Sets the minWidth and minHeight to the specified values. */
    public Cell<T> minSize(float width, float height){
        minSize(new Fixed(width), new Fixed(height));
        return this;
    }

    public Cell<T> minWidth(float minWidth){
        this.minWidth = new Fixed(minWidth);
        return this;
    }

    public Cell<T> minHeight(float minHeight){
        this.minHeight = new Fixed(minHeight);
        return this;
    }

    /** Sets the prefWidth and prefHeight to the specified value. */
    public Cell<T> prefSize(Value size){
        if(size == null) throw new IllegalArgumentException("size cannot be null.");
        return this;
    }

    /** Sets the prefWidth and prefHeight to the specified values. */
    public Cell<T> prefSize(Value width, Value height){
        if(width == null) throw new IllegalArgumentException("width cannot be null.");
        if(height == null) throw new IllegalArgumentException("height cannot be null.");
        return this;
    }

    public Cell<T> prefWidth(Value prefWidth){
        if(prefWidth == null) throw new IllegalArgumentException("prefWidth cannot be null.");
        return this;
    }

    public Cell<T> prefHeight(Value prefHeight){
        if(prefHeight == null) throw new IllegalArgumentException("prefHeight cannot be null.");
        return this;
    }

    /** Sets the prefWidth and prefHeight to the specified value. */
    public Cell<T> prefSize(float width, float height){
        prefSize(new Fixed(width), new Fixed(height));
        return this;
    }

    /** Sets the prefWidth and prefHeight to the specified values. */
    public Cell<T> prefSize(float size){
        prefSize(new Fixed(size));
        return this;
    }

    public Cell<T> prefWidth(float prefWidth){
        return this;
    }

    public Cell<T> prefHeight(float prefHeight){
        return this;
    }

    /** Sets the maxWidth and maxHeight to the specified value. */
    public Cell<T> maxSize(Value size){
        if(size == null) throw new IllegalArgumentException("size cannot be null.");
        maxWidth = size;
        maxHeight = size;
        return this;
    }

    /** Sets the maxWidth and maxHeight to the specified values. */
    public Cell<T> maxSize(Value width, Value height){
        if(width == null) throw new IllegalArgumentException("width cannot be null.");
        if(height == null) throw new IllegalArgumentException("height cannot be null.");
        maxWidth = width;
        maxHeight = height;
        return this;
    }

    public Cell<T> maxWidth(Value maxWidth){
        if(maxWidth == null) throw new IllegalArgumentException("maxWidth cannot be null.");
        this.maxWidth = maxWidth;
        return this;
    }

    public Cell<T> maxHeight(Value maxHeight){
        if(maxHeight == null) throw new IllegalArgumentException("maxHeight cannot be null.");
        this.maxHeight = maxHeight;
        return this;
    }

    /** Sets the maxWidth and maxHeight to the specified value. */
    public Cell<T> maxSize(float size){
        maxSize(new Fixed(size));
        return this;
    }

    /** Sets the maxWidth and maxHeight to the specified values. */
    public Cell<T> maxSize(float width, float height){
        maxSize(new Fixed(width), new Fixed(height));
        return this;
    }

    public Cell<T> maxWidth(float maxWidth){
        this.maxWidth = new Fixed(maxWidth);
        return this;
    }

    public Cell<T> maxHeight(float maxHeight){
        this.maxHeight = new Fixed(maxHeight);
        return this;
    }

    /** Sets the spaceTop, spaceLeft, spaceBottom, and spaceRight to the specified value. */
    public Cell<T> space(Value space){
        if(space == null) throw new IllegalArgumentException("space cannot be null.");
        spaceTop = space;
        spaceLeft = space;
        spaceBottom = space;
        spaceRight = space;
        return this;
    }

    public Cell<T> space(Value top, Value left, Value bottom, Value right){
        if(top == null) throw new IllegalArgumentException("top cannot be null.");
        if(left == null) throw new IllegalArgumentException("left cannot be null.");
        if(bottom == null) throw new IllegalArgumentException("bottom cannot be null.");
        if(right == null) throw new IllegalArgumentException("right cannot be null.");
        spaceTop = top;
        spaceLeft = left;
        spaceBottom = bottom;
        spaceRight = right;
        return this;
    }

    public Cell<T> spaceTop(Value spaceTop){
        if(spaceTop == null) throw new IllegalArgumentException("spaceTop cannot be null.");
        this.spaceTop = spaceTop;
        return this;
    }

    public Cell<T> spaceLeft(Value spaceLeft){
        if(spaceLeft == null) throw new IllegalArgumentException("spaceLeft cannot be null.");
        this.spaceLeft = spaceLeft;
        return this;
    }

    public Cell<T> spaceBottom(Value spaceBottom){
        if(spaceBottom == null) throw new IllegalArgumentException("spaceBottom cannot be null.");
        this.spaceBottom = spaceBottom;
        return this;
    }

    public Cell<T> spaceRight(Value spaceRight){
        if(spaceRight == null) throw new IllegalArgumentException("spaceRight cannot be null.");
        this.spaceRight = spaceRight;
        return this;
    }

    /** Sets the spaceTop, spaceLeft, spaceBottom, and spaceRight to the specified value. */
    public Cell<T> space(float space){
        if(space < 0) throw new IllegalArgumentException("space cannot be < 0.");
        space(new Fixed(space));
        return this;
    }

    public Cell<T> space(float top, float left, float bottom, float right){
        if(top < 0) throw new IllegalArgumentException("top cannot be < 0.");
        if(left < 0) throw new IllegalArgumentException("left cannot be < 0.");
        if(bottom < 0) throw new IllegalArgumentException("bottom cannot be < 0.");
        if(right < 0) throw new IllegalArgumentException("right cannot be < 0.");
        space(new Fixed(top), new Fixed(left), new Fixed(bottom), new Fixed(right));
        return this;
    }

    public Cell<T> spaceTop(float spaceTop){
        if(spaceTop < 0) throw new IllegalArgumentException("spaceTop cannot be < 0.");
        this.spaceTop = new Fixed(spaceTop);
        return this;
    }

    public Cell<T> spaceLeft(float spaceLeft){
        if(spaceLeft < 0) throw new IllegalArgumentException("spaceLeft cannot be < 0.");
        this.spaceLeft = new Fixed(spaceLeft);
        return this;
    }

    public Cell<T> spaceBottom(float spaceBottom){
        if(spaceBottom < 0) throw new IllegalArgumentException("spaceBottom cannot be < 0.");
        this.spaceBottom = new Fixed(spaceBottom);
        return this;
    }

    public Cell<T> spaceRight(float spaceRight){
        if(spaceRight < 0) throw new IllegalArgumentException("spaceRight cannot be < 0.");
        this.spaceRight = new Fixed(spaceRight);
        return this;
    }

    /** Sets the marginTop, marginLeft, marginBottom, and marginRight to the specified value. */
    public Cell<T> pad(Value pad){
        if(pad == null) throw new IllegalArgumentException("margin cannot be null.");
        padTop = pad;
        padLeft = pad;
        padBottom = pad;
        padRight = pad;
        return this;
    }

    public Cell<T> pad(Value top, Value left, Value bottom, Value right){
        if(top == null) throw new IllegalArgumentException("top cannot be null.");
        if(left == null) throw new IllegalArgumentException("left cannot be null.");
        if(bottom == null) throw new IllegalArgumentException("bottom cannot be null.");
        if(right == null) throw new IllegalArgumentException("right cannot be null.");
        padTop = top;
        padLeft = left;
        padBottom = bottom;
        padRight = right;
        return this;
    }

    public Cell<T> padTop(Value padTop){
        if(padTop == null) throw new IllegalArgumentException("marginTop cannot be null.");
        this.padTop = padTop;
        return this;
    }

    public Cell<T> padLeft(Value padLeft){
        if(padLeft == null) throw new IllegalArgumentException("marginLeft cannot be null.");
        this.padLeft = padLeft;
        return this;
    }

    public Cell<T> padBottom(Value padBottom){
        if(padBottom == null) throw new IllegalArgumentException("marginBottom cannot be null.");
        this.padBottom = padBottom;
        return this;
    }

    public Cell<T> padRight(Value padRight){
        if(padRight == null) throw new IllegalArgumentException("marginRight cannot be null.");
        this.padRight = padRight;
        return this;
    }

    /** Sets the marginTop, marginLeft, marginBottom, and marginRight to the specified value. */
    public Cell<T> pad(float pad){
        pad(new Fixed(pad));
        return this;
    }

    public Cell<T> pad(float top, float left, float bottom, float right){
        pad(new Fixed(top), new Fixed(left), new Fixed(bottom), new Fixed(right));
        return this;
    }

    public Cell<T> padTop(float padTop){
        this.padTop = new Fixed(padTop);
        return this;
    }

    public Cell<T> padLeft(float padLeft){
        this.padLeft = new Fixed(padLeft);
        return this;
    }

    public Cell<T> padBottom(float padBottom){
        this.padBottom = new Fixed(padBottom);
        return this;
    }

    public Cell<T> padRight(float padRight){
        this.padRight = new Fixed(padRight);
        return this;
    }

    /** Sets fillX and fillY to 1. */
    public Cell<T> fill(){
        fillX = 1;
        fillY = 1;
        return this;
    }

    /** Sets fillX to 1. */
    public Cell<T> fillX(){
        fillX = 1;
        return this;
    }

    /** Sets fillY to 1. */
    public Cell<T> fillY(){
        fillY = 1;
        return this;
    }

    public Cell<T> fill(float x, float y){
        fillX = x;
        fillY = y;
        return this;
    }

    /** Sets fillX and fillY to 1 if true, 0 if false. */
    public Cell<T> fill(boolean x, boolean y){
        fillX = x ? 1 : 0;
        fillY = y ? 1 : 0;
        return this;
    }

    /** Sets fillX and fillY to 1 if true, 0 if false. */
    public Cell<T> fill(boolean fill){
        fillX = fill ? 1 : 0;
        fillY = fill ? 1 : 0;
        return this;
    }

    /**
     * Sets the alignment of the element within the cell. Set to {@link Align#center}, {@link Align#top}, {@link Align#bottom},
     * {@link Align#left}, {@link Align#right}, or any combination of those.
     */
    public Cell<T> align(int align){
        this.align = align;
        return this;
    }

    /** Sets the alignment of the element within the cell to {@link Align#center}. This clears any other alignment. */
    public Cell<T> center(){
        align = Align.center;
        return this;
    }

    /** Adds {@link Align#top} and clears {@link Align#bottom} for the alignment of the element within the cell. */
    public Cell<T> top(){
        align = (align | Align.top) & ~Align.bottom;
        return this;
    }

    /** Adds {@link Align#left} and clears {@link Align#right} for the alignment of the element within the cell. */
    public Cell<T> left(){
        align = (align | Align.left) & ~Align.right;
        return this;
    }

    /** Adds {@link Align#bottom} and clears {@link Align#top} for the alignment of the element within the cell. */
    public Cell<T> bottom(){
        align = (align | Align.bottom) & ~Align.top;
        return this;
    }

    /** Adds {@link Align#right} and clears {@link Align#left} for the alignment of the element within the cell. */
    public Cell<T> right(){
        align = (align | Align.right) & ~Align.left;
        return this;
    }

    /** Sets expandX, expandY, fillX, and fillY to 1. */
    public Cell<T> grow(){
        expandX = 1;
        expandY = 1;
        fillX = 1;
        fillY = 1;
        return this;
    }

    /** Sets expandX and fillX to 1. */
    public Cell<T> growX(){
        expandX = 1;
        fillX = 1;
        return this;
    }

    /** Sets expandY and fillY to 1. */
    public Cell<T> growY(){
        expandY = 1;
        fillY = 1;
        return this;
    }

    /** Sets expandX and expandY to 1. */
    public Cell<T> expand(){
        expandX = 1;
        expandY = 1;
        return this;
    }

    /** Sets expandX to 1. */
    public Cell<T> expandX(){
        expandX = 1;
        return this;
    }

    /** Sets expandY to 1. */
    public Cell<T> expandY(){
        expandY = 1;
        return this;
    }

    public Cell<T> expand(int x, int y){
        expandX = x;
        expandY = y;
        return this;
    }

    /** Sets expandX and expandY to 1 if true, 0 if false. */
    public Cell<T> expand(boolean x, boolean y){
        expandX = x ? 1 : 0;
        expandY = y ? 1 : 0;
        return this;
    }

    public Cell<T> colspan(int colspan){
        this.colspan = colspan;
        return this;
    }

    /** Sets uniformX and uniformY to true. */
    public Cell<T> uniform(){
        uniformX = Boolean.TRUE;
        uniformY = Boolean.TRUE;
        return this;
    }

    /** Sets uniformX to true. */
    public Cell<T> uniformX(){
        uniformX = Boolean.TRUE;
        return this;
    }

    /** Sets uniformY to true. */
    public Cell<T> uniformY(){
        uniformY = Boolean.TRUE;
        return this;
    }

    public Cell<T> uniform(boolean x, boolean y){
        uniformX = x;
        uniformY = y;
        return this;
    }

    public void setBounds(float x, float y, float width, float height){
        elementX = x;
        elementY = y;
        elementWidth = width;
        elementHeight = height;
    }

    public int getColumn(){
        return column;
    }

    public int getRow(){
        return row;
    }

    /** @return May be null if this cell is row defaults. */
    public Value getMinWidthValue(){
        return minWidth;
    }

    public float getMinWidth(){
        return minWidth.get(element);
    }

    /** @return May be null if this cell is row defaults. */
    public Value getMinHeightValue(){
        return minHeight;
    }

    public float getMinHeight(){
        return minHeight.get(element);
    }

    /** @return May be null if this cell is row defaults. */
    public Value getMaxWidthValue(){
        return maxWidth;
    }

    public float getMaxWidth(){
        return maxWidth.get(element);
    }

    /** @return May be null if this cell is row defaults. */
    public Value getMaxHeightValue(){
        return maxHeight;
    }

    public float getMaxHeight(){
        return maxHeight.get(element);
    }

    /** @return May be null if this value is not set. */
    public Value getSpaceTopValue(){
        return spaceTop;
    }

    public float getSpaceTop(){
        return spaceTop.get(element);
    }

    /** @return May be null if this value is not set. */
    public Value getSpaceLeftValue(){
        return spaceLeft;
    }

    public float getSpaceLeft(){
        return spaceLeft.get(element);
    }

    /** @return May be null if this value is not set. */
    public Value getSpaceBottomValue(){
        return spaceBottom;
    }

    public float getSpaceBottom(){
        return spaceBottom.get(element);
    }

    /** @return May be null if this value is not set. */
    public Value getSpaceRightValue(){
        return spaceRight;
    }

    public float getSpaceRight(){
        return spaceRight.get(element);
    }

    /** @return May be null if this value is not set. */
    public Value getPadTopValue(){
        return padTop;
    }

    public float getPadTop(){
        return padTop.get(element);
    }

    /** @return May be null if this value is not set. */
    public Value getPadLeftValue(){
        return padLeft;
    }

    public float getPadLeft(){
        return padLeft.get(element);
    }

    /** @return May be null if this value is not set. */
    public Value getPadBottomValue(){
        return padBottom;
    }

    public float getPadBottom(){
        return padBottom.get(element);
    }

    /** @return May be null if this value is not set. */
    public Value getPadRightValue(){
        return padRight;
    }

    public float getPadRight(){
        return padRight.get(element);
    }

    /** Returns {@link #getPadLeft()} plus {@link #getPadRight()}. */
    public float getPadX(){
        return padLeft.get(element) + padRight.get(element);
    }

    /** Returns {@link #getPadTop()} plus {@link #getPadBottom()}. */
    public float getPadY(){
        return padTop.get(element) + padBottom.get(element);
    }

    /** @return May be null if this value is not set. */
    public float getFillX(){
        return fillX;
    }

    /** @return May be null. */
    public float getFillY(){
        return fillY;
    }

    /** @return May be null. */
    public int getAlign(){
        return align;
    }

    /** @return May be null. */
    public int getExpandX(){
        return expandX;
    }

    /** @return May be null. */
    public int getExpandY(){
        return expandY;
    }

    /** @return May be null. */
    public int getColspan(){
        return colspan;
    }

    /** @return May be null. */
    public boolean getUniformX(){
        return uniformX;
    }

    /** @return May be null. */
    public boolean getUniformY(){
        return uniformY;
    }

    /** Returns true if this cell is the last cell in the row. */
    public boolean isEndRow(){
        return endRow;
    }

    /** The actual amount of combined padding and spacing from the last layout. */
    public float getComputedPadTop(){
        return computedPadTop;
    }

    /** The actual amount of combined padding and spacing from the last layout. */
    public float getComputedPadLeft(){
        return computedPadLeft;
    }

    /** The actual amount of combined padding and spacing from the last layout. */
    public float getComputedPadBottom(){
        return computedPadBottom;
    }

    /** The actual amount of combined padding and spacing from the last layout. */
    public float getComputedPadRight(){
        return computedPadRight;
    }

    public void row(){
        table.row();
    }

    public Table getTable(){
        return table;
    }

    /** Sets all constraint fields to null. */
    void clear(){
        minWidth = null;
        minHeight = null;
        maxWidth = null;
        maxHeight = null;
        spaceTop = null;
        spaceLeft = null;
        spaceBottom = null;
        spaceRight = null;
        padTop = null;
        padLeft = null;
        padBottom = null;
        padRight = null;
        fillX = 0;
        fillY = 0;
        align = 0;
        expandX = 0;
        expandY = 0;
        colspan = 1;
        uniformX = false;
        uniformY = false;
    }

    /** Reset state so the cell can be reused, setting all constraints to their {@link #defaults() default} values. */
    public void reset(){
        element = null;
        table = null;
        endRow = false;
        cellAboveIndex = -1;

        Cell defaults = defaults();
        if(defaults != null) set(defaults);
    }

    public Cell<T> set(Cell cell){
        minWidth = cell.minWidth;
        minHeight = cell.minHeight;
        maxWidth = cell.maxWidth;
        maxHeight = cell.maxHeight;
        spaceTop = cell.spaceTop;
        spaceLeft = cell.spaceLeft;
        spaceBottom = cell.spaceBottom;
        spaceRight = cell.spaceRight;
        padTop = cell.padTop;
        padLeft = cell.padLeft;
        padBottom = cell.padBottom;
        padRight = cell.padRight;
        fillX = cell.fillX;
        fillY = cell.fillY;
        align = cell.align;
        expandX = cell.expandX;
        expandY = cell.expandY;
        colspan = cell.colspan;
        uniformX = cell.uniformX;
        uniformY = cell.uniformY;
        return this;
    }

    /** @param cell May be null. */
    void merge(Cell cell){
        if(cell == null) return;
        if(cell.minWidth != null) minWidth = cell.minWidth;
        if(cell.minHeight != null) minHeight = cell.minHeight;
        if(cell.maxWidth != null) maxWidth = cell.maxWidth;
        if(cell.maxHeight != null) maxHeight = cell.maxHeight;
        if(cell.spaceTop != null) spaceTop = cell.spaceTop;
        if(cell.spaceLeft != null) spaceLeft = cell.spaceLeft;
        if(cell.spaceBottom != null) spaceBottom = cell.spaceBottom;
        if(cell.spaceRight != null) spaceRight = cell.spaceRight;
        if(cell.padTop != null) padTop = cell.padTop;
        if(cell.padLeft != null) padLeft = cell.padLeft;
        if(cell.padBottom != null) padBottom = cell.padBottom;
        if(cell.padRight != null) padRight = cell.padRight;
        fillX = cell.fillX;
        fillY = cell.fillY;
        if(cell.align != 0) align = cell.align;
        expandX = cell.expandX;
        expandY = cell.expandY;
        colspan = cell.colspan;
        uniformX = cell.uniformX;
        uniformY = cell.uniformY;
    }

    public String toString(){
        return element != null ? element.toString() : super.toString();
    }
}
