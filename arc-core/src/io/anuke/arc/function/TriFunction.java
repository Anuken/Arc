package io.anuke.arc.function;

public interface TriFunction<P1, P2, P3, R>{
    R get(P1 param1, P2 param2, P3 param3);
}
