package arc;


import arc.struct.Seq;
import arc.struct.ObjectMap;
import arc.func.Cons;

/** Simple global event listener system. */
@SuppressWarnings("unchecked")
public class Events{
    private static final ObjectMap<Object, Seq<Cons<?>>> events = new ObjectMap<>();

    public static <T> void on(Class<T> type, Cons<T> listener){
        events.get(type, Seq::new).add(listener);
    }

    public static void run(Object type, Runnable listener){
        events.get(type, Seq::new).add(e -> listener.run());
    }

    public static <T> void remove(Class<T> type, Cons<T> listener){
        events.get(type, Seq::new).remove(listener);
    }

    public static <T> void fire(T type){
        fire(type.getClass(), type);
    }

    public static <T> void fire(Class<?> ctype, T type){
        if(events.get(type) != null) events.get(type).each(e -> ((Cons<T>)e).get(type));
        if(events.get(ctype) != null) events.get(ctype).each(e -> ((Cons<T>)e).get(type));
    }

    public static <T> void fireWrap(Class<?> ctype, T type, Cons<Cons<T>> wrapper){
        if(events.get(ctype) != null){
            events.get(ctype).each(e -> {
                wrapper.get(((Cons<T>)e));
            });
        }
    }
}
