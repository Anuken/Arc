package arc.ecs.fluid.generator.model;

import arc.ecs.*;
import arc.ecs.fluid.generator.model.type.*;

import java.lang.reflect.*;

/**
 * @author Daan van Yperen
 */
public class FluidTypes{
    public static final Type SUPERMAPPER_TYPE = new TypeDescriptor("arc.ecs.SuperMapper");
    public static final Type E_TYPE = new TypeDescriptor("arc.ecs.E");
    public static final Type EBAG_TYPE = new TypeDescriptor("arc.ecs.EBag");
    public static final Type EXTENDS_COMPONENT_TYPE = new WildcardType(){
        @Override
        public Type[] getUpperBounds(){
            return new Type[]{Component.class};
        }

        @Override
        public Type[] getLowerBounds(){
            return new Type[0];
        }
    };
}
