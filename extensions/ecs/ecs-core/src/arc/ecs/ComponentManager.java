package arc.ecs;

import arc.ecs.annotations.*;
import arc.ecs.utils.*;


/**
 * Handles the association between entities and their components.
 * <p>
 * Only one component manager exists per {@link Base} instance,
 * managed by the world.
 * </p>
 * @author Arni Arent
 */
@SkipWire
public class ComponentManager extends BaseSystem{
    /** Adrian's secret rebellion. */
    static final int NO_COMPONENTS = 0;

    /** Collects all Entites marked for deletion from this ComponentManager. */
    private Bag<Mapper> mappers = new Bag<>(Mapper.class);

    private final ComponentIdentityResolver identityResolver = new ComponentIdentityResolver();
    final ShortBag entityToIdentity;
    protected final ComponentTypeFactory typeFactory;

    /**
     * Creates a new instance of {@link ComponentManager}.
     */
    protected ComponentManager(int entityContainerSize){
        entityToIdentity = new ShortBag(entityContainerSize);
        typeFactory = new ComponentTypeFactory(this, entityContainerSize);
    }

    @Override
    protected void processSystem(){
    }

    /**
     * Create a component of given type by class.
     * @param owner entity id
     * @param componentClass class of component to instance.
     * @return Newly created packed, pooled or basic component.
     */
    protected <T extends Component> T create(int owner, Class<T> componentClass){
        return getMapper(componentClass).create(owner);
    }

    protected <T extends Component> Mapper<T> getMapper(Class<T> mapper){
        ComponentType type = typeFactory.getTypeFor(mapper);
        return mappers.get(type.getIndex());
    }

    void registerComponentType(ComponentType ct, int capacity){
        int index = ct.getIndex();
        Mapper mapper = new Mapper(ct.getType(), base);
        mapper.components.ensureCapacity(capacity);
        mappers.set(index, mapper);
    }

    static <T extends Component> T newInstance(Class<T> componentClass){
        try{
            return componentClass.newInstance();
        }catch(Exception e){
            throw new InvalidComponentException(componentClass, "Unable to instantiate component.", e);
        }
    }


    /**
     * Removes all components from deleted entities.
     * @param pendingPurge the entities to remove components from
     */
    void clean(IntBag pendingPurge){
        int[] ids = pendingPurge.getData();
        for(int i = 0, s = pendingPurge.size(); s > i; i++){
            removeComponents(ids[i]);
        }
    }

    private void removeComponents(int entityId){
        Bag<Mapper> mappers = componentMappers(entityId);
        for(int i = 0, s = mappers.size(); s > i; i++){
            mappers.get(i).internalRemove(entityId);
        }

        setIdentity(entityId, 0);
    }

    /**
     * Get all components from all entities for a given type.
     * @param type the type of components to get
     * @return a bag containing all components of the given type
     */
    protected Bag<Component> getComponentsByType(ComponentType type){
        return mappers.get(type.getIndex()).components;
    }

    /**
     * @return Bag of all generated component types, which identify components without having to use classes.
     */
    public ImmutableBag<ComponentType> getComponentTypes(){
        return typeFactory.types;
    }

    /**
     * Get a component of an entity.
     * @param entityId the entity associated with the component
     * @param type the type of component to get
     * @return the component of given type
     */
    protected Component getComponent(int entityId, ComponentType type){
        Mapper mapper = mappers.get(type.getIndex());
        return mapper.get(entityId);
    }

    /**
     * Get all component associated with an entity.
     * @param entityId the entity to get components from
     * @param fillBag a bag to be filled with components
     * @return the {@code fillBag}, filled with the entities components
     */
    public Bag<Component> getComponentsFor(int entityId, Bag<Component> fillBag){
        Bag<Mapper> mappers = componentMappers(entityId);

        for(int i = 0, s = mappers.size(); s > i; i++){
            fillBag.add(mappers.get(i).get(entityId));
        }

        return fillBag;
    }

