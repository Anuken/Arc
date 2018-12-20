package io.anuke.arc;


import io.anuke.arc.collection.Array;
import io.anuke.arc.collection.ObjectMap;
import io.anuke.arc.function.Consumer;

@SuppressWarnings("unchecked")
public class Events{
    private static ObjectMap<Class<? extends Event>, Array<Consumer<? extends Event>>> events = new ObjectMap<>();

    public static <T extends Event> void on(Class<T> type, Consumer<T> listener){
        if(events.get(type) == null)
            events.put(type, new Array<>());

        events.get(type).add(listener);
    }

    public static <T extends Event> void fire(T type){
        if(events.get(type.getClass()) == null)
            return;

        for(Consumer<? extends Event> event : events.get(type.getClass())){
            ((Consumer<T>)event).accept(type);
        }
    }

    public interface Event{

    }
}
