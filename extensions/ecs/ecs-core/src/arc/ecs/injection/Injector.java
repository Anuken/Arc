package arc.ecs.injection;

import arc.ecs.*;

import java.util.*;

/**
 * <p>API used by {@link World} to inject objects annotated with {@link arc.ecs.annotations.Wire} with
 * dependencies. An injector injects {@link ComponentMapper}, {@link BaseSystem} and {@link com.artemis
 * .Manager} types into systems and managers.
 * </p>
 * <p>To inject arbitrary types, use registered through {@link WorldConfig#register}.</p>
 * <p>To customize the injection-strategy for arbitrary types further, registered a custom {@link arc.ecs.injection.FieldHandler}
 * with custom one or more {@link arc.ecs.injection.FieldResolver}.</p>
 * @author Snorre E. Brekke
 * @see FieldHandler
 */
public interface Injector{

    /**
     * Programmatic retrieval of registered objects. Useful when
     * full injection isn't necessary.
     * @param id Name or class name.
     * @return the requested object, or null if not found
     * @see WorldConfig#register(String, Object)
     */
    <T> T getRegistered(String id);

    /**
     * Programmatic retrieval of registered objects. Useful when
     * full injection isn't necessary. This method internally
     * calls {@link #getRegistered(String)}, with the class name
     * as parameter.
     * @param id Uniquely registered instance, identified by class..
     * @return the requested object, or null if not found
     * @see WorldConfig#register(Object)
     */
    <T> T getRegistered(Class<T> id);

    /**
     * @param world this Injector will be used for
     * @param injectables registered via {@link WorldConfig#register}
     */
    void initialize(World world, Map<String, Object> injectables);

    /**
     * Inject dependencies on object. The injector delegates to {@link arc.ecs.injection.FieldHandler} to resolve
     * feiled values.
     * @param target object which should have dependencies injected.
     * @see FieldHandler
     */
    void inject(Object target) throws RuntimeException;

    /**
     * Determins if a target object can be injected by this injector.
     * @param target eligable for injection
     * @return true if the Injector is capable of injecting the target object.
     */
    boolean isInjectable(Object target);

    /**
     * Enables the injector to be configured with a custom {@link arc.ecs.injection.FieldHandler} which will
     * be used to resolve instance values for target-fields.
     * @param fieldHandler to use for resolving dependency values
     * @return this Injector for chaining
     */
    Injector setFieldHandler(FieldHandler fieldHandler);

}
