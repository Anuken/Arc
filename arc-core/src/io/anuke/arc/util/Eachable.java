package io.anuke.arc.util;

import io.anuke.arc.func.*;

public interface Eachable<T>{
    void each(Cons<T> cons);
}
