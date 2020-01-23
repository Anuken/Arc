package arc.ecs.fluid.generator.model.type;

import java.lang.reflect.*;
import java.util.*;

/**
 * Sourcecode generator agnostic model of class.
 * <p>
 * @author Daan van Yperen
 */
public class TypeModel{

    public String name = "unnamed";
    public String packageName = "arc.ecs";

    public List<MethodDescriptor> methods = new ArrayList<MethodDescriptor>();
    public List<FieldDescriptor> fields = new ArrayList<FieldDescriptor>();
    public Type superclass;
    public Type superinterface; // currently supports only 1 interface.

    /** Add method to model. */
    public void add(MethodDescriptor method){
        methods.add(method);
    }

    /**
     * Get method that matches signature exactly.
     * @return {@code method}, or {@code null}.
     */
    public MethodDescriptor getMethodBySignature(String signature){
        for(MethodDescriptor method : methods){
            if(signature.equals(method.signature(true, true))){
                return method;
            }
        }
        return null;
    }

    public void add(FieldDescriptor field){
        fields.add(field);
    }
}
