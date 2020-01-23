package arc.ecs.utils;

import arc.ecs.*;

import java.lang.reflect.*;
import java.util.*;

public final class ArReflect{
    private static final Class<?>[] PARAM_ENTITY = {Entity.class};
    private static final Class<?>[] PARAM_ID = {int.class};
    private static final Class<?>[] PARAM_IDS = {IntBag.class};

    private ArReflect(){
    }

    public static Class getElementType(java.lang.reflect.Field field, int index){
        Type genericType = field.getGenericType();
        if(genericType instanceof ParameterizedType){
            Type[] actualTypes = ((ParameterizedType)genericType).getActualTypeArguments();
            if(actualTypes.length - 1 >= index){
                Type actualType = actualTypes[index];
                if(actualType instanceof Class)
                    return (Class)actualType;
                else if(actualType instanceof ParameterizedType)
                    return (Class)((ParameterizedType)actualType).getRawType();
                else if(actualType instanceof GenericArrayType){
                    Type componentType = ((GenericArrayType)actualType).getGenericComponentType();
                    if(componentType instanceof Class) return Array.newInstance((Class)componentType, 0).getClass();
                }
            }
        }
        return null;
    }

    public static boolean implementsAnyObserver(BaseEntitySystem owner){

        // check parent chain for user-supplied implementations of
        // inserted() and removed()
        Class type = owner.getClass();
        while(type != BaseEntitySystem.class){
            for(Method m : type.getDeclaredMethods()){
                if(isObserver(m)) return true;
            }

            type = type.getSuperclass();
        }

        return false;
    }

    private static boolean isObserver(Method m){
        String name = m.getName();
        if("inserted".equals(name) || "removed".equals(name)){
            Class[] types = m.getParameterTypes();
            return Arrays.equals(PARAM_ID, types) || Arrays.equals(PARAM_IDS, types);
        }

        return false;
    }

    public static boolean isGenericType(Field f, Class<?> mainType, Class typeParameter){
        return mainType == f.getType() && typeParameter == getElementType(f, 0);
    }
}
