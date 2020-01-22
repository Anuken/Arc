package arc.ecs.injection;

/**
 * <p>Enum used by {@link ClassType} to indicate if a class or field is annotated
 * with {@link arc.ecs.annotations.Wire}.</p>
 * @author Snorre E. Brekke
 */
public enum WireType{
    /**
     * Indicates that a class is (implicitly or explicitly) annotated
     * with {@link arc.ecs.annotations.Wire}.
     */
    WIRE,
    /**
     * Indicates that a class is not annotated with anything relevant to the artemis-world.
     */
    IGNORED,
    /**
     * Indicates that a class is annotated with {@link arc.ecs.annotations.SkipWire}
     */
    SKIPWIRE
}
