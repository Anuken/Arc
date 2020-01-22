package arc.ecs.gradle;

import org.gradle.api.*;

/**
 * @author Daan van Yperen
 */
public class ArtemisGradlePlugin implements Plugin<Project>{

    @Override
    public void apply(Project target){
        target.getTasks().create("weave", ArtemisWeavingTask.class);
    }

}