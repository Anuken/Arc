package arc.util.pooling;

import arc.struct.Array;

/**
 * A {@link Pool} which keeps track of the obtained items (see {@link #obtain()}), which can be free'd all at once using the
 * {@link #flush()} method.
 * @author Xoppa
 */
public abstract class FlushablePool<T> extends Pool<T>{
    protected Array<T> obtained = new Array<>();

    public FlushablePool(){
        super();
    }

    public FlushablePool(int initialCapacity){
        super(initialCapacity);
    }

    public FlushablePool(int initialCapacity, int max){
        super(initialCapacity, max);
    }

    @Override
    public T obtain(){
        T result = super.obtain();
        obtained.add(result);
        return result;
    }

    /** Frees all obtained instances. */
    public void flush(){
        super.freeAll(obtained);
        obtained.clear();
    }

    @Override
    public void free(T object){
        obtained.remove(object, true);
        super.free(object);
    }

    @Override
    public void freeAll(Array<T> objects){
        obtained.removeAll(objects, true);
        super.freeAll(objects);
    }
}
