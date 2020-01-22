package arc.ecs;

import arc.ecs.annotations.*;
import arc.ecs.utils.*;

/**
 * Maintains the list of entities matched by an aspect. Entity subscriptions
 * are automatically updated during {@link Base#process()}.
 * <p>
 * Any {@link EntitySubscription.SubscriptionListener listeners}
 * are informed when entities are added or removed.
 * <p>
 * Be careful! Subscriptions do not immediately reflect changes by the
 * active system. Subscriptions only guarantee contained entities matched
 * before the current system started processing. Access entities and
 * components defensively.
 */
public class EntitySubscription{
    final SubscriptionExtra extra;

    private final IntBag entities;
    private final BitVector activeEntityIds;

    private final BitVector insertedIds;
    private final BitVector removedIds;

    final BitVector aspectCache = new BitVector();

    EntitySubscription(Base base, Aspect.Builder builder){
        extra = new SubscriptionExtra(builder.build(base), builder);

        activeEntityIds = new BitVector();
        entities = new IntBag();

        insertedIds = new BitVector();
        removedIds = new BitVector();

        EntityManager em = base.getEntityManager();
        em.registerEntityStore(activeEntityIds);
        em.registerEntityStore(insertedIds);
        em.registerEntityStore(removedIds);
    }

    /**
     * Returns a reference to the bag holding all matched entities.
     *
     * <p><b>Warning: </b> Never remove elements from the bag, as this
     * will lead to undefined behavior.</p>
     * @return View of all active entities.
     */
    public IntBag getEntities(){
        if(entities.isEmpty() && !activeEntityIds.isEmpty())
            rebuildCompressedActives();

        return entities;
    }

    /**
     * Returns the bitset tracking all matched entities.
     *
     * <p><b>Warning: </b> Never toggle bits in the bitset, as
     * this <i>may</i> lead to erroneously added or removed entities.</p>
     * @return View of all active entities.
     */
    public BitVector getActiveEntityIds(){
        return activeEntityIds;
    }

    /**
     * @return aspect used for matching entities to subscription.
     */
    public Aspect getAspect(){
        return extra.aspect;
    }

    /**
     * @return aspect builder used for matching entities to subscription.
     */
    public Aspect.Builder getAspectBuilder(){
        return extra.aspectReflection;
    }

    /**
     * A new unique component composition detected, check if this
     * subscription's aspect is interested in it.
     */
    void processComponentIdentity(int id, BitVector componentBits){
        aspectCache.ensureCapacity(id);
        aspectCache.set(id, extra.aspect.isInterested(componentBits));
    }

    void rebuildCompressedActives(){
        activeEntityIds.toIntBag(entities);
    }

    final void check(int id, int cid){
        boolean interested = aspectCache.unsafeGet(cid);
        boolean contains = activeEntityIds.unsafeGet(id);

        if(interested && !contains){
            insert(id);
        }else if(!interested && contains){
            remove(id);
        }
    }

    private void remove(int entityId){
        activeEntityIds.unsafeClear(entityId);
        removedIds.unsafeSet(entityId);
    }

    private void insert(int entityId){
        activeEntityIds.unsafeSet(entityId);
        insertedIds.unsafeSet(entityId);
    }

    void process(IntBag changed, IntBag deleted){
        deleted(deleted);
        changed(changed);

        informEntityChanges();
    }

    void processAll(IntBag changed, IntBag deleted){
        deletedAll(deleted);
        changed(changed);

        informEntityChanges();
    }

    void informEntityChanges(){
        if(insertedIds.isEmpty() && removedIds.isEmpty())
            return;

        transferBitsToInts(extra.inserted, extra.removed);
        extra.informEntityChanges();
        entities.setSize(0);
    }

    private void transferBitsToInts(IntBag inserted, IntBag removed){
        insertedIds.toIntBag(inserted);
        removedIds.toIntBag(removed);
        insertedIds.clear();
        removedIds.clear();
    }

