package arc.func;

/** A cons that throws seomthing. */
public interface ConsT<T, E extends Throwable>{
    void get(T t) throws E;
}
