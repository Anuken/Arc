package arc.ecs;

import arc.ecs.injection.*;
import arc.ecs.utils.*;

import java.util.*;

import static arc.ecs.BaseConfig.*;

/**
 * The primary instance for the framework.
 * <p>
 * It contains all the systems. You must use this to create, delete and
 * retrieve entities. It is also important to set the delta each game loop
 * iteration, and initialize before game loop.
 * </p>
 * @author Arni Arent
 * @author junkdog
 */
public class Base{

    /** Manages all entities for the world. */
    private final EntityManager em;

    /** Manages all component-entity associations for the world. */
    private final ComponentManager cm;

    /** Pool of entity edits. */
    final BatchChangeProcessor batchProcessor;

    /** Contains all systems unordered. */
    final Bag<BaseSystem> systemsBag;
    /** Manages all aspect based entity subscriptions for the world. */
    final AspectSubscriptionManager asm;

    /** Contains strategy for invoking systems upon process. */
    SystemInvoker invocationStrategy;

    final BaseSegment partition;

    /** The time passed since the last update. */
    public float delta;

    final boolean alwaysDelayComponentRemoval;

    /**
     * Creates a world without custom systems.
     * <p>
     * {@link EntityManager}, {@link ComponentManager} and {@link AspectSubscriptionManager} are
     * available by default.
     * </p>
     * Why are you using this? Use {@link #Base(BaseConfig)} to create a world with your own systems.
     */
    public Base(){
        this(new BaseConfig());
    }

    /**
     * Creates a new world.
     * <p>
     * {@link EntityManager}, {@link ComponentManager} and {@link AspectSubscriptionManager} are
     * available by default, on top of your own systems.
     * </p>
     * @see BaseConfigBuilder
     * @see BaseConfig
     */
    public Base(BaseConfig configuration){
        partition = new BaseSegment(configuration);
        systemsBag = configuration.systems;

        final ComponentManager lcm = (ComponentManager)systemsBag.get(COMPONENT_MANAGER_IDX);
        final EntityManager lem = (EntityManager)systemsBag.get(ENTITY_MANAGER_IDX);
        final AspectSubscriptionManager lasm = (AspectSubscriptionManager)systemsBag.get(ASPECT_SUBSCRIPTION_MANAGER_IDX);

        cm = lcm == null ? new ComponentManager(configuration.expectedEntityCount()) : lcm;
        em = lem == null ? new EntityManager(configuration.expectedEntityCount()) : lem;
        asm = lasm == null ? new AspectSubscriptionManager() : lasm;
        batchProcessor = new BatchChangeProcessor(this);
        alwaysDelayComponentRemoval = configuration.isAlwaysDelayComponentRemoval();

        configuration.initialize(this, partition.injector, asm);
    }

    /**
     * Inject dependencies on object.
     * <p/>
     * Immediately perform dependency injection on the target, even if the target isn't of an Artemis class.
     * <p/>
     * If you want to specify nonstandard dependencies to inject, use
     * {@link BaseConfig#register(String, Object)} instead, or
     * configure an {@link arc.ecs.injection.Injector}
     * <p/>
     * If you want a non-throwing alternative, use {@link #inject(Object, boolean)}
     * @param target Object to inject into.
     * throws {@link WireException} if {@code target} is annotated with {@link arc.ecs.annotations.SkipWire}
     * @see arc.ecs.annotations.Wire for more details about dependency injection.
     * @see #inject(Object, boolean)
     */
    public void inject(Object target){
        inject(target, true);
    }

    /**
     * Inject dependencies on object.
     * <p/>
     * Will not if it is annotated with {@link arc.ecs.annotations.Wire}.
     * <p/>
     * If you want to specify nonstandard dependencies to inject, use
     * {@link BaseConfig#register(String, Object)} instead, or
     * configure an {@link arc.ecs.injection.Injector}.
     * @param target Object to inject into.
     * @param failIfNotInjectable if true, this method will
     * throws {@link WireException} if {@code target} is annotated with
     * {@link arc.ecs.annotations.SkipWire} and {@code failIfNotInjectable} is true
     * @see arc.ecs.annotations.Wire for more details about dependency injection.
     * @see #inject(Object)
     */
    public void inject(Object target, boolean failIfNotInjectable){
        boolean injectable = partition.injector.isInjectable(target);
        if(!injectable && failIfNotInjectable)
            throw new WireException("Attempted injection on " + target.getClass()
            .getName() + ", which is annotated with @SkipWire");

        if(injectable)
            partition.injector.inject(target);
    }

