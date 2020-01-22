package arc.ecs.weaver.weaver.template;

import arc.ecs.*;
import arc.ecs.link.*;

import java.lang.reflect.*;

public class UniEntityLink extends Component{
    public Entity field;

    public static class Mutator implements UniFieldMutator{
        private World world;

        @Override
        public int read(Component c, Field f){
            Entity e = ((UniEntityLink)c).field;
            return (e != null) ? e.getId() : -1;
        }

        @Override
        public void write(int value, Component c, Field f){
            Entity e = (value != -1) ? world.getEntity(value) : null;
            ((UniEntityLink)c).field = e;
        }

        @Override
        public void setWorld(World world){
            this.world = world;
        }
    }
}
