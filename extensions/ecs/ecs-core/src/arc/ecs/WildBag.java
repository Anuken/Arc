package arc.ecs;

import arc.ecs.utils.*;


/**
 * Let the user set the size.
 * <p>
 * Setting the size does not resize the bag, nor will it clean up contents
 * beyond the given size. Only use this if you know what you are doing!
 * </p>
 * @param <T> object type this bag holds
 * @author junkdog
 */
class WildBag<T> extends Bag<T>{


    public WildBag(Class<T> type){
        super(type);
    }

    /**
     * Set the size.
     * <p>
     * This will not resize the bag, nor will it clean up contents beyond the
     * given size. Use with caution.
     * </p>
     * @param size the size to set
     */
    void setSize(int size){
        this.size = size;
    }

}
