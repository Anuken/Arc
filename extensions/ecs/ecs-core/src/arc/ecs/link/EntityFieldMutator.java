package arc.ecs.link;

import arc.ecs.*;

import java.lang.reflect.*;

class EntityFieldMutator implements UniFieldMutator{
    private World world;

    @Override
    public int read(Component c, Field f){
        try{
            Entity e = (Entity)f.get(c);
            return (e != null) ? e.getId() : -1;
        }catch(Exception exc){
            throw new RuntimeException(exc);
        }
    }

    @Override
    public void write(int value, Component c, Field f){
        try{
            Entity e = (value != -1) ? world.getEntity(value) : null;
            f.set(c, e);
        }catch(Exception exc){
            throw new RuntimeException(exc);
        }
    }

    @Override
    public void setWorld(World world){
        this.world = world;
    }
}
