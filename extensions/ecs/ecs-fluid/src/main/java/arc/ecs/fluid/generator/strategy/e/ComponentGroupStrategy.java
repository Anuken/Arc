package arc.ecs.fluid.generator.strategy.e;

import arc.ecs.fluid.generator.common.*;
import arc.ecs.fluid.generator.model.*;
import arc.ecs.fluid.generator.model.artemis.*;
import arc.ecs.fluid.generator.model.type.*;
import arc.ecs.fluid.generator.util.*;
import arc.ecs.utils.*;

/**
 * Adds methods to access entities groups, and find entities by group.
 * @author Daan van Yperen
 */
public class ComponentGroupStrategy implements BuilderModelStrategy{

    private MethodDescriptor createGroupSetter(){
        return
        new MethodBuilder(FluidTypes.E_TYPE, "group")
        .parameter(String.class, "group")
        .debugNotes("default group setter")
        .statement("World w = mappers.getWorld()")
        .statement("w.getSystem(arc.ecs.managers.GroupManager.class).add(w.getEntity(entityId), group)")
        .returnFluid()
        .build();
    }

    private MethodDescriptor createGroupsSetter(){
        return
        new MethodBuilder(FluidTypes.E_TYPE, "groups")
        .varArgs(true)
        .parameter(String[].class, "groups")
        .debugNotes("default groups setter")
        .statement("for (int i = 0; groups.length > i; i++) { group(groups[i]); }")
        .returnFluid()
        .build();
    }


    private MethodDescriptor createGroupRemover(){
        return
        new MethodBuilder(FluidTypes.E_TYPE, "removeGroup")
        .parameter(String.class, "group")
        .debugNotes("default group remover")
        .statement("World w = mappers.getWorld()")
        .statement("w.getSystem(arc.ecs.managers.GroupManager.class).remove(w.getEntity(entityId), group)")
        .returnFluid()
        .build();
    }

    private MethodDescriptor createGroupsRemover(){
        return
        new MethodBuilder(FluidTypes.E_TYPE, "removeGroups")
        .varArgs(true)
        .parameter(String[].class, "groups")
        .debugNotes("default groups remover")
        .statement("for (int i = 0; groups.length > i; i++) { removeGroup(groups[i]); }")
        .returnFluid()
        .build();
    }


    private MethodDescriptor createAllGroupRemover(){
        return
        new MethodBuilder(FluidTypes.E_TYPE, "removeGroups")
        .debugNotes("default groups remover")
        .statement("World w = mappers.getWorld()")
        .statement("w.getSystem(arc.ecs.managers.GroupManager.class).removeFromAllGroups(w.getEntity(entityId))")
        .returnFluid()
        .build();
    }

    private MethodDescriptor createGroupsGetter(){
        return
        new MethodBuilder(new ParameterizedTypeImpl(ImmutableBag.class, String.class), "groups")
        .debugNotes("default groups getter")
        .statement("World w = mappers.getWorld()")
        .statement("return w.getSystem(arc.ecs.managers.GroupManager.class).getGroups(w.getEntity(entityId))")
        .build();
    }


    private MethodDescriptor createIsInGroup(){
        return
        new MethodBuilder(boolean.class, "isInGroup")
        .parameter(String.class, "group")
        .debugNotes("default group setter")
        .statement("World w = mappers.getWorld()")
        .statement("return w.getSystem(arc.ecs.managers.GroupManager.class).isInGroup(w.getEntity(entityId), group)")
        .build();
    }


    /**
     * static EBag E::withGroup(groupName)
     */
    private MethodDescriptor createStaticWithGroup(){
        return
        new MethodBuilder(FluidTypes.EBAG_TYPE, "withGroup")
        .setStatic(true)
        .parameter(String.class, "groupName")
        .javaDoc("Get entities in group..\n@return {@code EBag} of entities in group. Returns empty bag if group contains no entities.")
        .statement("if(_processingMapper==null) throw new RuntimeException(\"SuperMapper system must be registered before any systems using E().\");")
        .statement("return new EBag((arc.ecs.utils.IntBag)_processingMapper.getWorld().getSystem(arc.ecs.managers.GroupManager.class).getEntityIds(groupName))")
        .build();
    }

    @Override
    public void apply(ArtemisModel artemisModel, TypeModel model){
        model.add(createGroupSetter());
        model.add(createGroupsSetter());
        model.add(createGroupRemover());
        model.add(createGroupsRemover());
        model.add(createAllGroupRemover());
        model.add(createGroupsGetter());
        model.add(createIsInGroup());
        model.add(createStaticWithGroup());
    }
}
