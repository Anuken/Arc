package arc;


import arc.struct.Seq;
import arc.struct.ObjectMap;
import arc.func.Cons;

/** Simple global event listener system. */
@SuppressWarnings("unchecked")
public class Events{
    private static final ObjectMap<Object, Seq<Cons<?>>> events = new ObjectMap<>(), onceEvents = new ObjectMap<>();

    /** Handle an event by class. */
    public static <T> void on(Class<T> type, Cons<T> listener){
        events.get(type, () -> new Seq<>(Cons.class)).add(listener);
    }

    /** Handle an event by enum trigger. */
    public static void run(Object type, Runnable listener){
        events.get(type, () -> new Seq<>(Cons.class)).add(e -> listener.run());
    }

    /** Handle an event by class once. */
    public static <T> void once(Class<T> type, Cons<T> listener){
        onceEvents.get(type, () -> new Seq<>(Cons.class)).add(listener);
    }

    /** Handle an event by enum trigger once. */
    public static void runOnce(Object type, Runnable listener){
        onceEvents.get(type, () -> new Seq<>(Cons.class)).add(e -> listener.run());
    }

    /** Only use this method if you have the reference to the exact listener object that was used. */
    public static <T> boolean remove(Class<T> type, Cons<T> listener){
        return events.get(type, () -> new Seq<>(Cons.class)).remove(listener);
    }

    /** Fires an enum trigger. */
    public static <T extends Enum<T>> void fire(Enum<T> type){
        Seq<Cons<?>> listeners = events.get(type);

        if(listeners != null){
            int len = listeners.size;
            Cons[] items = listeners.items;
            for(int i = 0; i < len; i++){
                items[i].get(type);
            }
        }
    }

    /** Fires a non-enum event by class. */
    public static <T> void fire(T type){
        fire(type.getClass(), type);
    }

    public static <T> void fire(Class<?> ctype, T type){
        Seq<Cons<?>> listeners = events.get(ctype), onceListeners = onceEvents.get(ctype);

        if(listeners != null){
            int len = listeners.size;
            Cons[] items = listeners.items;
            for(int i = 0; i < len; i++){
                items[i].get(type);
            }
        }

        if(onceListeners != null){
            int len = onceListeners.size;
            Cons[] items = onceListeners.items;
            for(int i = 0; i < len; i++){
                items[i].get(type);
            }
            onceListeners.clear();
        }
    }

    /** Don't do this. */
    public static void clear(){
        events.clear();
        onceEvents.clear();
    }
}
