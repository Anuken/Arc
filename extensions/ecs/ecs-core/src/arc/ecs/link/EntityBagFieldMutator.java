package arc.ecs.link;

import arc.ecs.*;
import arc.ecs.utils.*;

import java.lang.reflect.*;

import static arc.ecs.Aspect.all;

class EntityBagFieldMutator implements MultiFieldMutator<Bag<Entity>, Component>{
    private final Bag<Entity> empty = new Bag<>();
    private EntitySubscription all;

    @Override
    public void validate(int sourceId, Bag<Entity> entities, LinkListener listener){
        for(int i = 0; entities.size() > i; i++){
            Entity e = entities.get(i);
            if(!all.getActiveEntityIds().unsafeGet(e.getId())){
                entities.remove(i--);
                if(listener != null)
                    listener.onTargetDead(sourceId, e.getId());
            }
        }
    }

    @Override
    public Bag<Entity> read(Component c, Field f){
        try{
            Bag<Entity> e = (Bag<Entity>)f.get(c);
            return (e != null) ? e : empty;
        }catch(Exception exc){
            throw new RuntimeException(exc);
        }
    }

    @Override
    public void setBase(Base base){
        all = base.getAspectSubscriptionManager().get(all());
    }
}
