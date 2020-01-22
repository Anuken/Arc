package arc.ecs.fluid.generator.strategy.e;

import arc.ecs.fluid.generator.model.artemis.*;
import arc.ecs.fluid.generator.model.type.*;

import java.lang.reflect.*;

/**
 * Strategy for extending the fluid interface based on component fields.
 * <p>
 * Extend this and put it as a dependency on the fluid grade or maven module. The fluid interface generator will
 * scan the classpath for strategies and automatically call them.
 * @author Daan van Yperen
 * @see arc.ecs.fluid.generator.strategy.e.DefaultFieldProxyStrategy for example.
 */
public interface FieldProxyStrategy{

    /**
     * priority of this strategy compared to others.
     * Higher priority strategies will get first chance to match fields. Use {@code 0} for default.
     * @return desired priority.
     */
    int priority();

    /**
     * @param component Artemis component.
     * @param field Field to be proxied.
     * @param model Type model to extend.
     * @return {@code true} if this strategy wants to handle the proxy.
     */
    boolean matches(ComponentDescriptor component, Field field, TypeModel model);

    /**
     * Apply changes to model. Will be called only once per field.
     * @param component Artemis component.
     * @param field Field to be proxied.
     * @param model Type model to extend.
     */
    void execute(ComponentDescriptor component, Field field, TypeModel model);
}
