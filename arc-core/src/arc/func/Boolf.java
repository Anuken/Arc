package arc.func;

public interface Boolf<T>{
    boolean get(T t);

    default Boolf<T> and(Boolf<T> pred){
        return t -> get(t) && pred.get(t);
    }

    default Boolf<T> or(Boolf<T> pred){
        return t -> get(t) || pred.get(t);
    }
}
