package arc.ecs.fluid.generator.strategy.e;

import arc.ecs.fluid.generator.common.*;
import arc.ecs.fluid.generator.model.artemis.*;
import arc.ecs.fluid.generator.model.type.*;
import arc.ecs.fluid.generator.util.*;

/**
 * Adds deleteFromWorld() to fluid interface.
 * @author Daan van Yperen
 */
public class EToStringStrategy implements BuilderModelStrategy{

    private MethodDescriptor toStringMethod(){
        return
        new MethodBuilder(String.class, "toString")
        .debugNotes("default toString")
        .statement("return \"E{id=\" + entityId + \"}\"")
        .build();
    }

    @Override
    public void apply(ArtemisModel artemisModel, TypeModel model){
        model.add(toStringMethod());
    }
}
