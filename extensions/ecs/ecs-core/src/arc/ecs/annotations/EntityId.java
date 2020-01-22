package arc.ecs.annotations;

import arc.ecs.*;
import arc.ecs.link.*;
import arc.ecs.utils.*;

import java.lang.annotation.*;

/**
 * <p>Marks <code>int</code> and {@link IntBag} fields as holding entity id:s.
 * Only works on component types. This annotation ensures that:
 * <ul>
 * <li>Entity references can be safely serialized</li>
 * <li>Tracks inter-entity relationships, if the {@link EntityLinkManager}
 * is registed with the world.</li>
 * </ul>
 * <p>
 * Only supports public fields. Kotlin requires fields with this annotation to also be annotated with {@code @JvmField}.
 *
 * <p>Annotation has no effect on {@link Bag}-of-entities and plain {@link Entity}
 * fields.</p>
 * @see <a href="https://github.com/junkdog/artemis-odb/wiki/Entity-References-and-Serialization">Entity References and Serialization</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EntityId{
}
