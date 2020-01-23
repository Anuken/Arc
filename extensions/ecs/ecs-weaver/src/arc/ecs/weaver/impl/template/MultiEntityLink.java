package arc.ecs.weaver.impl.template;

import arc.ecs.*;
import arc.ecs.link.*;
import arc.ecs.utils.*;

import java.lang.reflect.*;

import static arc.ecs.Aspect.all;

public class MultiEntityLink extends Component{
    public Bag<Entity> field;

    public static class Mutator implements MultiFieldMutator<Bag<Entity>, MultiEntityLink>{
        private EntitySubscription all;

        @Override
        public void setBase(Base base){
            all = base.getAspectSubscriptionManager().get(all());
        }

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
        public Bag<Entity> read(MultiEntityLink c, Field f){
            return c.field;
        }
    }
}
