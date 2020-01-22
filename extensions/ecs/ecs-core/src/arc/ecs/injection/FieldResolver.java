package arc.ecs.injection;

import arc.ecs.*;

import java.lang.reflect.*;

/**
 * API used by {@link FieldHandler} to resolve field values in classes eligible for injection.
 * @author Snorre E. Brekke
 */
public interface FieldResolver{

    /**
     * Called after Wo
     */
    void initialize(Base base);

    /**
     * @param target object which should have dependencies injected.
     */
    Object resolve(Object target, Class<?> fieldType, Field field);
}
