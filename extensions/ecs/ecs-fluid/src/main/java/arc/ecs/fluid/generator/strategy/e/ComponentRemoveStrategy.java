package arc.ecs.fluid.generator.strategy.e;

import arc.ecs.fluid.generator.common.*;
import arc.ecs.fluid.generator.model.*;
import arc.ecs.fluid.generator.model.artemis.*;
import arc.ecs.fluid.generator.model.type.*;
import arc.ecs.fluid.generator.util.*;

/**
 * Adds methods to remove components from entity.
 * @author Daan van Yperen
 */
public class ComponentRemoveStrategy extends IterativeModelStrategy{

    @Override
    protected void apply(ComponentDescriptor component, TypeModel model){
        model.add(removeComponentStrategy(component));
    }

    /**
     * T componentName() -> create new entity.
     */
    private MethodDescriptor removeComponentStrategy(ComponentDescriptor component){
        return
        new MethodBuilder(FluidTypes.E_TYPE,
        Strings.assembleMethodName(component.getPreferences().getPrefixComponentRemove(), component.getName()))
        .debugNotes(component.getComponentType().getName())
        .mapper(component, ".remove(entityId)")
        .returnFluid()
        .build();
    }
}
