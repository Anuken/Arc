package arc;


import arc.struct.Seq;
import arc.struct.ObjectMap;
import arc.func.Cons;

@SuppressWarnings("unchecked")
public class Events{
    private static final ObjectMap<Object, Seq<Cons<?>>> events = new ObjectMap<>();

    public static <T> void on(Class<T> type, Cons<T> listener, Cons<Throwable> error){
        try{
            on(type, listener);
        }catch(Throwable t){
            error.get(t);
        }
    }

    public static <T> void on(Class<T> type, Cons<T> listener){
        events.get(type, Seq::new).add(listener);
    }

    public static void run(Object type, Runnable listener, Cons<Throwable> error){
        try{
            run(type, listener);
        }catch(Throwable t){
            error.get(t);
        }
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

    public static void dispose(){
        events.clear();
    }
}
