package arc.ecs.systems;

import arc.ecs.*;
import arc.ecs.utils.*;

/**
 * Iterates over {@link EntitySubscription} member entities by
 * entity identity.
 * <p>
 * Use this when you need to process entities matching an {@link Aspect},
 * and you want maximum performance.
 * @author Arni Arent
 * @author Adrian Papari
 */
public abstract class IteratingSystem extends BaseEntitySystem{

    /**
     * Creates a new IteratingSystem.
     * @param aspect the aspect to match entities
     */
    public IteratingSystem(Aspect.Builder aspect){
        super(aspect);
    }

    public IteratingSystem(){
    }

    /**
     * Process a entity this system is interested in.
     * @param entity the entity to process
     */
    protected abstract void process(int entity);

    /** @inheritDoc */
    @Override
    protected final void processSystem(){
        IntBag actives = subscription.getEntities();
        int[] ids = actives.getData();
        for(int i = 0, s = actives.size(); s > i; i++){
            process(ids[i]);
        }
    }
}
