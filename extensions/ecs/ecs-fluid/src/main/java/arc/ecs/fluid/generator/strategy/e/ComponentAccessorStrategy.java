package arc.ecs.fluid.generator.strategy.e;

import arc.ecs.fluid.generator.common.*;
import arc.ecs.fluid.generator.model.artemis.*;
import arc.ecs.fluid.generator.model.type.*;
import arc.ecs.fluid.generator.util.*;

/**
 * Adds method to get component from entity.
 * @author Daan van Yperen
 */
public class ComponentAccessorStrategy extends IterativeModelStrategy{

    @Override
    protected void apply(ComponentDescriptor component, TypeModel model){
        model.add(createGetComponentMethod(component));
    }

    /**
     * T _componentName() -> return instance of entity Component E::_componentName()
     */
    private MethodDescriptor createGetComponentMethod(ComponentDescriptor component){
        return
        new MethodBuilder(component.getComponentType(), Strings.assembleMethodName(component.getPreferences().getPrefixComponentGetter(), component.getMethodPrefix()))
        .debugNotes(component.getComponentType().getName())
        .mapper("return ", component, ".get(entityId)")
        .build();
    }
}
