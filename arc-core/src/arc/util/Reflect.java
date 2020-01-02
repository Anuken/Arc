package arc.util;

import java.lang.reflect.*;

@SuppressWarnings("unchecked")
public class Reflect{

    public static <T> T get(Field field){
        return get(null, field);
    }

    public static <T> T get(Object object, Field field){
        try{
            return (T)field.get(object);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static <T> T get(Class<?> type, Object object, String name){
        try{
            Field field = type.getDeclaredField(name);
            field.setAccessible(true);
            return (T)field.get(object);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static <T> T get(Object object, String name){
        return get(object.getClass(), object, name);
    }

    public static <T> T get(Class<?> type, String name){
        return get(type, null, name);
    }

    public static void set(Class<?> type, Object object, String name, Object value){
        try{
            Field field = type.getDeclaredField(name);
            field.setAccessible(true);
            field.set(object, value);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static void set(Object object, String name, Object value){
        set(object.getClass(), object, name, value);
    }

    public static void set(Class<?> type, String name, Object value){
        set(type, null, name, value);
    }
}
