package arc.ecs;

import arc.ecs.utils.*;

public class ComponentPool<T extends PooledComponent>{
    private final Bag<T> cache;
    private Class<T> type;

    ComponentPool(Class<T> type){
        this.type = type;
        cache = new Bag<>(type);
    }

    @SuppressWarnings("unchecked")
    <T extends PooledComponent> T obtain(){
        try{
            return (T)((cache.size() > 0)
            ? cache.removeLast()
            : type.newInstance());
        }catch(Exception e){
            throw new InvalidComponentException(type, e.getMessage(), e);
        }
    }

    void free(T component){
        component.reset();
        cache.add(component);
    }
}
