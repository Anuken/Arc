package arc.struct;

/** Tiny array wrapper with a mask int for fast contains() checks. */
public class EnumSet<T extends Enum<T>>{
    private int mask;

    /** Array, for iterating over. Do not change. */
    public T[] array;
    public int size;

    EnumSet(){
    }

    EnumSet(int size){
        this.size = size;
    }

    public static <T extends Enum<T>> EnumSet<T> of(T... arr){
        EnumSet<T> set = new EnumSet<>(arr.length);
        set.array = arr;
        for(T t : arr){
            set.mask |= (1 << t.ordinal());
        }
        return set;
    }

    public boolean contains(T t){
        return (mask & (1 << t.ordinal())) != 0;
    }
}
