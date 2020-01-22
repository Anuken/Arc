package arc.ecs.fluid.generator.strategy.supermapper;

import arc.ecs.*;
import arc.ecs.fluid.generator.common.*;
import arc.ecs.fluid.generator.model.*;
import arc.ecs.fluid.generator.model.artemis.*;
import arc.ecs.fluid.generator.model.type.*;
import arc.ecs.fluid.generator.util.*;
import arc.ecs.utils.*;

/**
 * Generate basic scaffold for SuperMapper class.
 * @author Daan van Yperen
 */
public class SuperMapperStrategy implements BuilderModelStrategy{

    @Override
    public void apply(ArtemisModel artemisModel, TypeModel model){
        model.name = "SuperMapper";
        model.packageName = "arc.ecs";
        model.superclass = BaseSystem.class;
        model.add(createInitializationMethod());
        model.add(createProcessingMethod());
        model.add(createEInstancingMethod());
        model.add(createEPoolingSet());
    }

    private MethodDescriptor createProcessingMethod(){
        return new MethodBuilder(void.class, "processSystem")
        .accessLevel(AccessLevel.PUBLIC)
        .statement("E._processingMapper=this")
        .build();
    }

    private MethodDescriptor createInitializationMethod(){
        return new MethodBuilder(void.class, "initialize")
        .accessLevel(AccessLevel.PROTECTED)
        .statement("E._processingMapper=this")
        .build();
    }

    /**
     * SuperMapper::getE(entityId)
     */
    private MethodDescriptor createEInstancingMethod(){
        return new MethodBuilder(FluidTypes.E_TYPE, "getE")
        .accessLevel(AccessLevel.UNSPECIFIED) // package local.
        .parameter(int.class, "entityId")
        .statement("E e = (E) es.safeGet(entityId)")
        .statement("if ( e == null ) { e = new E().init(this,entityId); es.set(entityId, e); }")
        .statement("return e")
        .build();
    }

    private FieldDescriptor createEPoolingSet(){
        return
        new FieldBuilder(new ParameterizedTypeImpl(Bag.class, FluidTypes.E_TYPE), "es").initializer("new Bag<>(128)").build();
    }
}
