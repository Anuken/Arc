package arc.ecs.fluid.generator.strategy.components;

import arc.ecs.fluid.generator.common.*;
import arc.ecs.fluid.generator.model.artemis.*;
import arc.ecs.fluid.generator.model.type.*;

/**
 * Generate basic scaffold for SuperMapper class.
 * @author Daan van Yperen
 */
public class ComponentsBaseStrategy implements BuilderModelStrategy{

    @Override
    public void apply(ArtemisModel artemisModel, TypeModel model){
        model.name = "C";
        model.packageName = "arc.ecs";
    }
}
