package arc.ecs.injection;

import arc.ecs.*;

import java.util.*;

/**
 * Field resolver for manually registered objects, for injection by type or name.
 * @author Daan van Yperen
 * @see BaseConfig#register
 */
public interface PojoFieldResolver extends FieldResolver{

    /**
     * Set manaully registered objects.
     * @param pojos Map of manually registered objects.
     */
    void setPojos(Map<String, Object> pojos);
}
