package io.anuke.arc.utils.reflect;

import io.anuke.gwtref.client.ReflectionCache;

/**
 * Utilities for Array reflection.
 * @author nexsoftware
 */
public final class ArrayReflection{

    /** Creates a new array with the specified component type and length. */
    static public Object newInstance(Class c, int size){
        return ReflectionCache.newArray(c, size);
    }

    /** Returns the length of the supplied array. */
    static public int getLength(Object array){
        return ReflectionCache.getType(array.getClass()).getArrayLength(array);
    }

    /** Returns the value of the indexed component in the supplied array. */
    static public Object get(Object array, int index){
        return ReflectionCache.getType(array.getClass()).getArrayElement(array, index);
    }

    /** Sets the value of the indexed component in the supplied array to the supplied value. */
    static public void set(Object array, int index, Object value){
        ReflectionCache.getType(array.getClass()).setArrayElement(array, index, value);
    }
}
