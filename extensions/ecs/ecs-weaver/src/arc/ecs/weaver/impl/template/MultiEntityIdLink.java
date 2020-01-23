package arc.ecs.weaver.impl.template;

import arc.ecs.*;
import arc.ecs.annotations.*;
import arc.ecs.link.*;
import arc.ecs.utils.*;

import java.lang.reflect.*;

import static arc.ecs.Aspect.all;

public class MultiEntityIdLink extends Component{
    @EntityId public IntBag field;

    public static class Mutator implements MultiFieldMutator<IntBag, MultiEntityIdLink>{
        private EntitySubscription all;

        @Override
        public void setBase(Base base){
            all = base.getAspectSubscriptionManager().get(all());
        }

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
        public IntBag read(MultiEntityIdLink c, Field f){
            return c.field;
        }
    }
}