    public <T> T getRegistered(String name){
        return partition.injector.getRegistered(name);
    }

    public <T> T getRegistered(Class<T> type){
        return partition.injector.getRegistered(type);
    }

    /**
     * Disposes all systems. Only necessary if either need to free
     * managed resources upon bringing the world to an end.
     * @throws RuntimeException if any system throws an exception.
     */
    public void dispose(){
        List<Throwable> exceptions = new ArrayList<>();

        for(BaseSystem system : systemsBag){
            try{
                system.dispose();
            }catch(Throwable e){
                exceptions.add(e);
            }
        }

        if(exceptions.size() > 0) {
            RuntimeException exc = new RuntimeException();
            for(Throwable t : exceptions){
                exc.addSuppressed(t);
            }

            throw exc;
        }
    }

    /**
     * Get entity editor for entity.
     * @param entityId entity to fetch editor for.
     * @return a fast albeit verbose editor to perform batch changes to entities.
     */
    public EntityEdit edit(int entityId){
        if(!em.isActive(entityId))
            throw new RuntimeException("Issued edit on deleted " + entityId);

        return batchProcessor.obtainEditor(entityId);
    }

    /**
     * Gets the <code>composition id</code> uniquely identifying the
     * component composition of an entity. Each composition identity maps
     * to one unique <code>BitVector</code>.
     * @param entityId Entity for which to get the composition id
     * @return composition identity of entity
     */
    public int compositionId(int entityId){
        return cm.getIdentity(entityId);
    }

    /**
     * Returns a manager that takes care of all the entities in the world.
     * @return entity manager
     */
    public EntityManager getEntityManager(){
        return em;
    }

    /**
     * Returns a manager that takes care of all the components in the world.
     * @return component manager
     */
    public ComponentManager getComponentManager(){
        return cm;
    }

    /**
     * Returns the manager responsible for creating and maintaining
     * {@link EntitySubscription subscriptions} in the world.
     * @return aspect subscription manager
     */
    public AspectSubscriptionManager getAspectSubscriptionManager(){
        return asm;
    }

    /**
     * Time since last game loop.
     * @return delta time since last game loop
     */
    public float getDelta(){
        return delta;
    }

    /**
     * You must specify the delta for the game here.
     * @param delta time since last game loop
     */
    public void setDelta(float delta){
        this.delta = delta;
    }

    /** Deletes all entities. */
    public void deleteAll(){
        IntBag entities = getAspectSubscriptionManager()
        .get(Aspect.all())
        .getEntities();

        int[] ids = entities.getData();
        for(int i = 0, s = entities.size(); s > i; i++){
            delete(ids[i]);
        }
    }

    /**
     * Delete the entity from the world.
     * @param e the entity to delete
     * @see #delete(int) recommended alternative.
     */
    public void deleteEntity(Entity e){
        delete(e.id);
    }

    /**
     * Delete the entity from the world.
     * <p>
     * The entity is considered to be in a final state once invoked;
     * adding or removing components from an entity scheduled for
     * deletion will likely throw exceptions.
     * @param entityId the entity to delete
     */
    public void delete(int entityId){
        batchProcessor.delete(entityId);
    }

    /**
     * Create and return a new or reused entity instance. Entity is
     * automatically added to the world.
     * @return entity
     * @see #create() recommended alternative.
     */
    public Entity createEntity(){
        Entity e = em.createEntityInstance();
        batchProcessor.changed.unsafeSet(e.getId());
        return e;
    }

    /**
     * Create and return a new or reused entity id. Entity is
     * automatically added to the world.
     * @return assigned entity id, where id >= 0.
     */
    public int create(){
        int entityId = em.create();
        batchProcessor.changed.unsafeSet(entityId);
        return entityId;
    }

