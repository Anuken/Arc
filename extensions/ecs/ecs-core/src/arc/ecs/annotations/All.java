package arc.ecs.annotations;

import arc.ecs.*;

import java.lang.annotation.*;

/**
 * <p>Auto-configures fields or systems pertaining to aspects.
 *
 * <p>On fields, this annotation works similar to {@link Wire}; fields are configured
 * during , or explicitly via {@link Base#inject(Object)}.</p>
 *
 * <p>On BaseEntitySystem subclasses, this annotation configures the aspects for the system,
 * replacing the need to use constructor parameters.</>/p>
 * <p>
 * The annotated field must be one the following types: {@link Archetype}, {@link Aspect}, {@link Aspect.Builder},
 * {@link EntitySubscription}, {@link EntityTransmuter}.</p>
 *
 * <p>This annotation can be combined with {@link One} and {@link Exclude},
 * but will be ignored if {@link AspectDescriptor} is present.</p>
 *
 * <h4>Note on EntityTransmuters/Archetypes</h4>
 * <p>{@link #value()} corresponds to create.</p>
 * @author Ken Schosinsky
 * @author Felix Bridault
 * @see One
 * @see Exclude
 * @see AspectDescriptor
 * @see Wire
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Documented
@UnstableApi
public @interface All{

    /** @return required types */
    Class<? extends Component>[] value() default {};
}
