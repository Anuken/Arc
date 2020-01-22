package arc.ecs;

/**
 * Simple sequential invocation strategy.
 * @see SystemInvoker
 */
public class DefaultInvoker extends SystemInvoker{

    /**
     * Processes all systems in order.
     * <p>
     * Should guarantee artemis is in a sane state using calls to #updateEntityStates
     * before each call to a system, and after the last system has been called, or if no
     * systems have been called at all.
     */
    @Override
    protected void process(){
        BaseSystem[] systemsData = systems.getData();
        for(int i = 0, s = systems.size(); s > i; i++){
            if(disabled.get(i))
                continue;

            updateEntityStates();
            systemsData[i].process();
        }

        updateEntityStates();
    }
}
