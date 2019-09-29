package io.anuke.arc.util;

import java.lang.annotation.*;

public class ArcAnnotate{
    /** Indicates that a method return or field can be null.*/
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Nullable{

    }

    /** Indicates that a method return or field cannot be null.*/
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface NonNull{

    }
}
