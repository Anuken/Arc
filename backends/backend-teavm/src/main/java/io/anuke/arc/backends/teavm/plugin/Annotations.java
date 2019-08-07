package io.anuke.arc.backends.teavm.plugin;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Annotations{

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Emulate{
        Class<?> value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Replace{
        Class<?> value();
    }
}
