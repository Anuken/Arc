package arc.ecs.gradle;

import org.gradle.api.*;

/**
 * @author Daan van Yperen
 */
public class EcsGradlePlugin implements Plugin<Project>{

    @Override
    public void apply(Project target){
        target.getTasks().create("weave", EcsWeavingTask.class);
    }

}