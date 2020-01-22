package arc.ecs.fluid.generator.model.artemis;

import arc.ecs.fluid.generator.strategy.e.*;

import java.util.*;

/**
 * Describes the known artemis universe and generator plugins.
 * @author Daan van Yperen
 */
public class ArtemisModel{
    public Collection<ComponentDescriptor> components;
    public Collection<FieldProxyStrategy> fieldProxyStrategies;

    public ArtemisModel(Collection<ComponentDescriptor> components, Collection<FieldProxyStrategy> fieldProxyStrategies){
        this.components = components;
        this.fieldProxyStrategies = fieldProxyStrategies;
    }
}
