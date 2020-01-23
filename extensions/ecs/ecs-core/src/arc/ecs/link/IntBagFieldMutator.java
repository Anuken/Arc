package arc.ecs.link;

import arc.ecs.*;
import arc.ecs.utils.*;

import java.lang.reflect.*;

import static arc.ecs.Aspect.all;

class IntBagFieldMutator implements MultiFieldMutator<IntBag, Component>{
    private final IntBag empty = new IntBag();
    private EntitySubscription all;

    @Override
    public void validate(int sourceId, IntBag ids, LinkListener listener){
        for(int i = 0; ids.size() > i; i++){
            int id = ids.get(i);
            if(!all.getActiveEntityIds().unsafeGet(id)){
                ids.removeIndex(i--);
                if(listener != null)
                    listener.onTargetDead(sourceId, id);
            }
        }
    }

    @Override
    public IntBag read(Component c, Field f){
        try{
            final boolean isNotAccessible = !f.isAccessible();
            if(isNotAccessible){
                f.setAccessible(true);
            }
            IntBag e = (IntBag)f.get(c);
            if(isNotAccessible){
                f.setAccessible(false);
            }
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
