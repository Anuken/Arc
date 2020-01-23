package arc.ecs.link;

import arc.ecs.*;

import java.lang.reflect.*;

/**
 * <p>Internal interface. Used for reading/writing entity
 * fields pointing to multiple entities.</p>
 */
public interface MultiFieldMutator<T, C extends Component>{
    void validate(int sourceId, T collection, LinkListener listener);

    T read(C c, Field f);

    void setBase(Base base);
}
