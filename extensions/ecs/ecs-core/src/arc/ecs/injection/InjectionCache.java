package arc.ecs.injection;

import arc.ecs.*;
import arc.ecs.annotations.*;
import arc.ecs.utils.*;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Date: 31/7/2015
 * Time: 17:13 PM
 * @author Snorre E. Brekke
 */
public class InjectionCache{
    public static final SharedInjectionCache sharedCache = new SharedInjectionCache();

    private final Map<Class<?>, CachedClass> classCache = new HashMap<>();
    private final Map<Class<?>, ClassType> fieldClassTypeCache = new HashMap<>();
    private final Map<Field, CachedField> namedWireCache = new HashMap<>();
    private final Map<Field, Class<?>> genericsCache = new HashMap<>();

    private static final Wire DEFAULT_WIRE = new Wire(){

        @Override
        public Class<? extends Annotation> annotationType(){
            return Wire.class;
        }

        @Override
        public boolean injectInherited(){
            return true;
        }

        @Override
        public boolean failOnNull(){
            return true;
        }

        @Override
        public String name(){
            return null;
        }
    };

    public CachedClass getCachedClass(Class<?> clazz){
        CachedClass cachedClass = classCache.get(clazz);
        if(cachedClass == null){
            cachedClass = new CachedClass(clazz);

            cachedClass.wireType = getWireType(clazz);
            if(cachedClass.wireType == WireType.IGNORED && clazz != Object.class){
                setWireAnnotation(cachedClass, DEFAULT_WIRE);
            }else if(cachedClass.wireType == WireType.WIRE){
                setWireAnnotation(cachedClass, clazz.getAnnotation(Wire.class));
            }

            classCache.put(clazz, cachedClass);
        }
        return cachedClass;
    }

    /**
     * Set {@code @Wire} annotation value for cached class.
     */
    private void setWireAnnotation(CachedClass cachedClass, Wire wireAnnotation){
        cachedClass.wireType = WireType.WIRE;
        cachedClass.wireAnnotation = wireAnnotation;
        cachedClass.failOnNull = wireAnnotation.failOnNull();
        cachedClass.injectInherited = wireAnnotation.injectInherited();
    }

    /**
     * Determine desired wiring on class by annotation.
     * Convention is {@code Wire(injectInherited=true)}
     */
    private WireType getWireType(Class<?> clazz){
        return
        clazz.isAnnotationPresent(Wire.class) ? WireType.WIRE :
        clazz.isAnnotationPresent(SkipWire.class) ? WireType.SKIPWIRE :
        WireType.IGNORED;
    }


    public CachedField getCachedField(Field field){
        CachedField cachedField = namedWireCache.get(field);
        if(cachedField == null){
            if(field.isAnnotationPresent(Wire.class)){
                final Wire wire = field.getAnnotation(Wire.class);
                cachedField = new CachedField(field, WireType.WIRE, wire.name(), wire.failOnNull());
            }else if(field.isAnnotationPresent(SkipWire.class)){
                cachedField = new CachedField(field, WireType.SKIPWIRE, null, false);
            }else{
                cachedField = new CachedField(field, WireType.IGNORED, null, false);
            }
            namedWireCache.put(field, cachedField);
        }
        return cachedField;
    }


    public ClassType getFieldClassType(Class<?> fieldType){
        ClassType injectionType = fieldClassTypeCache.get(fieldType);
        if(injectionType == null){
            if(Mapper.class.isAssignableFrom(fieldType)){
                injectionType = ClassType.MAPPER;
            }else if(BaseSystem.class.isAssignableFrom(fieldType)){
                injectionType = ClassType.SYSTEM;
            }else if(Base.class.isAssignableFrom(fieldType)){
                injectionType = ClassType.BASE;
            }else{
                injectionType = ClassType.CUSTOM;
            }
            fieldClassTypeCache.put(fieldType, injectionType);
        }
        return injectionType;
    }

    public Class<?> getGenericType(Field field){
        Class<?> genericsType = genericsCache.get(field);
        if(genericsType == null){
            genericsType = ArReflect.getElementType(field, 0);
            genericsCache.put(field, genericsType);
        }
        return genericsType;
    }

}
