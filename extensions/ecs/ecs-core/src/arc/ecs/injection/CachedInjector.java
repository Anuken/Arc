package arc.ecs.injection;

import arc.ecs.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * By default, injects {@link Mapper}, {@link BaseSystem} and {@link Manager} types into systems and
 * managers. Can also inject arbitrary types if registered through {@link BaseConfig#register}.
 * <p>
 * Caches all type-information.
 *
 * <p>
 * For greater control over the dependency-resolution, provide a {@link FieldHandler} to {@link #setFieldHandler(FieldHandler)},
 * which will be used to resolve dependency values instead.
 * </p>
 * @author Arni Arent
 * @author Snorre E. Brekke
 * @see arc.ecs.injection.FieldHandler
 */
public final class CachedInjector implements Injector{
    private InjectionCache cache = InjectionCache.sharedCache.get();
    private FieldHandler fieldHandler;
    private Map<String, Object> injectables;

    @Override
    public Injector setFieldHandler(FieldHandler fieldHandler){
        this.fieldHandler = fieldHandler;
        return this;
    }

    @Override
    public <T> T getRegistered(String id){
        return (T)injectables.get(id);
    }

    @Override
    public <T> T getRegistered(Class<T> id){
        return getRegistered(id.getName());
    }

    @Override
    public void initialize(Base base, Map<String, Object> injectables){
        this.injectables = injectables;
        if(fieldHandler == null){
            fieldHandler = new FieldHandler(cache);
        }

        fieldHandler.initialize(base, injectables);
    }

    @Override
    public boolean isInjectable(Object target){
        CachedClass cachedClass = cache.getCachedClass(target.getClass());
        return cachedClass.wireType == WireType.WIRE;
    }

    @Override
    public void inject(Object target) throws RuntimeException{
        try{
            Class<?> clazz = target.getClass();
            CachedClass cachedClass = cache.getCachedClass(clazz);

            if(cachedClass.wireType == WireType.WIRE){
                injectValidFields(target, cachedClass);
            }else{
                injectAnnotatedFields(target, cachedClass);
            }
        }catch(RuntimeException e){
            throw new WireException("Error while wiring " + target.getClass().getName(), e);
        }
    }

    private void injectValidFields(Object target, CachedClass cachedClass){
        Field[] declaredFields = getAllInjectableFields(cachedClass);
        for(Field declaredField : declaredFields){
            injectField(target, declaredField, cachedClass.failOnNull);
        }
    }

    private Field[] getAllInjectableFields(CachedClass cachedClass){
        Field[] declaredFields = cachedClass.allFields;
        if(declaredFields == null){
            List<Field> fieldList = new ArrayList<>();
            Class<?> clazz = cachedClass.clazz;
            collectDeclaredInjectableFields(fieldList, clazz);

            while(cachedClass.injectInherited && (clazz = clazz.getSuperclass()) != Object.class){
                collectDeclaredInjectableFields(fieldList, clazz);
            }
            cachedClass.allFields = declaredFields = fieldList.toArray(new Field[0]);
        }
        return declaredFields;
    }

    private void collectDeclaredInjectableFields(List<Field> fieldList, Class<?> clazz){

        if(cache.getCachedClass(clazz).wireType != WireType.SKIPWIRE){
            Field[] classFields = clazz.getDeclaredFields();
            for(Field classField : classFields){
                if(isWireable(classField)){
                    fieldList.add(classField);
                }
            }
        }
    }

    private boolean isWireable(Field field){
        return cache.getCachedField(field).wireType != WireType.SKIPWIRE;
    }

    private void injectAnnotatedFields(Object target, CachedClass cachedClass){
        injectClass(target, cachedClass);
    }

    private void injectClass(Object target, CachedClass cachedClass){
        Field[] declaredFields = getAllInjectableFields(cachedClass);
        for(Field field : declaredFields){
            CachedField cachedField = cache.getCachedField(field);
            if(cachedField.wireType != WireType.IGNORED){
                injectField(target, field, cachedField.wireType == WireType.WIRE);
            }
        }
    }

    private void injectField(Object target, Field field, boolean failOnNotInjected){
        Class<?> fieldType;
        try{
            fieldType = field.getType();
        }catch(RuntimeException ignore){
            return;
        }

        Object resolve = fieldHandler.resolve(target, fieldType, field);
        if(resolve != null){
            setField(target, field, resolve);
        }

        if(resolve == null && failOnNotInjected && cache.getFieldClassType(fieldType) != ClassType.CUSTOM){
            throw onFailedInjection(fieldType.getSimpleName(), field);
        }
    }

    private void setField(Object target, Field field, Object fieldValue){
        try{
            field.setAccessible(true);
            field.set(target, fieldValue);
        }catch(Exception e){
            throw new WireException("Failed to set " + field + " of " + target, e);
        }
    }

    private WireException onFailedInjection(String typeName, Field failedInjection){
        String error = "Failed to inject " + failedInjection.getType().getName() +
        " into " + failedInjection.getDeclaringClass().getName() + ": " +
        typeName + " not registered with base.";

        return new WireException(error);
    }
}
