package arc.ecs.annotations;

import java.lang.annotation.*;

/** Indicates that an EntitySystem subclass should automatically have methods for mappers injected.
 * TODO implement */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface Automap{
}
