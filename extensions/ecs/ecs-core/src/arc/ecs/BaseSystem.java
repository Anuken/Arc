package arc.ecs;

import arc.ecs.annotations.*;

/**
 * Most basic system.
 * <p>
 * Upon calling world.process(), your systems are processed in sequence.
 * <p>
 * Flow:
 * {@link #initialize()} - Initialize your system, on top of the dependency injection.
 * {@link #begin()} - Called before the entities are processed.
 * {@link #processSystem()} - Called once per cycle.
 * {@link #end()} - Called after the entities have been processed.
 * @see arc.ecs.annotations.Wire
 */
public abstract class BaseSystem{
    /** The world this system belongs to. */
    @SkipWire
    protected Base base;

    public BaseSystem(){
    }

    /**
     * Called before system processing begins.
     * <p>
     * <b>Nota Bene:</b> Any entities created in this method
     * won't become active until the next system starts processing
     * or when a new processing rounds begins, whichever comes first.
     * </p>
     */
    protected void begin(){
    }

    /**
     * Process system.
     * <p>
     * Does nothing if {@link #checkProcessing()} is false or the system
     * is disabled.
     * @see DefaultInvoker
     */
    public final void process(){
        if(checkProcessing()){
            begin();
            processSystem();
            end();
        }
    }

    /**
     * Process the system.
     */
    protected abstract void processSystem();

    /**
     * Called after the systems has finished processing.
     */
    protected void end(){
    }

    /**
     * Does the system desire processing.
     * <p>
     * Useful when the system is enabled, but only occasionally
     * needs to process.
     * <p>
     * This only affects processing, and does not affect events
     * or subscription lists.
     * @return true if the system should be processed, false if not.
     * @see #isEnabled() both must be true before the system will process.
     */
    @SuppressWarnings("static-method")
    protected boolean checkProcessing(){
        return true;
    }

    /**
     * Override to implement code that gets executed when systems are
     * initialized.
     * <p>
     * Note that artemis native types like systems, factories and
     * component mappers are automatically injected by artemis.
     */
    protected void initialize(){
    }

    /**
     * Check if the system is enabled.
     * @return {@code true} if enabled, otherwise false
     */
    public boolean isEnabled(){
        return base.invocationStrategy.isEnabled(this);
    }

    /**
     * Enabled systems run during {@link #process()}.
     * <p>
     * This only affects processing, and does not affect events
     * or subscription lists.
     * <p>
     * Systems are enabled by default.
     * @param enabled system will not run when set to false
     * @see #checkProcessing() both must be true before the system will process.
     */
    public void setEnabled(boolean enabled){
        base.invocationStrategy.setEnabled(this, enabled);
    }

    /**
     * Set the world this system works on.
     * @param base the world to set
     */
    protected void setBase(Base base){
        this.base = base;
    }

    /**
     * Get the world associated with the manager.
     * @return the associated world
     */
    protected Base getBase(){
        return base;
    }

    /**
     * see {@link Base#dispose()}
     */
    protected void dispose(){
    }
}
