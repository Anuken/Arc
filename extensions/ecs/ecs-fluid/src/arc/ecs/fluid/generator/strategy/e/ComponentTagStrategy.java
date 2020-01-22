package arc.ecs.fluid.generator.strategy.e;

import arc.ecs.fluid.generator.common.*;
import arc.ecs.fluid.generator.model.*;
import arc.ecs.fluid.generator.model.artemis.*;
import arc.ecs.fluid.generator.model.type.*;
import arc.ecs.fluid.generator.util.*;

/**
 * Adds convenience methods for accessing entities tag, and finding entity by tag.
 * @author Daan van Yperen
 */
public class ComponentTagStrategy implements BuilderModelStrategy{

    private MethodDescriptor createTagMethodSetter(){
        return
        new MethodBuilder(FluidTypes.E_TYPE, "tag")
        .parameter(String.class, "tag")
        .debugNotes("default tag setter")
        .statement("mappers.getWorld().getSystem(arc.ecs.managers.TagManager.class).register(tag, entityId)")
        .returnFluid()
        .build();
    }

    private MethodDescriptor createTagMethodGetter(){
        return
        new MethodBuilder(String.class, "tag")
        .debugNotes("default tag getter")
        .statement("return mappers.getWorld().getSystem(arc.ecs.managers.TagManager.class).getTag(entityId)")
        .build();
    }

    /**
     * static EBag E::withGroup(groupName)
     */
    private MethodDescriptor createStaticWithTag(){
        return
        new MethodBuilder(FluidTypes.E_TYPE, "withTag")
        .setStatic(true)
        .parameter(String.class, "tag")
        .javaDoc("Get entity by tag.\n@return {@code E}, or {@code null} if no such tag.")
        .statement("if(_processingMapper==null) throw new RuntimeException(\"SuperMapper system must be registered before any systems using E().\");")
        .statement("int id=_processingMapper.getWorld().getSystem(arc.ecs.managers.TagManager.class).getEntityId(tag)")
        .statement("return id != -1 ? E(id) : null")
        .build();
    }


    @Override
    public void apply(ArtemisModel artemisModel, TypeModel model){
        model.add(createTagMethodSetter());
        model.add(createTagMethodGetter());
        model.add(createStaticWithTag());
    }
}
