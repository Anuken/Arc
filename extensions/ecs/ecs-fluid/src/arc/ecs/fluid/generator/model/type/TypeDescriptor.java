package arc.ecs.fluid.generator.model.type;

import java.lang.reflect.*;

/**
 * Fake type.
 * <p>
 * Bit of a hack, we need this to refer to types that have not been generated yet, like SuperMapper and E.
 * @author Daan van Yperen
 * @todo is there an idiomatic solution?
 */
public class TypeDescriptor implements Type{

    private String name;

    public TypeDescriptor(String name){
        this.name = name;
    }

    @Override
    public String toString(){
        return name;
    }
}
