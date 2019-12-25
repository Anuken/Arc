package arc.backend.teavm.plugin;

import java.lang.annotation.*;

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