    /** Get component composition of entity. */
    BitVector componentBits(int entityId){
        int identityIndex = entityToIdentity.get(entityId);
        return identityResolver.compositionBits.get(identityIndex);
    }

    /** Get component composition of entity. */
    private Bag<Mapper> componentMappers(int entityId){
        int identityIndex = entityToIdentity.get(entityId);
        return identityResolver.compositionMappers.get(identityIndex);
    }

    /**
     * Fetches unique identifier for composition.
     * @param componentBits composition to fetch unique identifier for.
     * @return Unique identifier for passed composition.
     */
    public int compositionIdentity(BitVector componentBits){
        int identity = identityResolver.getIdentity(componentBits);
        if(identity == -1){
            identity = identityResolver.allocateIdentity(componentBits, this);
            base.getAspectSubscriptionManager()
            .processComponentIdentity(identity, componentBits);
        }

        return identity;
    }

    /**
     * Fetch composition id for entity.
     * <p>
     * A composition id is uniquely identified by a single Aspect. For performance reasons, each entity is
     * identified by its composition id. Adding or removing components from an entity will change its compositionId.
     * @return composition identity.
     */
    public int getIdentity(int entityId){
        return entityToIdentity.get(entityId);
    }

    /**
     * Synchronizes new subscriptions with {@link Base} state.
     * @param es entity subscription to update.
     */
    void synchronize(EntitySubscription es){
        Bag<BitVector> compositionBits = identityResolver.compositionBits;
        for(int i = 1, s = compositionBits.size(); s > i; i++){
            BitVector componentBits = compositionBits.get(i);
            es.processComponentIdentity(i, componentBits);
        }

        for(Entity e : base.getEntityManager().entities){
            if(e != null) es.check(e.id, getIdentity(e.id));
        }

        es.informEntityChanges();
        es.rebuildCompressedActives();
    }

    /**
     * Set composition id of entity.
     * @param entityId entity id
     * @param compositionId composition id
     */
    void setIdentity(int entityId, int compositionId){
        entityToIdentity.unsafeSet(entityId, (short)compositionId);
    }

    /**
     * @return Factory responsible for tracking all component types.
     */
    public ComponentTypeFactory getTypeFactory(){
        return typeFactory;
    }

    public void ensureCapacity(int newSize){
        typeFactory.initialMapperCapacity = newSize;
        entityToIdentity.ensureCapacity(newSize);
        for(Mapper mapper : mappers){
            mapper.components.ensureCapacity(newSize);
        }
    }

    /** Tracks all unique component compositions. */
    static final class ComponentIdentityResolver{
        final Bag<BitVector> compositionBits;
        final Bag<Bag<Mapper>> compositionMappers;

        ComponentIdentityResolver(){
            compositionBits = new Bag(BitVector.class);
            compositionBits.add(new BitVector());
            compositionMappers = new Bag<>();
            compositionMappers.add(new Bag(Mapper.class));
        }

        /** Fetch unique identity for passed composition. */
        int getIdentity(BitVector components){
            BitVector[] bitsets = compositionBits.getData();
            int size = compositionBits.size();
            for(int i = NO_COMPONENTS; size > i; i++){ // want to start from 1 so that 0 can mean null
                if(components.equals(bitsets[i]))
                    return i;
            }

            return -1;
        }

        int allocateIdentity(BitVector componentBits, ComponentManager cm){
            Bag<Mapper> mappers =
            new Bag<>(Mapper.class, componentBits.cardinality());

            ComponentTypeFactory tf = cm.getTypeFactory();
            for(int i = componentBits.nextSetBit(0); i >= 0; i = componentBits.nextSetBit(i + 1)){
                mappers.add(cm.getMapper(tf.getTypeFor(i).getType()));
            }

            compositionMappers.add(mappers);
            compositionBits.add(new BitVector(componentBits));

            return compositionBits.size() - 1;
        }
    }
}
