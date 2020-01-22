package arc.ecs.annotations;

import arc.ecs.*;

import java.lang.annotation.*;

/**
 * <p>Annotation for entity-referencing fields in component types. This annotation
 * allows overriding the default link policy. Fields referencing a single
 * entity - int or {@link Entity} - default to
 * {@link Policy#CHECK_SOURCE_AND_TARGETS}, while Bag and IntBag of entities are
 * assigned {@link Policy#CHECK_SOURCE}.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface LinkPolicy{
    Policy value();

    enum Policy{
        /**
         * Performs no validation on this field's entity reference(s).
         */
        SKIP,

        /**
         * Validates entity id.
         * @see arc.ecs.link.LinkListener#onLinkEstablished(int, int)
         * @see arc.ecs.link.LinkListener#onLinkKilled(int, int)
         */
        CHECK_SOURCE,

        /**
         * <p>Validates source entity and any targets.
         * {@link arc.ecs.link.LinkListener#onTargetChanged(int, int, int)} is
         * only invoked if the field deals with a single entity.</p>
         * @see arc.ecs.link.LinkListener#onTargetChanged(int, int, int)
         * @see arc.ecs.link.LinkListener#onTargetDead(int, int)
         */
        CHECK_SOURCE_AND_TARGETS
    }
}
