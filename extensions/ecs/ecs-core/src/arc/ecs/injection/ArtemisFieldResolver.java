package arc.ecs.injection;

import arc.ecs.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * Can resolve {@link Base}, {@link Mapper}, {@link BaseSystem} and
 * {@link arc.ecs.Manager} types registered in the {@link Base}
 * @author Snorre E. Brekke
 */
public class ArtemisFieldResolver implements FieldResolver, UseInjectionCache{

    private Base base;
    private InjectionCache cache;

    private Map<Class<?>, Class<?>> systems;

    public ArtemisFieldResolver(){
        systems = new IdentityHashMap<>();
    }

    @Override
    public void initialize(Base base){
        this.base = base;

        for(BaseSystem es : base.getSystems()){
            Class<?> origin = es.getClass();
            Class<?> clazz = origin;
            do{
                systems.put(clazz, origin);
            }while((clazz = clazz.getSuperclass()) != Object.class);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object resolve(Object target, Class<?> fieldType, Field field){
        ClassType injectionType = cache.getFieldClassType(fieldType);
        switch(injectionType){
            case MAPPER:
                return getComponentMapper(field);
            case SYSTEM:
                return base.getSystem((Class<BaseSystem>)systems.get(fieldType));
            case BASE:
                return base;
            default:
                return null;

        }
    }

    @SuppressWarnings("unchecked")
    private Mapper<?> getComponentMapper(Field field){
        Class<?> mapperType = cache.getGenericType(field);
        return base.getMapper((Class<? extends Component>)mapperType);

    }

    @Override
    public void setCache(InjectionCache cache){
        this.cache = cache;
    }
}
