package arc.ecs.fluid.generator.strategy.e;

import arc.ecs.fluid.generator.common.*;
import arc.ecs.fluid.generator.model.artemis.*;
import arc.ecs.fluid.generator.model.type.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * Adds methods to interact with component fields.
 * @author Daan van Yperen
 * @see DefaultFieldProxyStrategy for default getter/setter logic.
 */
public class ComponentFieldAccessorStrategy extends IterativeModelStrategy{

    private Collection<FieldProxyStrategy> fieldProxyStrategies;

    @Override
    public void apply(ArtemisModel artemisModel, TypeModel model){
        fieldProxyStrategies = artemisModel.fieldProxyStrategies;
        super.apply(artemisModel, model);
    }

    @Override
    protected void apply(ComponentDescriptor component, TypeModel model){
        final Set<Field> fields = component.getAllPublicFields();
        exposeFields(component, model, fields);
    }

    private void exposeFields(ComponentDescriptor component, TypeModel model, Set<Field> fields){
        for(Field field : fields){
            for(FieldProxyStrategy fieldProxyStrategy : fieldProxyStrategies){
                if(fieldProxyStrategy.matches(component, field, model)){
                    fieldProxyStrategy.execute(component, field, model);
                    break;
                }
            }

        }
    }

}
