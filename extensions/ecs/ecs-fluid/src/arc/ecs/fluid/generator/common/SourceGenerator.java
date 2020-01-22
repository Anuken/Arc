package arc.ecs.fluid.generator.common;

import arc.ecs.fluid.generator.model.type.*;

/**
 * Convert agnostic class model to java source.
 * @author Daan van Yperen
 */
public interface SourceGenerator{
    void generate(TypeModel model, Appendable out);
}