    private void changed(IntBag entitiesWithCompositions){
        int[] ids = entitiesWithCompositions.getData();
        for(int i = 0, s = entitiesWithCompositions.size(); s > i; i += 2){
            int id = ids[i];
            boolean interested = aspectCache.unsafeGet(ids[i + 1]);
            boolean contains = activeEntityIds.unsafeGet(id);

            if(interested && !contains){
                insert(id);
            }else if(!interested && contains){
                remove(id);
            }
        }
    }

    private void deleted(IntBag entities){
        int[] ids = entities.getData();
        for(int i = 0, s = entities.size(); s > i; i++){
            int id = ids[i];
            if(activeEntityIds.unsafeGet(id))
                remove(id);
        }
    }

    private void deletedAll(IntBag entities){
        int[] ids = entities.getData();
        for(int i = 0, s = entities.size(); s > i; i++){
            int id = ids[i];
            activeEntityIds.unsafeClear(id);
            removedIds.unsafeSet(id);
        }
    }

    /**
     * Add listener interested in changes to the subscription.
     * @param listener listener to add.
     */
    public void addSubscriptionListener(SubscriptionListener listener){
        extra.listeners.add(listener);
    }

    /**
     * Remove previously registered listener.
     * @param listener listener to remove.
     */
    public void removeSubscriptionListener(SubscriptionListener listener){
        extra.listeners.remove(listener);
    }

    @Override
    public String toString(){
        return "EntitySubscription[" + getAspectBuilder() + "]";
    }

    /**
     * <p>This interfaces reports entities inserted or
     * removed when matched against their {@link EntitySubscription}</p>
     * <p>
     * Listeners are only triggered after a system finishes processing and the entity composition has changed.
     * <p>
     * Replacing a component with another of the same type does not permanently change the composition and does not
     * count as a composition change, neither does adding and immediately removing a component.
     */
    public interface SubscriptionListener{
        /**
         * Called after entities match an {@link EntitySubscription}.
         * <p>
         * Triggers right after a system finishes processing. Adding and immediately removing a component does not
         * permanently change the composition and will prevent this method from being called.
         * <p>
         * Not triggered for entities that have been destroyed immediately after being created (within a system).
         */
        void inserted(IntBag entities);

        /**
         * <p>Called after entities no longer match an EntitySubscription.</p>
         * <p>
         * Triggers right after a system finishes processing. Replacing a component with another of the same type
         * within a system does not count as a composition change and will prevent this method from being called.
         * <p>
         * Can trigger for entities that have been destroyed immediately after being created (within a system).
         *
         * <p>
         * Important note on accessing components:
         * Using {@link Mapper#get(int)} to retrieve a component is unsafe, unless:
         * - You annotate the component with {@link DelayedComponentRemoval}.
         * - {@link Base#isAlwaysDelayComponentRemoval} is enabled to make accessing all components safe,
         * for a small performance hit.
         * <p>
         * {@link Mapper#has(int)} always returns {@code false}, even for DelayedComponentRemoval components.
         */
        void removed(IntBag entities);
    }

    public static class SubscriptionExtra{
        final IntBag inserted = new IntBag();
        final IntBag removed = new IntBag();
        final Aspect aspect;
        final Aspect.Builder aspectReflection;
        final Bag<SubscriptionListener> listeners = new Bag<>();

        public SubscriptionExtra(Aspect aspect, Aspect.Builder aspectReflection){
            this.aspect = aspect;
            this.aspectReflection = aspectReflection;
        }

        void informEntityChanges(){
            informListeners();

            removed.setSize(0);
            inserted.setSize(0);
        }

        private void informListeners(){
            for(int i = 0, s = listeners.size(); s > i; i++){
                SubscriptionListener listener = listeners.get(i);
                if(removed.size() > 0)
                    listener.removed(removed);

                if(inserted.size() > 0)
                    listener.inserted(inserted);
            }
        }
    }
}
