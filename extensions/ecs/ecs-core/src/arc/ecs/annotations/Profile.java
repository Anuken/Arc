package arc.ecs.annotations;

import arc.ecs.utils.*;

import java.lang.annotation.*;

/**
 * Profile EntitySystems with user-specified profiler class, implementing ArtemisProfiler.
 *
 * <p>Injects conditional profiler call at start of <code>begin()</code> and before any exit
 * point in <code>end()</code>.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface Profile{
    Class<? extends ArtemisProfiler> using();

    boolean enabled() default false;
}