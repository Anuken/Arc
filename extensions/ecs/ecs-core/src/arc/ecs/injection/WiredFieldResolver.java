package arc.ecs.injection;

import arc.ecs.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * Can inject arbitrary fields annotated with {@link arc.ecs.annotations.Wire},
 * typically registered via registered via {@link BaseConfig#register}
 * @author Snorre E. Brekke
 */
public class WiredFieldResolver implements UseInjectionCache, PojoFieldResolver{
    private InjectionCache cache;

    private Map<String, Object> pojos = new HashMap<>();
    private Base base;

    public WiredFieldResolver(){
    }

    @Override
    public void initialize(Base base){
        this.base = base;
    }

    @Override
    public Object resolve(Object target, Class<?> fieldType, Field field){
        ClassType injectionType = cache.getFieldClassType(fieldType);
        CachedField cachedField = cache.getCachedField(field);

        if(injectionType == ClassType.CUSTOM || injectionType == ClassType.BASE){
            if(cachedField.wireType == WireType.WIRE){
                String key = cachedField.name;
                if("".equals(key)){
                    key = field.getType().getName();
                }

                if(!pojos.containsKey(key) && cachedField.failOnNull){
                    String err = "Not registered: " + key + "=" + fieldType;
                    throw new WireException(err);
                }

                return pojos.get(key);
            }
        }
        return null;
    }

    @Override
    public void setCache(InjectionCache cache){
        this.cache = cache;
    }

    @Override
    public void setPojos(Map<String, Object> pojos){
        this.pojos = pojos;
    }
}
