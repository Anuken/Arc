package arc.ecs.fluid.generator.common;

import arc.ecs.fluid.generator.model.artemis.*;
import arc.ecs.fluid.generator.model.type.*;

/**
 * Strategy for generating builder model from component set.
 * @author Daan van Yperen
 */
public interface BuilderModelStrategy{

    /** Apply strategy to model, generating whatever methods needed. */
    void apply(ArtemisModel artemisModel, TypeModel model);
}
