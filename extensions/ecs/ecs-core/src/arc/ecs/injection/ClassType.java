package arc.ecs.injection;

import arc.ecs.*;

/**
 * Enum used to cache class type according to their usage in Artemis.
 * @author Snorre E. Brekke
 */
public enum ClassType{
    /**
     * Used for (sub)classes of {@link Mapper}
     */
    MAPPER,
    /**
     * Used for (sub)classes of {@link BaseSystem}
     */
    SYSTEM,
    /**
     * Used for (sub)classes of {@link Base}
     */
    BASE,
    /**
     * Used for everything else.
     */
    CUSTOM
}
