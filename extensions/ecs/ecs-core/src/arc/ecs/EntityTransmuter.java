package arc.ecs;

import arc.ecs.utils.*;

/**
 * Fastest way of changing entity component compositions. Primarily useful when
 * bootstrapping entities over several different systems or when
 * dealing with many entities at the same time (light particle systems etc).
 * <p>
 * Given a set of component additions/removals: for each encountered
 * compositionId, cache the calculated new compositionId. This extends
 * the performance benefits introduced with
 * {@link Archetype Archetypes} in 0.7.0 to carry over to existing entities.
 * </p>
 * @see EntityTransmuterFactory
 */
public final class EntityTransmuter{
    private final Factory factory;

    private final EntityManager em;
    private final BatchChangeProcessor batchProcessor;
    private final Bag<TransmuteOperation> operations;

    private final ShortBag entityToIdentity;

    public EntityTransmuter(Base base, Aspect.Builder aspect){
        this(base, base.getAspectSubscriptionManager().get(aspect).getAspect());
    }

    EntityTransmuter(Base base, Aspect aspect){
        this(base, new BitVector(aspect.allSet), new BitVector(aspect.exclusionSet));
    }

    EntityTransmuter(Base base, BitVector additions, BitVector removals){
        em = base.getEntityManager();
        entityToIdentity = base.getComponentManager().entityToIdentity;
        batchProcessor = base.batchProcessor;
        operations = new Bag<>(TransmuteOperation.class);

        factory = new Factory(base, additions, removals);
    }


    /**
     * <p>Apply on target entity. Does nothing if entity has been scheduled for
     * deletion.</p>
     *
     * <p>Transmuter will add components by replacing and retire pre-existing components.</p>
     * @param entityId target entity id
     */
    public void transmute(int entityId){
        if(!isValid(entityId)) return;

        TransmuteOperation operation = getOperation(entityId);
        operation.perform(entityId);
        entityToIdentity.unsafeSet(entityId, operation.compositionId);
    }

    void transmuteNoOperation(int entityId){
        if(!isValid(entityId)) return;

        TransmuteOperation operation = getOperation(entityId);
        entityToIdentity.unsafeSet(entityId, operation.compositionId);
    }

    private boolean isValid(int entityId){
        if(!em.isActive(entityId))
            throw new RuntimeException("Issued transmute on deleted " + entityId);

        if(batchProcessor.isDeleted(entityId))
            return false;

        batchProcessor.changed.unsafeSet(entityId);

        return true;
    }

    /**
     * Apply on target entity.
     * <p>
     * Transmuter will add components by replacing and retire pre-existing components.
     * @param e target entity.
     */
    public void transmute(Entity e){
        transmute(e.id);
    }

    TransmuteOperation getOperation(int entityId){
        return operation(entityId, entityToIdentity.get(entityId));
    }

    private TransmuteOperation operation(int entityId, int compositionId){
        TransmuteOperation operation = operations.safeGet(compositionId);
        if(operation == null){
            operation = factory.createOperation(entityId);
            operations.set(compositionId, operation);
        }
        return operation;
    }

    @Override
    public String toString(){
        return "EntityTransmuter(add=" + factory.additions + " remove=" + factory.removals + ")";
    }

    static class Factory{
        private final ComponentManager cm;
        private final BitVector additions;
        private final BitVector removals;
        private final BitVector bs;

        Factory(Base base, BitVector additions, BitVector removals){
            this.cm = base.getComponentManager();
            this.additions = additions;
            this.removals = removals;
            this.bs = new BitVector();
        }

        TransmuteOperation createOperation(int entityId){
            BitVector componentBits = cm.componentBits(entityId);

            bs.clear();
            bs.or(componentBits);
            bs.or(additions);
            bs.andNot(removals);
            int compositionId = cm.compositionIdentity(bs);
            return new TransmuteOperation(compositionId,
            getAdditions(componentBits), getRemovals(componentBits));
        }

        private Bag<Mapper> getAdditions(BitVector origin){
            ComponentTypeFactory tf = cm.typeFactory;
            Bag<Mapper> types = new Bag(Mapper.class);
            for(int i = additions.nextSetBit(0); i >= 0; i = additions.nextSetBit(i + 1)){
                if(!origin.get(i))
                    types.add(cm.getMapper(tf.getTypeFor(i).getType()));
            }

            return types;
        }

        private Bag<Mapper> getRemovals(BitVector origin){
            ComponentTypeFactory tf = cm.typeFactory;
            Bag<Mapper> types = new Bag(Mapper.class);
            for(int i = removals.nextSetBit(0); i >= 0; i = removals.nextSetBit(i + 1)){
                if(origin.get(i))
                    types.add(cm.getMapper(tf.getTypeFor(i).getType()));
            }

            return types;
        }
    }

    static class TransmuteOperation{
        private final Mapper[] additions;
        private final Mapper[] removals;

        public final short compositionId;

        public TransmuteOperation(int compositionId, Mapper[] additions, Mapper[] removals){
            this.compositionId = (short)compositionId;
            this.additions = additions;
            this.removals = removals;
        }

        public TransmuteOperation(int compositionId, Bag<Mapper> additions, Bag<Mapper> removals){

            this.compositionId = (short)compositionId;
            this.additions = new Mapper[additions.size()];
            this.removals = new Mapper[removals.size()];

            for(int i = 0, s = additions.size(); s > i; i++){
                this.additions[i] = additions.get(i);
            }

            for(int i = 0, s = removals.size(); s > i; i++){
                this.removals[i] = removals.get(i);
            }
        }

        public void perform(int entityId){
            for(Mapper addition : additions){
                addition.internalCreate(entityId);
            }

            for(Mapper removal : removals){
                removal.internalRemove(entityId);
            }
        }

        Bag<Class<? extends Component>> getAdditions(Bag<Class<? extends Component>> out){
            for(Mapper addition : additions){
                out.add(addition.getType().getType());
            }

            return out;
        }

        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder();
            sb.append("TransmuteOperation(");

            if(additions.length > 0){
                sb.append("add={");
                String delim = "";
                for(Mapper mapper : additions){
                    sb.append(delim).append(mapper.getType().getType().getSimpleName());
                    delim = ", ";
                }
                sb.append("}");
            }

            if(removals.length > 0){
                if(additions.length > 0)
                    sb.append(" ");

                sb.append("remove={");
                String delim = "";
                for(Mapper mapper : removals){
                    sb.append(delim).append(mapper.getType().getType().getSimpleName());
                    delim = ", ";
                }
                sb.append("}");
            }
            sb.append(")");

            return sb.toString();
        }
    }

}
