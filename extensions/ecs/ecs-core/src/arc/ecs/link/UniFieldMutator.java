package arc.ecs.link;

import arc.ecs.*;

import java.lang.reflect.*;

/**
 * <p>Internal interface. Used for reading/writing entity
 * fields pointing to a single entity.</p>
 */
public interface UniFieldMutator{
    int read(Component c, Field f);

    void write(int value, Component c, Field f);

    void setBase(Base base);
}
