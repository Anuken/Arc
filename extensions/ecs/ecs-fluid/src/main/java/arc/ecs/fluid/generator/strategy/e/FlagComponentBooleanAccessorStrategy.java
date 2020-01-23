package arc.ecs.fluid.generator.strategy.e;

import arc.ecs.fluid.generator.common.*;
import arc.ecs.fluid.generator.model.*;
import arc.ecs.fluid.generator.model.artemis.*;
import arc.ecs.fluid.generator.model.type.*;
import arc.ecs.fluid.generator.util.*;

/**
 * Adds boolean accessors for flag components.
 *
 * <p>
 * Flag components are all components with no public fields and methods.
 * @author Daan van Yperen
 */
public class FlagComponentBooleanAccessorStrategy extends IterativeModelStrategy{

    @Override
    protected void apply(ComponentDescriptor component, TypeModel model){
        Class type = component.getComponentType();

        if(component.isFlagComponent()){
            model.add(createCheckFlagComponentExistenceMethod(component));
            model.add(createFlagComponentToggleMethod(component));
        }
    }

    private MethodDescriptor createFlagComponentToggleMethod(ComponentDescriptor component){
        return
        new MethodBuilder(FluidTypes.E_TYPE, component.getMethodPrefix())
        .parameter(boolean.class, "value")
        .mapper(component, ".set(entityId, value)")
        .debugNotes("flag component(=field/method-less) " + component.getComponentType().getName())
        .returnFluid()
        .build();
    }

    /**
     * T componentName() -> create new entity.
     */
    private MethodDescriptor createCheckFlagComponentExistenceMethod(ComponentDescriptor component){
        return
        new MethodBuilder(boolean.class, "is" + component.getName())
        .debugNotes("flag component(=field/method-less) " + component.getComponentType().getName())
        .mapper("return ", component, ".has(entityId)")
        .build();
    }
}
