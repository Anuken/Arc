package arc.ecs.fluid.generator.strategy.e;

import arc.ecs.*;
import arc.ecs.fluid.generator.common.*;
import arc.ecs.fluid.generator.model.*;
import arc.ecs.fluid.generator.model.artemis.*;
import arc.ecs.fluid.generator.model.type.*;
import arc.ecs.fluid.generator.util.*;

/**
 * Adds methods to locate entities by aspect or component.
 * @author Daan van Yperen
 */
public class EQueryExtensionsStrategy implements BuilderModelStrategy{

    @Override
    public void apply(ArtemisModel artemisModel, TypeModel model){
        model.add(createStaticWithAspect());
        model.add(createStaticWithComponent());
    }

    /**
     * static EBag E::withAspect(aspect)
     */
    private MethodDescriptor createStaticWithAspect(){
        return
        new MethodBuilder(FluidTypes.EBAG_TYPE, "withAspect")
        .setStatic(true)
        .parameter(Aspect.Builder.class, "aspect")
        .javaDoc("Get all entities matching aspect.\nFor performance reasons do not create the aspect every call.\n@return {@code EBag} of entities matching aspect. Returns empty bag if no entities match aspect.")
        .statement("if(_processingMapper==null) throw new RuntimeException(\"SuperMapper system must be registered before any systems using E().\");")
        .statement("return new EBag(_processingMapper.getBase().getAspectSubscriptionManager().get(aspect).getEntities())")
        .build();
    }

    /**
     * static EBag E::withComponent(component)
     */
    private MethodDescriptor createStaticWithComponent(){
        return
        new MethodBuilder(FluidTypes.EBAG_TYPE, "withComponent")
        .setStatic(true)
        .parameter(new ParameterizedTypeImpl(Class.class, FluidTypes.EXTENDS_COMPONENT_TYPE), "component")
        .javaDoc("Get all entities with component.\nThis is a relatively costly operation. For performance use withAspect instead.\n@return {@code EBag} of entities matching aspect. Returns empty bag if no entities match aspect.")
        .statement("if(_processingMapper==null) throw new RuntimeException(\"SuperMapper system must be registered before any systems using E().\");")
        .statement("return new EBag(_processingMapper.getBase().getAspectSubscriptionManager().get(Aspect.all(component)).getEntities())")
        .build();
    }
}
