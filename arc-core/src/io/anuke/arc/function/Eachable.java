package io.anuke.arc.function;

public interface Eachable<T>{
    void each(Consumer<T> cons);
}