    /**
     * Create and return an {@link Entity} wrapping a new or reused entity instance.
     * Entity is automatically added to the world.
     * <p>
     * Use {@link Entity#edit()} to set up your newly created entity.
     * <p>
     * You can also create entities using:
     * <ul>
     * <li>{@link arc.ecs.utils.EntityBuilder} Convenient entity creation. Not useful when pooling.</li>
     * <li>{@link Archetype} Fastest, low level, no parameterized components.</li>
     * <li><a href="https://github.com/junkdog/artemis-odb/wiki/Serialization">Serialization</a>,
     * with a simple prefab-like class to parameterize the entities.</li>
     * </ul>
     * @return entity
     * @see #create() recommended alternative.
     */
    public Entity createEntity(Archetype archetype){
        Entity e = em.createEntityInstance();

        int id = e.getId();
        archetype.transmuter.perform(id);
        cm.setIdentity(e.id, archetype.compositionId);

        batchProcessor.changed.unsafeSet(id);

        return e;
    }

    /**
     * Create and return an {@link Entity} wrapping a new or reused entity instance.
     * Entity is automatically added to the world.
     * <p>
     * Use {@link Entity#edit()} to set up your newly created entity.
     * <p>
     * You can also create entities using:
     * - {@link arc.ecs.utils.EntityBuilder} Convenient entity creation. Not useful when pooling.
     * - {@link Archetype} Fastest, low level, no parameterized components.
     * @return assigned entity id
     */
    public int create(Archetype archetype){
        int entityId = em.create();

        archetype.transmuter.perform(entityId);
        cm.setIdentity(entityId, archetype.compositionId);

        batchProcessor.changed.unsafeSet(entityId);

        return entityId;
    }

    /**
     * Get entity with the specified id.
     * <p>
     * Resolves entity id to the unique entity instance. <em>This method may
     * return an entity even if it isn't active in the world.</em> Make sure to
     * not retain id's of deleted entities.
     * @param entityId the entities id
     * @return the specific entity
     */
    public Entity getEntity(int entityId){
        return em.getEntity(entityId);
    }

    /**
     * Gives you all the systems in this world for possible iteration.
     * @return all entity systems in world
     */
    public ImmutableBag<BaseSystem> getSystems(){
        return systemsBag;
    }

    /**
     * Retrieve a system for specified system type.
     * @param <T> the class type of system
     * @param type type of system
     * @return instance of the system in this world
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseSystem> T getSystem(Class<T> type){
        return (T)partition.systems.get(type);
    }

    /** Set strategy for invoking systems on {@link #process()}. */
    protected void setInvocationStrategy(SystemInvoker invocationStrategy){
        this.invocationStrategy = invocationStrategy;
        invocationStrategy.setBase(this);
        invocationStrategy.setSystems(systemsBag);
        invocationStrategy.initialize();
    }

    /**
     * Process all non-passive systems.
     * @see DefaultInvoker to control and extend how systems are invoked.
     */
    public void process(){
        invocationStrategy.process();

        IntBag pendingPurge = batchProcessor.getPendingPurge();
        if(!pendingPurge.isEmpty()){
            cm.clean(pendingPurge);
            em.clean(pendingPurge);

            batchProcessor.purgeComponents();
        }
    }

    /**
     * Retrieves a ComponentMapper instance for fast retrieval of components
     * from entities.
     * <p>
     * Odb automatically injects component mappers into systems, calling this
     * method is usually not required.,
     * @param <T> class type of the component
     * @param type type of component to get mapper for
     * @return mapper for specified component type
     */
    public <T extends Component> Mapper<T> getMapper(Class<T> type){
        return cm.getMapper(type);
    }

    /**
     * @return Injector responsible for dependency injection.
     */
    public Injector getInjector(){
        return partition.injector;
    }

    /**
     * @return Strategy used for invoking systems during {@link Base#process()}.
     */
    public <T extends SystemInvoker> T getInvocationStrategy(){
        return (T)invocationStrategy;
    }

    static class BaseSegment{
        /** Contains all systems and systems classes mapped. */
        final Map<Class<?>, BaseSystem> systems;

        /** Responsible for dependency injection. */
        final Injector injector;

        BaseSegment(BaseConfig configuration){
            systems = new IdentityHashMap<>();
            injector = (configuration.injector != null)
            ? configuration.injector
            : new CachedInjector();
        }
    }

    /**
     * When true, component removal is delayed for all components until all subscriptions have been notified.
     * @see BaseConfig#setAlwaysDelayComponentRemoval(boolean)
     * @see BaseConfigBuilder#alwaysDelayComponentRemoval(boolean)
     */
    public boolean isAlwaysDelayComponentRemoval(){
        return alwaysDelayComponentRemoval;
    }
}
