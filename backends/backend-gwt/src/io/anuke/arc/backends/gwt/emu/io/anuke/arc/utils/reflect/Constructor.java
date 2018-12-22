package io.anuke.arc.util.reflect;

import io.anuke.gwtref.client.Parameter;

/**
 * Provides information about, and access to, a single constructor for a Class.
 * @author nexsoftware
 */
public final class Constructor{

    private final io.anuke.gwtref.client.Constructor constructor;

    Constructor(io.anuke.gwtref.client.Constructor constructor){
        this.constructor = constructor;
    }

    /** Returns an array of Class objects that represent the formal parameter types, in declaration order, of the constructor. */
    public Class[] getParameterTypes(){
        Parameter[] parameters = constructor.getParameters();
        Class[] parameterTypes = new Class[parameters.length];
        for(int i = 0, j = parameters.length; i < j; i++){
            parameterTypes[i] = parameters[i].getClazz();
        }
        return parameterTypes;
    }

    /** Returns the Class object representing the class or interface that declares the constructor. */
    public Class getDeclaringClass(){
        return constructor.getEnclosingType();
    }

    public boolean isAccessible(){
        return constructor.isPublic();
    }

    public void setAccessible(boolean accessible){
        // NOOP in GWT
    }

    /**
     * Uses the constructor to create and initialize a new instance of the constructor's declaring class, with the supplied
     * initialization parameters.
     */
    public Object newInstance(Object... args) throws ReflectionException{
        try{
            return constructor.newInstance(args);
        }catch(IllegalArgumentException e){
            throw new ReflectionException("Illegal argument(s) supplied to constructor for class: " + getDeclaringClass().getName(),
            e);
        }
    }

}
