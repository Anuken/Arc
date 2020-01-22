package arc.ecs.link;

import arc.ecs.*;

import java.lang.reflect.*;

class IntFieldMutator implements UniFieldMutator{
    @Override
    public int read(Component c, Field f){
        try{
            return (Integer)f.get(c);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(int value, Component c, Field f){
        try{
            f.set(c, value);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setBase(Base base){
    }
}
