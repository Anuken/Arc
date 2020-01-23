package arc.ecs.weaver.impl.optimizer;

import arc.ecs.weaver.meta.*;

public enum EntitySystemType{
    ITERATING(
    "arc/ecs/systems/IteratingSystem",
    "arc/ecs/BaseEntitySystem");

    public final String superName;
    public final String replacedSuperName;

    EntitySystemType(String superName, String replacedSuperName){
        this.superName = superName;
        this.replacedSuperName = replacedSuperName;
    }

    public static EntitySystemType resolve(ClassMetadata meta){
        return resolve(meta.superClass);
    }

    public static EntitySystemType resolve(String owner){
        for(EntitySystemType type : EntitySystemType.values()){
            if(type.superName.equals(owner))
                return type;
        }

        return null;
    }
}
