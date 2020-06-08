package arc.struct;

import java.util.*;

/**
 * An {@link ObjectSet} that also stores keys in an {@link Seq} using the insertion order. {@link #iterator() Iteration} is
 * ordered and faster than an unordered set. Keys can also be accessed and the order changed using {@link #orderedItems()}. There
 * is some additional overhead for put and remove. When used for faster iteration versus ObjectSet and the order does not actually
 * matter, copying during remove can be greatly reduced by setting {@link Seq#ordered} to false for
 * {@link OrderedSet#orderedItems()}.
 * @author Nathan Sweet
 */

public class OrderedSet<T> extends ObjectSet<T>{
    final Seq<T> items;
    OrderedSetIterator iterator1, iterator2;

    public OrderedSet(){
        items = new Seq<>();
    }

    public OrderedSet(int initialCapacity, float loadFactor){
        super(initialCapacity, loadFactor);
        items = new Seq<>(capacity);
    }

    public OrderedSet(int initialCapacity){
        super(initialCapacity);
        items = new Seq<>(capacity);
    }

    @SuppressWarnings("unchecked")
    public OrderedSet(OrderedSet set){
        super(set);
        items = new Seq<>(capacity);
        items.addAll(set.items);
    }

    @Override
    public T first(){
        return items.first();
    }

    public boolean add(T key){
        if(!super.add(key)) return false;
        items.add(key);
        return true;
    }

    public boolean add(T key, int index){
        if(!super.add(key)){
            items.remove(key, true);
            items.insert(index, key);
            return false;
        }
        items.insert(index, key);
        return true;
    }

    public boolean remove(T key){
        if(!super.remove(key)) return false;
        items.remove(key, false);
        return true;
    }

    public T removeIndex(int index){
        T key = items.remove(index);
        super.remove(key);
        return key;
    }

    public void clear(int maximumCapacity){
        items.clear();
        super.clear(maximumCapacity);
    }

    public void clear(){
        items.clear();
        super.clear();
    }

    public Seq<T> orderedItems(){
        return items;
    }

    @Override
    public OrderedSetIterator iterator(){
        if(iterator1 == null){
            iterator1 = new OrderedSetIterator();
            iterator2 = new OrderedSetIterator();
        }

        if(iterator1.done){
            iterator1.reset();
            return iterator1;
        }

        if(iterator2.done){
            iterator2.reset();
            return iterator2;
        }

        return new OrderedSetIterator();
    }

    @Override
    public String toString(){
        if(size == 0) return "{}";
        T[] items = this.items.items;
        StringBuilder buffer = new StringBuilder(32);
        buffer.append('{');
        buffer.append(items[0]);
        for(int i = 1; i < size; i++){
            buffer.append(", ");
            buffer.append(items[i]);
        }
        buffer.append('}');
        return buffer.toString();
    }

    public String toString(String separator){
        return items.toString(separator);
    }

    public class OrderedSetIterator extends ObjectSetIterator{

        @Override
        public void reset(){
            super.reset();
            nextIndex = 0;
            hasNext = size > 0;
        }

        @Override
        public T next(){
            if(!hasNext) throw new NoSuchElementException();
            T key = items.get(nextIndex);
            nextIndex++;
            hasNext = nextIndex < size;
            return key;
        }

        @Override
        public void remove(){
            if(nextIndex < 0) throw new IllegalStateException("next must be called before remove.");
            nextIndex--;
            removeIndex(nextIndex);
        }
    }
}
