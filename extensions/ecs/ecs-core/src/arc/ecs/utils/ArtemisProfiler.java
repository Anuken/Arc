package arc.ecs.utils;

import arc.ecs.*;
import arc.ecs.annotations.*;


/**
 * @see Profile
 */
public interface ArtemisProfiler{
    void start();
    void stop();
    void initialize(BaseSystem owner, Base base);
}