package arc.ecs;

import arc.ecs.EntityTransmuter.*;
import arc.ecs.utils.*;

/**
 * Builder for basic Archetype instances. To reap the maximum benefit of Archetypes,
 * it's recommended to stash them away inside a manager or similar. Archetypes'
 * main advantage come from the improved insertion into systems performance.
 * Calling {@link Entity#edit() edit()} on the Entity returned by {@link Base#createEntity(Archetype)}
 * nullifies this optimization.
 * <p>
 * Generated archetypes provide a blueprint for quick entity creation.
 * Instance generated entities using {@link Base#createEntity(Archetype)}
 * @since 0.7
 */
public class ArchetypeBuilder{
    private final Bag<Class<? extends Component>> classes;

    @SafeVarargs
    public static Archetype with(Base base, String name, Class<? extends Component>... types){
        return new ArchetypeBuilder().add(types).build(base, name);
    }

    @SafeVarargs
    public static Archetype with(Base base, String name, Archetype parent, Class<? extends Component>... types){
        return new ArchetypeBuilder(parent).add(types).build(base, name);
    }

    /**
     * Constructs an archetype builder containing the composition of the specified parent.
     * @param parent archetype composition to copy.
     */
    public ArchetypeBuilder(Archetype parent){
        classes = new Bag<>();
        if(parent == null)
            return;

        parent.transmuter.getAdditions(classes);
    }

    /**
     * Constructs an empty archetype builder.
     */
    public ArchetypeBuilder(){
        this(null);
    }

    /**
     * Ensure this builder includes the specified component type.
     * @return This instance for chaining.
     */
    public ArchetypeBuilder add(Class<? extends Component> type){
        if(!classes.contains(type))
            classes.add(type);

        return this;
    }

    /**
     * Ensure this builder includes the specified component types.
     * @return This instance for chaining.
     */
    public ArchetypeBuilder add(Class<? extends Component>... types){
        for(Class<? extends Component> type : types){
            if(!classes.contains(type))
                classes.add(type);
        }

        return this;
    }

    /**
     * Remove the specified component from this builder, if it is present (optional operation).
     * @return This instance for chaining.
     */
    public ArchetypeBuilder remove(Class<? extends Component> type){
        classes.remove(type);
        return this;
    }

    /**
     * Remove the specified component from this builder, if it is present (optional operation).
     * @return This instance for chaining.
     */
    public ArchetypeBuilder remove(Class<? extends Component>... types){
        for(Class<? extends Component> type : types){
            classes.remove(type);
        }

        return this;
    }

    /**
     * Create a new world specific instance of Archetype based on the current state.
     * @param base applicable domain of the Archetype.
     * @return new Archetype based on current state
     */
    public Archetype build(Base base){
        return build(base, null);
    }

    /**
     * Create a new world specific instance of Archetype based on the current state.
     * @param base applicable domain of the Archetype.
     * @param name uniquely identifies Archetype by name. If null or empty == compisitionId
     * @return new Archetype based on current state
     */
    public Archetype build(Base base, String name){
        ComponentType[] types = resolveTypes(base);

        ComponentManager cm = base.getComponentManager();
        Mapper[] mappers = new Mapper[types.length];
        for(int i = 0, s = mappers.length; s > i; i++){
            mappers[i] = cm.getMapper(types[i].getType());
        }

        int compositionId = cm.compositionIdentity(bitset(types));
        if(name == null || name.isEmpty()){
            name = String.valueOf(compositionId);
        }
        TransmuteOperation operation = new TransmuteOperation(compositionId, mappers, new Mapper[0]);

        return new Archetype(operation, compositionId, name);
    }

    /** generate bitset mask of types. */
    private static BitVector bitset(ComponentType[] types){
        BitVector bs = new BitVector();
        for(ComponentType type : types) bs.set(type.getIndex());

        return bs;
    }

    /** Converts java classes to component types. */
    private ComponentType[] resolveTypes(Base base){
        ComponentTypeFactory tf = base.getComponentManager().typeFactory;
        ComponentType[] types = new ComponentType[classes.size()];
        for(int i = 0, s = classes.size(); s > i; i++)
            types[i] = tf.getTypeFor(classes.get(i));

        return types;
    }
}
