package io.anuke.arc.typelabel.reg;

import java.lang.annotation.*;

/**
 * An annotation for the GWT compiler.
 *
 * @author smelC from the SquidLib project
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD})
public @interface GwtIncompatible{

}
