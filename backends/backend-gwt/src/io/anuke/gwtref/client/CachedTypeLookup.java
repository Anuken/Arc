package io.anuke.gwtref.client;

/**
 * A cache for a Type lookups.
 * @author hneuer
 */
class CachedTypeLookup{
    final Class clazz;
    private Type type;

    CachedTypeLookup(Class clazz){
        this.clazz = clazz;
    }

    Type getType(){
        if(type == null && clazz != null) type = ReflectionCache.getType(clazz);
        return type;
    }

    @Override
    public String toString(){
        return String.valueOf(clazz);
    }
}
