package arc.ecs.annotations;

import arc.ecs.*;

import java.lang.annotation.*;

/**
 * Transforms a {@link Component} into a {@link PooledComponent}. Component transformation
 * takes place during the <code>artemis</code> goal defined in <code>artemis-odb-maven-plugin</code>
 * or the <code>weave</code> task in <code>artemis-odb-gradle-plugin</code>.
 * <p>
 * This feature helps mitigate garbage collection related freezes and stuttering on the Android platform.
 * It is considered stable. If pooling is not available on a platform, this feature will degrade
 * gracefully to vanilla components.
 * @see <a href="https://github.com/junkdog/artemis-odb/wiki/%40PooledWeaver">Component pooling</a>
 * on the wiki.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Documented
public @interface PooledWeaver{

    /**
     * If true, forces weaving even if maven property <code>enablePooledWeaving</code> is
     * set to <code>false</code>.
     */
    boolean forceWeaving() default false;
}
