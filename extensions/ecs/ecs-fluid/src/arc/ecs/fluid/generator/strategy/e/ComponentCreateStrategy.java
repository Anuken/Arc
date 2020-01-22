package arc.ecs.fluid.generator.strategy.e;

import arc.ecs.fluid.generator.common.*;
import arc.ecs.fluid.generator.model.*;
import arc.ecs.fluid.generator.model.artemis.*;
import arc.ecs.fluid.generator.model.type.*;
import arc.ecs.fluid.generator.util.*;

/**
 * Adds methods to create component (if missing).
 * @author Daan van Yperen
 */
public class ComponentCreateStrategy extends IterativeModelStrategy{

    @Override
    protected void apply(ComponentDescriptor component, TypeModel model){
        model.add(createComponentMethod(component));
    }

    /**
     * T componentName() -> create new component.
     */
    private MethodDescriptor createComponentMethod(ComponentDescriptor component){
        return
        new MethodBuilder(FluidTypes.E_TYPE,
        Strings.assembleMethodName(component.getPreferences().getPrefixComponentCreate(), component.getMethodPrefix()))
        .debugNotes(component.getComponentType().getName())
        .mapper(component, ".create(entityId)")
        .returnFluid()
        .build();
    }
}
