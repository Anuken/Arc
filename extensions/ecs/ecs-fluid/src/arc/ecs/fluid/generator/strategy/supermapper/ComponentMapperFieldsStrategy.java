package arc.ecs.fluid.generator.strategy.supermapper;

import arc.ecs.*;
import arc.ecs.fluid.generator.common.*;
import arc.ecs.fluid.generator.model.artemis.*;
import arc.ecs.fluid.generator.model.type.*;
import arc.ecs.fluid.generator.util.*;

/**
 * Generates component mappers for each component.
 * @author Daan van Yperen
 */
public class ComponentMapperFieldsStrategy extends IterativeModelStrategy{

    @Override
    protected void apply(ComponentDescriptor component, TypeModel model){
        model.add(createComponentMapper(component));
    }

    private FieldDescriptor createComponentMapper(ComponentDescriptor component){
        return new FieldBuilder(new ParameterizedTypeImpl(Mapper.class, component.getComponentType()), "m" + component.getName())
        .debugNotes(component.getComponentType().getName())
        .setAccessLevel(AccessLevel.PUBLIC)
        .build();
    }

}
