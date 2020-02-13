package arc.fx.util;

import arc.util.*;
import arc.util.pooling.*;

import java.util.*;

@SuppressWarnings("unchecked")
public class PrioritizedArray<T> implements Iterable<T>{
    private final Comparator<Wrapper<T>> comparator = Structs.comparingInt(t -> t.priority);
    private final ValueArrayMap<T, Wrapper<T>> items;
    private PrioritizedArrayIterable<T> iterable;

    public PrioritizedArray(){
        items = new ValueArrayMap<>();
    }

    public PrioritizedArray(int capacity){
        items = new ValueArrayMap<>(capacity);
    }

    public T get(int index){
        return items.getValueAt(index).item;
    }

    public void add(T item){
        add(item, 0);
    }

    public void add(T item, int priority){
        items.put(item, Wrapper.pool.obtain().initialize(item, priority));
        items.sort(comparator);
    }

    public void remove(int index){
        Wrapper<T> wrapper = items.getValueAt(index);
        remove(wrapper.item);
    }

    public void remove(T item){
        Wrapper<T> wrapper = items.remove(item);
        if(wrapper != null){
            Wrapper.pool.free(wrapper);
        }
    }

    public boolean contains(T item){
        return items.contains(item);
    }

    public void clear(){
        for(int i = 0; i < items.size(); i++){
            Wrapper<T> wrapper = items.getValueAt(i);
            Wrapper.pool.free(wrapper);
        }
        items.clear();
    }

    public int size(){
        return items.size();
    }

    public void setPriority(T item, int priority){
        items.get(item).priority = priority;
        items.sort(comparator);
    }

    /**
     * Returns an iterator for the items in the array. Remove is supported. Note that the same iterator instance is returned each
     * time this method is called. Use the ArrayIterator constructor for nested or multithreaded iteration.
     */
    @Override
    public Iterator<T> iterator(){
        if(iterable == null) iterable = new PrioritizedArrayIterable<T>(this);
        return iterable.iterator();
    }

    @Override
    public String toString(){
        return items.toString();
    }

    public String toString(String separator){
        return items.toString(separator);
    }

    private static class Wrapper<T> implements Pool.Poolable{
        private static final Pool<Wrapper> pool = new Pool<Wrapper>(){
            @Override
            protected Wrapper newObject(){
                return new Wrapper();
            }
        };

        T item;
        int priority;

        public Wrapper initialize(T item, int priority){
            this.item = item;
            this.priority = priority;
            return this;
        }

        @Override
        public void reset(){
            item = null;
            priority = 0;
        }

        @Override
        public String toString(){
            return item + "[" + priority + "]";
        }
    }

    //region Iterator implementation
    public static class PrioritizedArrayIterator<T> implements Iterator<T>, Iterable<T>{
        private final PrioritizedArray<T> array;
        private final boolean allowRemove;
        int index;
        boolean valid = true;

        public PrioritizedArrayIterator(PrioritizedArray<T> array){
            this(array, true);
        }

        public PrioritizedArrayIterator(PrioritizedArray<T> array, boolean allowRemove){
            this.array = array;
            this.allowRemove = allowRemove;
        }

        public boolean hasNext(){
            if(!valid){
                throw new ArcRuntimeException("#iterator() cannot be used nested.");
            }
            return index < array.size();
        }

        public T next(){
            if(index >= array.size()) throw new NoSuchElementException(String.valueOf(index));
            if(!valid){
                throw new ArcRuntimeException("#iterator() cannot be used nested.");
            }
            return array.items.getValueAt(index++).item;
        }

        public void remove(){
            if(!allowRemove) throw new ArcRuntimeException("Remove not allowed.");
            index--;
            array.remove(index);
        }

        public void reset(){
            index = 0;
        }

        public Iterator<T> iterator(){
            return this;
        }
    }

    public static class PrioritizedArrayIterable<T> implements Iterable<T>{
        private final PrioritizedArray<T> array;
        private final boolean allowRemove;
        private PrioritizedArray.PrioritizedArrayIterator<T> iterator1, iterator2;

        public PrioritizedArrayIterable(PrioritizedArray<T> array){
            this(array, true);
        }

        public PrioritizedArrayIterable(PrioritizedArray<T> array, boolean allowRemove){
            this.array = array;
            this.allowRemove = allowRemove;
        }

        public Iterator<T> iterator(){
            if(iterator1 == null){
                iterator1 = new PrioritizedArrayIterator<T>(array, allowRemove);
                iterator2 = new PrioritizedArrayIterator<T>(array, allowRemove);
            }
            if(!iterator1.valid){
                iterator1.index = 0;
                iterator1.valid = true;
                iterator2.valid = false;
                return iterator1;
            }
            iterator2.index = 0;
            iterator2.valid = true;
            iterator1.valid = false;
            return iterator2;
        }
    }
    //endregion
}
