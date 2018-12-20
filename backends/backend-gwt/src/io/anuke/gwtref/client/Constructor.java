package io.anuke.gwtref.client;

import java.lang.annotation.Annotation;

/**
 * A constructor for the enclosing type.
 * @author mzechner
 */
public class Constructor extends Method{
    Constructor(String name, Class enclosingType, Class returnType, Parameter[] parameters, boolean isAbstract, boolean isFinal,
                boolean isStatic, boolean isDefaultAccess, boolean isPrivate, boolean isProtected, boolean isPublic, boolean isNative,
                boolean isVarArgs, boolean isMethod, boolean isConstructor, int methodId, Annotation[] annotations){
        super(name, enclosingType, returnType, parameters, isAbstract, isFinal, isStatic, isDefaultAccess, isPrivate, isProtected,
        isPublic, isNative, isVarArgs, isMethod, isConstructor, methodId, annotations);
    }

    /** @return a new instance of the enclosing type of this constructor. */
    public Object newInstance(Object... params){
        return super.invoke(null, params);
    }
}
