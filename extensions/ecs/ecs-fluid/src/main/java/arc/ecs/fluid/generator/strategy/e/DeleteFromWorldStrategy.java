package arc.ecs.fluid.generator.strategy.e;

import arc.ecs.fluid.generator.common.*;
import arc.ecs.fluid.generator.model.artemis.*;
import arc.ecs.fluid.generator.model.type.*;
import arc.ecs.fluid.generator.util.*;

/**
 * Adds deleteFromWorld() to fluid interface.
 * @author Daan van Yperen
 */
public class DeleteFromWorldStrategy implements BuilderModelStrategy{

    private MethodDescriptor deleteFromWorldMethod(){
        return
        new MethodBuilder(void.class, "deleteFromWorld")
        .debugNotes("default delete from world")
        .statement("mappers.getWorld().delete(entityId)")
        .build();
    }

    @Override
    public void apply(ArtemisModel artemisModel, TypeModel model){
        model.add(deleteFromWorldMethod());
    }
}
