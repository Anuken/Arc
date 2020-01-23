package arc.ecs.fluid.generator.strategy.e;

import arc.ecs.*;
import arc.ecs.fluid.generator.common.*;
import arc.ecs.fluid.generator.model.*;
import arc.ecs.fluid.generator.model.artemis.*;
import arc.ecs.fluid.generator.model.type.*;
import arc.ecs.fluid.generator.util.*;

/**
 * Generate basic scaffold of E class.
 * <p>
 * - basic class setup (fields, initialization).
 * - static method to obtain instances of E.
 * @author Daan van Yperen
 */
public class EBaseStrategy implements BuilderModelStrategy{

    @Override
    public void apply(ArtemisModel artemisModel, TypeModel model){
        model.name = "E";
        model.packageName = "arc.ecs";
        model.add(createMapperField());
        model.add(createStaticMapperField());
        model.add(createEntityIdField());
        model.add(createInitMethod());
        model.add(createStaticInstancerMethodByInt());
        model.add(createStaticInstancerMethodByEntity());
        model.add(createStaticInstancerMethodNewEntity());

        model.add(createEntityIdGetter());
        model.add(createEntityGetter());
    }

    private MethodDescriptor createInitMethod(){
        return
        new MethodBuilder(FluidTypes.E_TYPE, "init")
        .parameter(FluidTypes.SUPERMAPPER_TYPE, "mappers")
        .parameter(int.class, "entityId")
        .statement("this.mappers = mappers")
        .statement("this.entityId = entityId")
        .returnFluid()
        .build();
    }

    private FieldDescriptor createEntityIdField(){
        return new FieldBuilder(int.class, "entityId")
        .debugNotes("Default entityId field.")
        .build();
    }

    private FieldDescriptor createMapperField(){
        return new FieldBuilder(FluidTypes.SUPERMAPPER_TYPE, "mappers")
        .debugNotes("Default mappers field.")
        .build();
    }

    private FieldDescriptor createStaticMapperField(){
        return new FieldBuilder(FluidTypes.SUPERMAPPER_TYPE, "_processingMapper")
        .debugNotes("Default _processingMapper field.")
        .setStatic(true).build();
    }

    /**
     * static E::E(entityId)
     */
    private MethodDescriptor createStaticInstancerMethodByInt(){
        return
        new MethodBuilder(FluidTypes.E_TYPE, "E")
        .setStatic(true)
        .parameter(int.class, "entityId")
        .statement("if(_processingMapper==null) throw new RuntimeException(\"SuperMapper system must be registered before any systems using E().\");")
        .statement("return _processingMapper.getE(entityId)")
        .build();
    }

    /**
     * static E::E(entity)
     */
    private MethodDescriptor createStaticInstancerMethodByEntity(){
        return
        new MethodBuilder(FluidTypes.E_TYPE, "E")
        .setStatic(true)
        .parameter(Entity.class, "entity")
        .statement("return E(entity.getId())")
        .build();
    }

    /**
     * static E::E() Create a new entity.
     */
    private MethodDescriptor createStaticInstancerMethodNewEntity(){
        return
        new MethodBuilder(FluidTypes.E_TYPE, "E")
        .setStatic(true)
        .statement("return E(_processingMapper.getBase().create())")
        .build();
    }

    /**
     * Getter Entity E::entity()
     */
    private MethodDescriptor createEntityGetter(){
        return
        new MethodBuilder(Entity.class, "entity")
        .statement("return mappers.getBase().getEntity(entityId)")
        .build();
    }

    /**
     * Getter Entity E::id()
     */
    private MethodDescriptor createEntityIdGetter(){
        return
        new MethodBuilder(int.class, "id")
        .statement("return entityId")
        .build();
    }

}
