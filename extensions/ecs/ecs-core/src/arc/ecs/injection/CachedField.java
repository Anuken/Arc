package arc.ecs.injection;

import java.lang.reflect.*;

/**
 * Provides cached information about a class-field, limiting the need for reflection
 * on repeated access.
 * This class only caches the state related to the {@link arc.ecs.annotations.Wire} annotation of the field.
 * <p>
 * CachedField is typically managed by {@link InjectionCache},
 * and can be retrieved with {@link InjectionCache#getCachedField(Field)}.
 * </p>
 * @author Snorre E. Brekke
 */
public class CachedField{
    public CachedField(Field field, WireType wireType, String name, boolean failOnNull){
        this.field = field;
        this.wireType = wireType;
        this.name = name;
        this.failOnNull = failOnNull;
    }

    /**
     * The field this class represents.
     */
    public final Field field;

    /**
     * {@link WireType#WIRE} if the field is annotated with {@link arc.ecs.annotations.Wire},
     * {@link WireType#SKIPWIRE} if the field is annotated with {@link arc.ecs.annotations.SkipWire),
     * {@link WireType#IGNORED} otherwise.
     */
    public final WireType wireType;

    /**
     * If the field is annotated with {@link arc.ecs.annotations.Wire}, this will contain the cached value of
     * {@link arc.ecs.annotations.Wire#name()}.
     */
    public final String name;


    public final boolean failOnNull;
}
