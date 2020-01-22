package arc.ecs.link;

import arc.ecs.*;
import arc.ecs.annotations.*;

import java.lang.reflect.*;

class MultiLinkSite extends LinkSite{
    MultiFieldMutator fieldMutator;

    protected MultiLinkSite(Base base,
                            ComponentType type,
                            Field field){

        super(base, type, field, LinkPolicy.Policy.CHECK_SOURCE);
    }

    @Override
    protected void check(int id){
        Object collection = fieldMutator.read(mapper.get(id), field);
        fieldMutator.validate(id, collection, listener);
    }

    @Override
    protected void insert(int id){
        if(listener != null)
            listener.onLinkEstablished(id, -1);
    }

    @Override
    protected void removed(int id){
        if(listener != null)
            listener.onLinkKilled(id, -1);
    }
}
