package arc.ecs.fluid.generator.strategy.e;

import arc.ecs.fluid.generator.common.*;
import arc.ecs.fluid.generator.model.artemis.*;
import arc.ecs.fluid.generator.model.type.*;
import arc.ecs.fluid.generator.util.*;

/**
 * Adds methods to check if entity has a component.
 * @author Daan van Yperen
 */
public class ComponentExistStrategy extends IterativeModelStrategy{

    @Override
    protected void apply(ComponentDescriptor component, TypeModel model){
        model.add(createHasComponentMethod(component));
    }

    /**
     * boolean E::hasComponent()
     */
    private MethodDescriptor createHasComponentMethod(ComponentDescriptor component){
        return
        new MethodBuilder(boolean.class,

        Strings.assembleMethodName(component.getPreferences().getPrefixComponentHas(), component.getName()))
        .debugNotes(component.getComponentType().getName())
        .mapper("return ", component, ".has(entityId)")
        .build();
    }
}
