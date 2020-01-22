package arc.ecs.annotations;

import arc.ecs.EntitySubscription.*;
import arc.ecs.utils.*;

import java.lang.annotation.*;

/**
 * Extends the lifecycle of this component type, ensuring removed instances are retrievable until
 * all {@link SubscriptionListener#removed(IntBag) listeners} have been notified - regardless
 * of removal method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DelayedComponentRemoval{
}
