package arc.ecs.annotations;

import arc.ecs.*;

import java.lang.annotation.*;

/**
 * <p>Auto-configures fields or systems pertaining to aspects. The annotated field
 * must be one the following types: {@link Aspect}, {@link Aspect.Builder},
 * {@link EntitySubscription}.</p>
 *
 * <p>On BaseEntitySystem subclasses, this annotation configures the aspects for the system,
 * replacing the need to use constructor parameters.</>/p>
 *
 * <p>This annotation can be combined with {@link All} and {@link Exclude},
 * but will be ignored if {@link AspectDescriptor} is present.</p>
 *
 * <p>This annotation works similar to {@link Wire}; fields are configured
 * during , or explicitly via {@link Base#inject(Object)}.</p>
 * @author Felix Bridault
 * @author Ken Schosinsky
 * @see All
 * @see Exclude
 * @see AspectDescriptor
 * @see Wire
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Documented
@UnstableApi
public @interface One{

    /**
     * @return match at least one
     */
    Class<? extends Component>[] value() default {};

}
