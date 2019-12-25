package arc.func;

public interface Cons2<T, N>{
    void get(T t, N n);

    default Cons2<T, N> with(Cons2<T, N> cons){
        return (t, n) -> {
            get(t, n);
            cons.get(t, n);
        };
    }
}
