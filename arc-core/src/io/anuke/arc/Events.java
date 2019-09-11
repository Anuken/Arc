package io.anuke.arc;


import io.anuke.arc.collection.Array;
import io.anuke.arc.collection.ObjectMap;
import io.anuke.arc.function.Consumer;

@SuppressWarnings("unchecked")
public class Events{
    private static ObjectMap<Object, Array<Consumer<?>>> events = new ObjectMap<>();

    public static <T> void on(Class<T> type, Consumer<T> listener){
        events.getOr(type, Array::new).add(listener);
    }

    public static void on(Object type, Runnable listener){
        events.getOr(type, Array::new).add(e -> listener.run());
    }

    public static <T> void fire(T type){
        fire(type.getClass(), type);
    }

    public static <T> void fire(Class<?> ctype, T type){
        if(events.get(type) != null) events.get(type).each(e -> ((Consumer<T>)e).accept(type));
        if(events.get(ctype) != null) events.get(ctype).each(e -> ((Consumer<T>)e).accept(type));
    }

    public static void dispose(){
        events.clear();
    }
}
