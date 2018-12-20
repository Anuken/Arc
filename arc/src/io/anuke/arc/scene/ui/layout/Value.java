package io.anuke.arc.scene.ui.layout;

import io.anuke.arc.scene.Element;

/**
 * Value placeholder, allowing the value to be computed on request. Values are provided an actor for context which reduces the
 * number of value instances that need to be created and reduces verbosity in code that specifies values.
 * @author Nathan Sweet
 */
public interface Value{
    /** A value that is always zero. */
    Value zero = e -> 0;
    /** Value that is the minWidth of the actor in the cell. */
    Value minWidth = context -> context == null ? 0 : context.getMinWidth();
    /** Value that is the minHeight of the actor in the cell. */
    Value minHeight = context -> context == null ? 0 : context.getMinHeight();
    /** Value that is the prefWidth of the actor in the cell. */
    Value prefWidth = context -> context == null ? 0 : context.getPrefWidth();
    /** Value that is the prefHeight of the actor in the cell. */
    Value prefHeight = context -> context == null ? 0 : context.getPrefHeight();
    /** Value that is the maxWidth of the actor in the cell. */
    Value maxWidth = context -> context == null ? 0 : context.getMaxWidth();
    /** Value that is the maxHeight of the actor in the cell. */
    Value maxHeight = context -> context == null ? 0 : context.getMaxHeight();

    /** Returns a value that is a percentage of the actor's width. */
    static Value percentWidth(final float percent){
        return actor -> actor.getWidth() * percent;
    }

    /** Returns a value that is a percentage of the actor's height. */
    static Value percentHeight(final float percent){
        return actor -> actor.getHeight() * percent;
    }

    /** Returns a value that is a percentage of the specified actor's width. The context actor is ignored. */
    static Value percentWidth(final float percent, final Element actor){
        if(actor == null) throw new IllegalArgumentException("actor cannot be null.");
        return context -> actor.getWidth() * percent;
    }

    /** Returns a value that is a percentage of the specified actor's height. The context actor is ignored. */
    static Value percentHeight(final float percent, final Element actor){
        if(actor == null) throw new IllegalArgumentException("actor cannot be null.");
        return context -> actor.getHeight() * percent;
    }

    /** @param context May be null. */
    float get(Element context);
}
