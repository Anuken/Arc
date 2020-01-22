package arc.ecs.annotations;

import java.lang.annotation.*;


/**
 * Skip reflective dependency injection on annotated field or class.
 * <p>
 * Allows excluding specific fields or classes in {@link Wire}d hierarchy.
 * @author Daan van Yperen
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
@Documented
public @interface SkipWire{
}
