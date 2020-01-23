package arc.ecs.weaver.impl.template;

import arc.ecs.*;
import arc.ecs.annotations.*;
import arc.ecs.link.*;

import java.lang.reflect.*;

public class UniEntityIdLink extends Component{
    @EntityId public int field;

    public static class Mutator implements UniFieldMutator{
        @Override
        public int read(Component c, Field f){
            return ((UniEntityIdLink)c).field;
        }

        @Override
        public void write(int value, Component c, Field f){
            ((UniEntityIdLink)c).field = value;
        }

        @Override
        public void setBase(Base base){
        }
    }
}
