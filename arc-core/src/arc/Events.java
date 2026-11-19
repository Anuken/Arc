package arc;


import arc.struct.Seq;
import arc.struct.ObjectMap;
import arc.func.Cons;
import arc.struct.SnapshotSeq;
import arc.util.Priority;

import java.util.Comparator;

/** Simple global event listener system. */
@SuppressWarnings("unchecked")
public class Events{
    private static final ObjectMap<Object, SnapshotSeq<Cons<?>>> events = new ObjectMap<>();
    private static final Comparator<Cons<?>> comparator = (a, b) -> {
        Priority priorityA = a instanceof ConsWithPriority ? ((ConsWithPriority<?>)a).priority : Priority.normal;
        Priority priorityB = b instanceof ConsWithPriority ? ((ConsWithPriority<?>)b).priority : Priority.normal;
        return priorityA.compareTo(priorityB);
    };

    /** Handle an event by class. */
    public static <T> void on(Class<T> type, Cons<T> listener){
        on(type, Priority.normal, listener);
    }

    /** Handle an event by class with the specified priority. */
    public static <T> void on(Class<T> type, Priority priority, Cons<T> listener){
        events.get(type, () -> new SnapshotSeq<>(Cons.class))
                .add(priority == Priority.normal ? listener : new ConsWithPriority<>(listener, priority))
                .sort(comparator);
    }

    /** Handle an event by enum trigger. */
    public static void run(Object type, Runnable listener){
        run(type, Priority.normal, listener);
    }

    /** Handle an event by enum trigger with the specified priority. */
    public static void run(Object type, Priority priority, Runnable listener){
        events.get(type, () -> new SnapshotSeq<>(Cons.class))
                .add(priority == Priority.normal
                        ? new ConsWithRunnable<>(listener)
                        : new ConsWithPriority<>(new ConsWithRunnable<>(listener), priority))
                .sort(comparator);
    }

    /** Removes the event listener from the specified event type. */
    public static <T> boolean remove(Class<T> type, Cons<T> listener){
        Seq<Cons<?>> listeners = events.get(type);
        if(listeners == null){
            return false;
        }
        return listeners.remove(l -> {
            if(l instanceof ConsWithPriority<?>){
                return ((ConsWithPriority<?>)l).cons.equals(listener);
            }else{
                return l.equals(listener);
            }
        });
    }

    /** Removes the event listener from the specified event type. */
    public static <T> boolean remove(T type, Runnable listener){
        Seq<Cons<?>> listeners = events.get(type);
        if(listeners == null){
            return false;
        }
        return listeners.remove(l -> {
            if(l instanceof ConsWithPriority<?>){
                l = ((ConsWithPriority<?>)l).cons;
            }
            if(l instanceof ConsWithRunnable<?>){
                return ((ConsWithRunnable<?>)l).runnable.equals(listener);
            }
            return false;
        });
    }

    /** Fires an enum trigger. */
    public static <T extends Enum<T>> void fire(Enum<T> type){
        SnapshotSeq<Cons<?>> listeners = events.get(type);
        if(listeners == null){
            return;
        }
        try{
            int len = listeners.size;
            Cons[] items = listeners.begin();
            for(int i = 0; i < len; i++){
                items[i].get(type);
            }
        }finally{
            listeners.end();
        }
    }

    /** Fires a non-enum event by class. */
    public static <T> void fire(T type){
        fire(type.getClass(), type);
    }

    public static <T> void fire(Class<?> ctype, T type){
        SnapshotSeq<Cons<?>> listeners = events.get(ctype);
        if(listeners == null){
            return;
        }
        try{
            int len = listeners.size;
            Cons[] items = listeners.begin();
            for(int i = 0; i < len; i++){
                items[i].get(type);
            }
        }finally{
            listeners.end();
        }
    }

    /** Don't do this. */
    public static void clear(){
        events.clear();
    }

    private static final class ConsWithPriority<T> implements Cons<T>{
        private final Cons<T> cons;
        private final Priority priority;

        private ConsWithPriority(Cons<T> cons, Priority priority){
            this.cons = cons;
            this.priority = priority;
        }

        @Override
        public void get(T t){
            this.cons.get(t);
        }
    }

    private static final class ConsWithRunnable<T> implements Cons<T>{
        private final Runnable runnable;

        private ConsWithRunnable(Runnable runnable){
            this.runnable = runnable;
        }

        @Override
        public void get(T t){
            this.runnable.run();
        }
    }
}
