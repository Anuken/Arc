package arc;


import arc.struct.Array;
import arc.struct.ObjectMap;
import arc.func.Cons;

@SuppressWarnings("unchecked")
public class Events{
    private static ObjectMap<Object, Array<Cons<?>>> events = new ObjectMap<>();

    public static <T> void on(Class<T> type, Cons<T> listener){
        events.getOr(type, Array::new).add(listener);
    }

    public static void on(Object type, Runnable listener){
        events.getOr(type, Array::new).add(e -> listener.run());
    }

    public static <T> void fire(T type){
        fire(type.getClass(), type);
    }

    public static <T> void fire(Class<?> ctype, T type){
        if(events.get(type) != null) events.get(type).each(e -> ((Cons<T>)e).get(type));
        if(events.get(ctype) != null) events.get(ctype).each(e -> ((Cons<T>)e).get(type));
    }

    public static void dispose(){
        events.clear();
    }
}
