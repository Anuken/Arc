package io.anuke.arc;


import io.anuke.arc.collection.Array;
import io.anuke.arc.collection.ObjectMap;
import io.anuke.arc.function.Consumer;

@SuppressWarnings("unchecked")
public class Events{
    private static ObjectMap<Class<?>, Array<Consumer<?>>> events = new ObjectMap<>();

    public static <T> void on(Class<T> type, Consumer<T> listener){
        if(events.get(type) == null)
            events.put(type, new Array<>());

        events.get(type).add(listener);
    }

    public static <T> void fire(T type){
        if(events.get(type.getClass()) == null)
            return;

        for(Consumer<?> event : events.get(type.getClass())){
            ((Consumer<T>)event).accept(type);
        }
    }

    public static void dispose(){
        events.clear();
    }
}
