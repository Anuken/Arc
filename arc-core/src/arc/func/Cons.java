package arc.func;

public interface Cons<T>{
    void get(T t);

    default Cons<T> with(Cons<T> cons){
        return t -> {
            get(t);
            cons.get(t);
        };
    }
}
