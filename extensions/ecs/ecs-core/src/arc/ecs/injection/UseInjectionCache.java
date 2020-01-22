package arc.ecs.injection;

import arc.ecs.*;

/**
 * {@link FieldResolver} implementing this interface will have the {@link #setCache(InjectionCache)}
 * method called during , prior to {@link FieldResolver#initialize(Base)}
 * being called.
 * @author Snorre E. Brekke
 */
public interface UseInjectionCache{
    /**
     * @param cache used by the {@link FieldHandler}
     */
    void setCache(InjectionCache cache);
}
