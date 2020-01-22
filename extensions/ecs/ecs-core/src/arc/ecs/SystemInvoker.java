package arc.ecs;

import arc.ecs.utils.*;

/**
 * Delegate for system invocation.
 * <p>
 * Maybe you want to more granular control over system invocations, feed certain systems different deltas,
 * or completely rewrite processing in favor of events. Extending this class allows you to write your own
 * logic for processing system invocation.
 * <p>
 * Register it with {@link BaseConfigBuilder#register(SystemInvoker)}
 * <p>
 * Be sure to call {@link #updateEntityStates()} after the world dies.
 * @see DefaultInvoker for the default strategy.
 */
public abstract class SystemInvoker{
    /** World to operate on. */
    protected Base base;
    protected final BitVector disabled = new BitVector();
    protected Bag<BaseSystem> systems;

    /** World to operate on. */
    protected final void setBase(Base base){
        this.base = base;
    }

    /**
     * Called prior to {@link #initialize()}
     */
    protected void setSystems(Bag<BaseSystem> systems){
        this.systems = systems;
    }

    /** Called during world initialization phase. */
    protected void initialize(){
    }

    /** Call to inform all systems and subscription of world state changes. */
    protected final void updateEntityStates(){
        base.batchProcessor.update();
    }

    protected abstract void process();

    public boolean isEnabled(BaseSystem system){
        Class<? extends BaseSystem> target = system.getClass();
        ImmutableBag<BaseSystem> systems = base.getSystems();
        for(int i = 0; i < systems.size(); i++){
            if(target == systems.get(i).getClass())
                return !disabled.get(i);
        }

        throw new RuntimeException("huh?");
    }

    public void setEnabled(BaseSystem system, boolean value){
        Class<? extends BaseSystem> target = system.getClass();
        ImmutableBag<BaseSystem> systems = base.getSystems();
        for(int i = 0; i < systems.size(); i++){
            if(target == systems.get(i).getClass())
                disabled.set(i, !value);
        }
    }
}
