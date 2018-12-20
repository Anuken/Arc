package io.anuke.arc.scene.event;

/**
 * Low level interface for receiving events. Typically there is a listener class for each specific event class.
 * @author Nathan Sweet
 * @see InputListener
 * @see InputEvent
 */
public interface EventListener{
    /**
     * Try to handle the given event, if it is applicable.
     * @return true if the event should be considered {@link Event#handle() handled} by scene2d.
     */
    boolean handle(Event event);
}
