package arc.ecs.components;

import arc.ecs.*;
import arc.ecs.annotations.*;

/**
 * Creates a tag, local to an instance of {@link arc.ecs.io.SaveFileFormat}.
 */
@Transient
public class SerializationTag extends PooledComponent{
    public String tag;

    @Override
    protected void reset(){
        tag = null;
    }
}
