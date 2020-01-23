package arc.ecs;


import arc.ecs.injection.*;

import java.lang.reflect.*;

/**
 * Plugin to enable fluid entity functionality in your world.
 *
 * - Enables fluid entity functionality.
 * - Adds support for {@code arc.ecs.ESubscription} field dependency injection.
 *   (when annotated with {@code @arc.ecs.annotations.All}, {@code @arc.ecs.annotations.One} and/or {@code @arc.ecs.annotations.Exclude})
 * <p>
 * This file is generated.
 * <p>
 * For artemis-odb developers: Make sure you edit the file in  artemis-fluid-core-utility-sources, and not a
 * generated-sources version.
 */
public final class FluidEntityPlugin implements ArtemisPlugin{
    public void setup(BaseConfigBuilder b) {
        b.dependsOn(BaseConfigBuilder.Priority.HIGH, SuperMapper.class);
        b.register(new ESubscriptionAspectResolver());
    }

    /**
     *  Resolver with support for ESubscription.
     */
    private static class ESubscriptionAspectResolver implements FieldResolver{

        // we need to delegate to the aspect field resolver.
        private AspectFieldResolver aspectFieldResolver = new AspectFieldResolver();

        @Override
        public void initialize(Base world) {
            aspectFieldResolver.initialize(world);
        }

        @Override
        public Object resolve(Object target, Class<?> fieldType, Field field) {
            if (ESubscription.class == fieldType) {
                return new ESubscription(((EntitySubscription) aspectFieldResolver.resolve(target, EntitySubscription.class, field)));
            }
            return null;
        }
    }
}
