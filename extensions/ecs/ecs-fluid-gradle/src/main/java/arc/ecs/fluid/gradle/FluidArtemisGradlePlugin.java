package arc.ecs.fluid.gradle;

import org.gradle.api.*;

/**
 * @author Daan van Yperen
 */
public class FluidArtemisGradlePlugin implements Plugin<Project>{

    @Override
    public void apply(Project target){
        target.getTasks().create("fluid", FluidApiGenerationTask.class);
    }

}