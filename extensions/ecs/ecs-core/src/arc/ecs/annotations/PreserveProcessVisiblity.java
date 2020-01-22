package arc.ecs.annotations;

import arc.ecs.systems.*;

import java.lang.annotation.*;

/**
 * When optimizing an {@link EntityProcessingSystem}, don't reduce the visibility
 * of {@link EntityProcessingSystem#process()}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface PreserveProcessVisiblity{
}
