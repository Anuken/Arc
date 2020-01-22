package arc.ecs.fluid.generator.strategy.components;

import arc.ecs.fluid.generator.common.*;
import arc.ecs.fluid.generator.model.artemis.*;
import arc.ecs.fluid.generator.model.type.*;
import arc.ecs.fluid.generator.util.*;

/**
 * Generates create method for each component type.
 * @author Daan van Yperen
 */
public class ComponentsClassLibraryStrategy extends IterativeModelStrategy{

    @Override
    protected void apply(ComponentDescriptor component, TypeModel model){
        model.add(componentClassField(component));
    }

    /**
     * T componentName() -> create new entity.
     */
    private FieldDescriptor componentClassField(ComponentDescriptor component){
        return new FieldBuilder(new ParameterizedTypeImpl(Class.class, component.getComponentType()), component.getName())
        .debugNotes(component.getComponentType().getName())
        .setAccessLevel(AccessLevel.PUBLIC)
        .setStatic(true)
        .setFinal(true)
        .initializer(component.getComponentType().getSimpleName() + ".class")
        .build();
    }
}
