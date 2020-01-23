package arc.ecs;

import arc.ecs.annotations.*;
import arc.ecs.utils.*;

import static arc.ecs.utils.ArReflect.implementsAnyObserver;

/**
 * Tracks a subset of entities, but does not implement any sorting or iteration.
 * @author Arni Arent
 * @author Adrian Papari
 */
public abstract class BaseEntitySystem extends BaseSystem implements EntitySubscription.SubscriptionListener{
    private final Aspect.Builder aspectConfiguration;
    protected EntitySubscription subscription;

    public BaseEntitySystem(){
        this(null);
    }

    /**
     * Creates an entity system that uses the specified aspect as a matcher
     * against entities.
     * @param aspect to match against entities
     */
    public BaseEntitySystem(Aspect.Builder aspect){
        if(aspect == null){
            aspect = getAnnotationAspect();
            if(aspect == null){
                String error = "Aspect was null and no aspect annotations set on system (@All); to use systems which " +
                "do not subscribe to entities, extend BaseSystem directly.";
                throw new NullPointerException(error);
            }
        }

        aspectConfiguration = aspect;
    }

    @Override
    protected void setBase(Base base){
        super.setBase(base);

        subscription = getSubscription();
        if(implementsAnyObserver(this))
            subscription.addSubscriptionListener(this);
    }

    /**
     * Return aspect as defined in annotation.
     * @return {@code Aspect.Builder} as defined in annotations, or {@code null} if none.
     */
    protected Aspect.Builder getAnnotationAspect(){
        Class<? extends BaseSystem> c = getClass();
        final Aspect.Builder aspect = Aspect.all();
        final All all = c.getAnnotation(All.class);
        if(all != null){
            aspect.all(all.value());
        }
        final One one = c.getAnnotation(One.class);
        if(one != null){
            aspect.one(one.value());
        }
        final Exclude exclude = c.getAnnotation(Exclude.class);
        if(exclude != null){
            aspect.exclude(exclude.value());
        }
        return (all != null || exclude != null || one != null) ? aspect : null;
    }

    /** @return entity subscription backing this system.*/
    public EntitySubscription getSubscription(){
        return base.getAspectSubscriptionManager().get(aspectConfiguration);
    }

    @Override
    public void inserted(IntBag entities){
        int[] ids = entities.getData();
        for(int i = 0, s = entities.size(); s > i; i++){
            inserted(ids[i]);
        }
    }

    /**
     * Gets the entities processed by this system. Do not delete entities from
     * this bag - it is the live thing.
     * @return System's entity ids, as matched by aspect.
     */
    public IntBag getEntityIds(){
        return subscription.getEntities();
    }

    /**
     * Called if entity has come into scope for this system, e.g created or a component was added to it.
     * <p>
     * Triggers right after any system finishes processing. Adding and immediately removing a component
     * does not count as a permanently change and will prevent this method from being called.
     * <p>
     * Not triggered for entities that have been destroyed immediately after being created (within a system).
     * @param entityId the entity that was added to this system
     */
    protected void inserted(int entityId){
    }

    /**
     * <p>Called if entity has gone out of scope of this system, e.g deleted
     * or had one of it's components removed.</p>
     * <p>
     * <p>Explicitly removed components are only retrievable at this point
     * if annotated with {@link DelayedComponentRemoval}.</p>
     * <p>
     * <p>Deleted entities retain all their components - until all listeners
     * have been informed.</p>
     * @param entities entities removed from this system.
     */
    @Override
    public void removed(IntBag entities){
        int[] ids = entities.getData();
        for(int i = 0, s = entities.size(); s > i; i++){
            removed(ids[i]);
        }
    }

    /**
     * <p>Called if entity has gone out of scope of this system, e.g deleted
     * or had one of it's components removed.</p>
     * <p>
     * Important note on accessing components:
     * Using {@link Mapper#get(int)} to retrieve a component is unsafe, unless:
     * - You annotate the component with {@link DelayedComponentRemoval}.
     * - {@link Base#isAlwaysDelayComponentRemoval} is enabled to make accessing all components safe,
     * for a small performance hit.
     * <p>
     * {@link Mapper#has(int)} always returns {@code false}, even for DelayedComponentRemoval components.
     * <p>
     * Can trigger for entities that have been destroyed immediately after being created (within a system).
     * @param entityId the entity that was removed from this system
     */
    protected void removed(int entityId){
    }
}
