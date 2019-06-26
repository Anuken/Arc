package io.anuke.arc.collection;

import io.anuke.arc.function.*;
import io.anuke.arc.math.Mathf;
import io.anuke.arc.util.*;
import io.anuke.arc.util.reflect.ArrayReflection;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A resizable, ordered or unordered array of objects. If unordered, this class avoids a memory copy when removing elements (the
 * last element is moved to the removed element's position).
 * @author Nathan Sweet
 */
@SuppressWarnings("unchecked")
public class Array<T> implements Iterable<T>{
    /**
     * Provides direct access to the underlying array. If the Array's generic type is not Object, this field may only be accessed
     * if the {@link Array#Array(boolean, int, Class)} constructor was used.
     */
    public T[] items;

    public int size;
    public boolean ordered;

    private ArrayIterable iterable;

    /** Creates an ordered array with a capacity of 16. */
    public Array(){
        this(true, 16);
    }

    /** Creates an ordered array with the specified capacity. */
    public Array(int capacity){
        this(true, capacity);
    }

    /**
     * @param ordered If false, methods that remove elements may change the order of other elements in the array, which avoids a
     * memory copy.
     * @param capacity Any elements added beyond this will cause the backing array to be grown.
     */
    public Array(boolean ordered, int capacity){
        this.ordered = ordered;
        items = (T[])new Object[capacity];
    }

    /**
     * Creates a new array with {@link #items} of the specified type.
     * @param ordered If false, methods that remove elements may change the order of other elements in the array, which avoids a
     * memory copy.
     * @param capacity Any elements added beyond this will cause the backing array to be grown.
     */
    public Array(boolean ordered, int capacity, Class arrayType){
        this.ordered = ordered;
        items = (T[])ArrayReflection.newInstance(arrayType, capacity);
    }

    /** Creates an ordered array with {@link #items} of the specified type and a capacity of 16. */
    public Array(Class arrayType){
        this(true, 16, arrayType);
    }

    /**
     * Creates a new array containing the elements in the specified array. The new array will have the same type of backing array
     * and will be ordered if the specified array is ordered. The capacity is set to the number of elements, so any subsequent
     * elements added will cause the backing array to be grown.
     */
    public Array(Array<? extends T> array){
        this(array.ordered, array.size, array.items.getClass().getComponentType());
        size = array.size;
        System.arraycopy(array.items, 0, items, 0, size);
    }

    /**
     * Creates a new ordered array containing the elements in the specified array. The new array will have the same type of
     * backing array. The capacity is set to the number of elements, so any subsequent elements added will cause the backing array
     * to be grown.
     */
    public Array(T[] array){
        this(true, array, 0, array.length);
    }

    /**
     * Creates a new array containing the elements in the specified array. The new array will have the same type of backing array.
     * The capacity is set to the number of elements, so any subsequent elements added will cause the backing array to be grown.
     * @param ordered If false, methods that remove elements may change the order of other elements in the array, which avoids a
     * memory copy.
     */
    public Array(boolean ordered, T[] array, int start, int count){
        this(ordered, count, array.getClass().getComponentType());
        size = count;
        System.arraycopy(array, start, items, 0, size);
    }

    /** @see #Array(Class) */
    public static <T> Array<T> of(Class<T> arrayType){
        return new Array<>(arrayType);
    }

    /** @see #Array(boolean, int, Class) */
    public static <T> Array<T> of(boolean ordered, int capacity, Class<T> arrayType){
        return new Array<>(ordered, capacity, arrayType);
    }

    public static <T> Array<T> withRecursive(Object... arrays){
        Array<T> result = new Array<>();
        for(Object a : arrays){
            if(a instanceof Array){
                result.addAll((Array<? extends T>) a);
            }else{
                result.add((T)a);
            }
        }
        return result;
    }

    /** @see #Array(Object[]) */
    public static <T> Array<T> with(T... array){
        return new Array<>(array);
    }

    public static <T> Array<T> with(Iterable<T> array){
        Array<T> out = new Array<>();
        for(T thing : array){
            out.add((T)thing);
        }
        return out;
    }

    /** @see #Array(Object[]) */
    public static <T> Array<T> select(T[] array, Predicate<T> test){
        Array<T> out = new Array<>(array.length);
        for(int i = 0; i < array.length; i++){
            if(test.test(array[i])){
                out.add(array[i]);
            }
        }
        return out;
    }

    public float sum(FloatFunction<T> summer){
        float sum = 0;
        for(int i = 0; i < size; i++){
            sum += summer.get(items[i]);
        }
        return sum;
    }

    public <E extends T> void each(Predicate<? super T> pred, Consumer<E> consumer){
        for(int i = 0; i < size; i++){
            if(pred.test(items[i])) consumer.accept((E)items[i]);
        }
    }

    public void each(Consumer<? super T> consumer){
        for(int i = 0; i < size; i++){
            consumer.accept(items[i]);
        }
    }

    /**Replaces values without creating a new array.*/
    public void replace(Function<T, T> mapper){
        for(int i = 0; i < size; i++){
            items[i] = mapper.get(items[i]);
        }
    }

    /**Returns a new array with the mapped values.*/
    public <R> Array<R> map(Function<T, R> mapper){
        Array<R> arr = new Array<>(size);
        for(int i = 0; i < size; i++){
            arr.add(mapper.get(items[i]));
        }
        return arr;
    }

    /**Returns a new int array with the mapped values.*/
    public IntArray mapInt(IntFunction<T> mapper){
        IntArray arr = new IntArray(size);
        for(int i = 0; i < size; i++){
            arr.add(mapper.get(items[i]));
        }
        return arr;
    }

    public <R> R reduce(R initial, BiFunction<T, R, R> reducer){
        R result = initial;
        for(int i = 0; i < size; i++){
            result = reducer.get(items[i], result);
        }
        return result;
    }

    public boolean contains(Predicate<T> predicate){
        return find(predicate) != null;
    }

    public T min(FloatFunction<T> func){
        T result = null;
        float min = Float.MAX_VALUE;
        for(int i = 0; i < size; i++){
            T t = items[i];
            float val = func.get(t);
            if(val <= min){
                result = t;
                min = val;
            }
        }
        return result;
    }

    public T max(FloatFunction<T> func){
        T result = null;
        float max = Float.NEGATIVE_INFINITY;
        for(int i = 0; i < size; i++){
            T t = items[i];
            float val = func.get(t);
            if(val >= max){
                result = t;
                max = val;
            }
        }
        return result;
    }

    public T find(Predicate<T> predicate){
        for(int i = 0; i < size; i++){
            if(predicate.test(items[i])){
                return items[i];
            }
        }
        return null;
    }

    public void add(T value){
        T[] items = this.items;
        if(size == items.length) items = resize(Math.max(8, (int)(size * 1.75f)));
        items[size++] = value;
    }

    public void add(T value1, T value2){
        T[] items = this.items;
        if(size + 1 >= items.length) items = resize(Math.max(8, (int)(size * 1.75f)));
        items[size] = value1;
        items[size + 1] = value2;
        size += 2;
    }

    public void add(T value1, T value2, T value3){
        T[] items = this.items;
        if(size + 2 >= items.length) items = resize(Math.max(8, (int)(size * 1.75f)));
        items[size] = value1;
        items[size + 1] = value2;
        items[size + 2] = value3;
        size += 3;
    }

    public void add(T value1, T value2, T value3, T value4){
        T[] items = this.items;
        if(size + 3 >= items.length) items = resize(Math.max(8, (int)(size * 1.8f))); // 1.75 isn't enough when size=5.
        items[size] = value1;
        items[size + 1] = value2;
        items[size + 2] = value3;
        items[size + 3] = value4;
        size += 4;
    }

    public void addAll(Array<? extends T> array){
        addAll(array.items, 0, array.size);
    }

    public void addAll(Array<? extends T> array, int start, int count){
        if(start + count > array.size)
            throw new IllegalArgumentException("start + count must be <= size: " + start + " + " + count + " <= " + array.size);
        addAll(array.items, start, count);
    }

    public void addAll(T... array){
        addAll(array, 0, array.length);
    }

    public void addAll(T[] array, int start, int count){
        T[] items = this.items;
        int sizeNeeded = size + count;
        if(sizeNeeded > items.length) items = resize(Math.max(8, (int)(sizeNeeded * 1.75f)));
        System.arraycopy(array, start, items, size, count);
        size += count;
    }

    public void addAll(Iterable<? extends T> items){
        for(T t : items){
            add(t);
        }
    }

    /** Sets this array's contents to the specified array.*/
    public void set(Array<? extends T> array){
        clear();
        addAll(array);
    }

    public T get(int index){
        if(index >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
        return items[index];
    }

    public void set(int index, T value){
        if(index >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
        items[index] = value;
    }

    public void insert(int index, T value){
        if(index > size) throw new IndexOutOfBoundsException("index can't be > size: " + index + " > " + size);
        T[] items = this.items;
        if(size == items.length) items = resize(Math.max(8, (int)(size * 1.75f)));
        if(ordered)
            System.arraycopy(items, index, items, index + 1, size - index);
        else
            items[size] = items[index];
        size++;
        items[index] = value;
    }

    public void swap(int first, int second){
        if(first >= size) throw new IndexOutOfBoundsException("first can't be >= size: " + first + " >= " + size);
        if(second >= size) throw new IndexOutOfBoundsException("second can't be >= size: " + second + " >= " + size);
        T[] items = this.items;
        T firstValue = items[first];
        items[first] = items[second];
        items[second] = firstValue;
    }

    public boolean contains(T value){
        return contains(value, false);
    }

    /**
     * Returns if this array contains value.
     * @param value May be null.
     * @param identity If true, == comparison will be used. If false, .equals() comparison will be used.
     * @return true if array contains value, false if it doesn't
     */
    public boolean contains(T value, boolean identity){
        T[] items = this.items;
        int i = size - 1;
        if(identity || value == null){
            while(i >= 0)
                if(items[i--] == value) return true;
        }else{
            while(i >= 0)
                if(value.equals(items[i--])) return true;
        }
        return false;
    }

    public int indexOf(T value){
        return indexOf(value, false);
    }

    /**
     * Returns the index of first occurrence of value in the array, or -1 if no such value exists.
     * @param value May be null.
     * @param identity If true, == comparison will be used. If false, .equals() comparison will be used.
     * @return An index of first occurrence of value in array or -1 if no such value exists
     */
    public int indexOf(T value, boolean identity){
        T[] items = this.items;
        if(identity || value == null){
            for(int i = 0, n = size; i < n; i++)
                if(items[i] == value) return i;
        }else{
            for(int i = 0, n = size; i < n; i++)
                if(value.equals(items[i])) return i;
        }
        return -1;
    }

    /**
     * Returns an index of last occurrence of value in array or -1 if no such value exists. Search is started from the end of an
     * array.
     * @param value May be null.
     * @param identity If true, == comparison will be used. If false, .equals() comparison will be used.
     * @return An index of last occurrence of value in array or -1 if no such value exists
     */
    public int lastIndexOf(T value, boolean identity){
        T[] items = this.items;
        if(identity || value == null){
            for(int i = size - 1; i >= 0; i--)
                if(items[i] == value) return i;
        }else{
            for(int i = size - 1; i >= 0; i--)
                if(value.equals(items[i])) return i;
        }
        return -1;
    }

    /** Removes a value, without using identity. */
    public boolean remove(T value){
        return removeValue(value, false);
    }

    /** Removes a single value by predicate. */
    public boolean remove(Predicate<T> value){
        for(int i = 0; i < size; i++){
            if(value.test(items[i])){
                remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the first instance of the specified value in the array.
     * @param value May be null.
     * @param identity If true, == comparison will be used. If false, .equals() comparison will be used.
     * @return true if value was found and removed, false otherwise
     */
    public boolean removeValue(T value, boolean identity){
        T[] items = this.items;
        if(identity || value == null){
            for(int i = 0, n = size; i < n; i++){
                if(items[i] == value){
                    remove(i);
                    return true;
                }
            }
        }else{
            for(int i = 0, n = size; i < n; i++){
                if(value.equals(items[i])){
                    remove(i);
                    return true;
                }
            }
        }
        return false;
    }

    /** Removes and returns the item at the specified index. */
    public T remove(int index){
        if(index >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
        T[] items = this.items;
        T value = items[index];
        size--;
        if(ordered)
            System.arraycopy(items, index + 1, items, index, size - index);
        else
            items[index] = items[size];
        items[size] = null;
        return value;
    }

    /** Removes the items between the specified indices, inclusive. */
    public void removeRange(int start, int end){
        if(end >= size) throw new IndexOutOfBoundsException("end can't be >= size: " + end + " >= " + size);
        if(start > end) throw new IndexOutOfBoundsException("start can't be > end: " + start + " > " + end);
        T[] items = this.items;
        int count = end - start + 1;
        if(ordered)
            System.arraycopy(items, start + count, items, start, size - (start + count));
        else{
            int lastIndex = this.size - 1;
            for(int i = 0; i < count; i++)
                items[start + i] = items[lastIndex - i];
        }
        size -= count;
    }

    public boolean removeAll(Array<? extends T> array){
        return removeAll(array, false);
    }

    /**
     * Removes from this array all of elements contained in the specified array.
     * @param identity True to use ==, false to use .equals().
     * @return true if this array was modified.
     */
    public boolean removeAll(Array<? extends T> array, boolean identity){
        int size = this.size;
        int startSize = size;
        T[] items = this.items;
        if(identity){
            for(int i = 0, n = array.size; i < n; i++){
                T item = array.get(i);
                for(int ii = 0; ii < size; ii++){
                    if(item == items[ii]){
                        remove(ii);
                        size--;
                        break;
                    }
                }
            }
        }else{
            for(int i = 0, n = array.size; i < n; i++){
                T item = array.get(i);
                for(int ii = 0; ii < size; ii++){
                    if(item.equals(items[ii])){
                        remove(ii);
                        size--;
                        break;
                    }
                }
            }
        }
        return size != startSize;
    }

    /** Removes and returns the last item. */
    public T pop(){
        if(size == 0) throw new IllegalStateException("Array is empty.");
        --size;
        T item = items[size];
        items[size] = null;
        return item;
    }

    /** Returns the last item. */
    public T peek(){
        if(size == 0) throw new IllegalStateException("Array is empty.");
        return items[size - 1];
    }

    /** Returns the first item. */
    public T first(){
        if(size == 0) throw new IllegalStateException("Array is empty.");
        return items[0];
    }

    /** Returns true if the array is empty. */
    public boolean isEmpty(){
        return size == 0;
    }

    public void clear(){
        T[] items = this.items;
        for(int i = 0, n = size; i < n; i++)
            items[i] = null;
        size = 0;
    }

    /**
     * Reduces the size of the backing array to the size of the actual items. This is useful to release memory when many items
     * have been removed, or if it is known that more items will not be added.
     * @return {@link #items}
     */
    public T[] shrink(){
        if(items.length != size) resize(size);
        return items;
    }

    /**
     * Increases the size of the backing array to accommodate the specified number of additional items. Useful before adding many
     * items to avoid multiple backing array resizes.
     * @return {@link #items}
     */
    public T[] ensureCapacity(int additionalCapacity){
        if(additionalCapacity < 0)
            throw new IllegalArgumentException("additionalCapacity must be >= 0: " + additionalCapacity);
        int sizeNeeded = size + additionalCapacity;
        if(sizeNeeded > items.length) resize(Math.max(8, sizeNeeded));
        return items;
    }

    /**
     * Sets the array size, leaving any values beyond the current size null.
     * @return {@link #items}
     */
    public T[] setSize(int newSize){
        truncate(newSize);
        if(newSize > items.length) resize(Math.max(8, newSize));
        size = newSize;
        return items;
    }

    /** Creates a new backing array with the specified size containing the current items. */
    protected T[] resize(int newSize){
        T[] items = this.items;
        T[] newItems = (T[])ArrayReflection.newInstance(items.getClass().getComponentType(), newSize);
        System.arraycopy(items, 0, newItems, 0, Math.min(size, newItems.length));
        this.items = newItems;
        return newItems;
    }

    /**
     * Sorts this array. The array elements must implement {@link Comparable}. This method is not thread safe (uses
     * {@link Sort#instance()}).
     */
    public void sort(){
        Sort.instance().sort(items, 0, size);
    }

    /** Sorts the array. This method is not thread safe (uses {@link Sort#instance()}). */
    public void sort(Comparator<? super T> comparator){
        Sort.instance().sort(items, comparator, 0, size);
    }

    public Array<T> selectFrom(Array<T> base, Predicate<T> predicate){
        clear();
        base.each(t -> {
            if(predicate.test(t)){
                add(t);
            }
        });
        return this;
    }

    /** Allocates a new array with all elements that match the predicate.*/
    public Array<T> select(Predicate<T> predicate){
        Array<T> arr = new Array<>();
        for(int i = 0; i < size; i++){
            if(predicate.test(items[i])){
                arr.add(items[i]);
            }
        }
        return arr;
    }

    public int count(Predicate<T> predicate){
        int count = 0;
        for(int i = 0; i < size; i++){
            if(predicate.test(items[i])){
                count ++;
            }
        }
        return count;
    }

    /**
     * Selects the nth-lowest element from the Array according to Comparator ranking. This might partially sort the Array. The
     * array must have a size greater than 0, or a {@link ArcRuntimeException} will be thrown.
     * @param comparator used for comparison
     * @param kthLowest rank of desired object according to comparison, n is based on ordinal numbers, not array indices. for min
     * value use 1, for max value use size of array, using 0 results in runtime exception.
     * @return the value of the Nth lowest ranked object.
     * @see Select
     */
    public T selectRanked(Comparator<T> comparator, int kthLowest){
        if(kthLowest < 1){
            throw new ArcRuntimeException("nth_lowest must be greater than 0, 1 = first, 2 = second...");
        }
        return Select.instance().select(items, comparator, kthLowest, size);
    }

    /**
     * @param comparator used for comparison
     * @param kthLowest rank of desired object according to comparison, n is based on ordinal numbers, not array indices. for min
     * value use 1, for max value use size of array, using 0 results in runtime exception.
     * @return the index of the Nth lowest ranked object.
     * @see Array#selectRanked(java.util.Comparator, int)
     */
    public int selectRankedIndex(Comparator<T> comparator, int kthLowest){
        if(kthLowest < 1){
            throw new ArcRuntimeException("nth_lowest must be greater than 0, 1 = first, 2 = second...");
        }
        return Select.instance().selectIndex(items, comparator, kthLowest, size);
    }

    public void reverse(){
        T[] items = this.items;
        for(int i = 0, lastIndex = size - 1, n = size / 2; i < n; i++){
            int ii = lastIndex - i;
            T temp = items[i];
            items[i] = items[ii];
            items[ii] = temp;
        }
    }

    public void shuffle(){
        T[] items = this.items;
        for(int i = size - 1; i >= 0; i--){
            int ii = Mathf.random(i);
            T temp = items[i];
            items[i] = items[ii];
            items[ii] = temp;
        }
    }

    /**
     * Returns an iterator for the items in the array. Remove is supported. Note that the same iterator instance is returned each
     * time this method is called. Use the {@link ArrayIterator} constructor for nested or multithreaded iteration.
     */
    @Override
    public Iterator<T> iterator(){
        if(iterable == null) iterable = new ArrayIterable(this);
        return iterable.iterator();
    }

    /**
     * Reduces the size of the array to the specified size. If the array is already smaller than the specified size, no action is
     * taken.
     */
    public void truncate(int newSize){
        if(newSize < 0) throw new IllegalArgumentException("newSize must be >= 0: " + newSize);
        if(size <= newSize) return;
        for(int i = newSize; i < size; i++)
            items[i] = null;
        size = newSize;
    }

    /** Returns a random item from the array, or null if the array is empty. */
    public T random(){
        if(size == 0) return null;
        return items[Mathf.random(0, size - 1)];
    }

    /**
     * Returns the items as an array. Note the array is typed, so the {@link #Array(Class)} constructor must have been used.
     * Otherwise use {@link #toArray(Class)} to specify the array type.
     */
    public T[] toArray(){
        return (T[])toArray(items.getClass().getComponentType());
    }

    public <V> V[] toArray(Class type){
        V[] result = (V[])ArrayReflection.newInstance(type, size);
        System.arraycopy(items, 0, result, 0, size);
        return result;
    }

    public int hashCode(){
        if(!ordered) return super.hashCode();
        Object[] items = this.items;
        int h = 1;
        for(int i = 0, n = size; i < n; i++){
            h *= 31;
            Object item = items[i];
            if(item != null) h += item.hashCode();
        }
        return h;
    }

    public boolean equals(Object object){
        if(object == this) return true;
        if(!ordered) return false;
        if(!(object instanceof Array)) return false;
        Array array = (Array)object;
        if(!array.ordered) return false;
        int n = size;
        if(n != array.size) return false;
        Object[] items1 = this.items;
        Object[] items2 = array.items;
        for(int i = 0; i < n; i++){
            Object o1 = items1[i];
            Object o2 = items2[i];
            if(!(o1 == null ? o2 == null : o1.equals(o2))) return false;
        }
        return true;
    }

    @Override
    public String toString(){
        if(size == 0) return "[]";
        T[] items = this.items;
        StringBuilder buffer = new StringBuilder(32);
        buffer.append('[');
        buffer.append(items[0]);
        for(int i = 1; i < size; i++){
            buffer.append(", ");
            buffer.append(items[i]);
        }
        buffer.append(']');
        return buffer.toString();
    }

    public String toString(String separator, Function<T, String> stringifier){
        if(size == 0) return "";
        T[] items = this.items;
        StringBuilder buffer = new StringBuilder(32);
        buffer.append(stringifier.get(items[0]));
        for(int i = 1; i < size; i++){
            buffer.append(separator);
            buffer.append(stringifier.get(items[i]));
        }
        return buffer.toString();
    }

    public String toString(String separator){
        return toString(separator, String::valueOf);
    }

    public static class ArrayIterator<T> implements Iterator<T>, Iterable<T>{
        private final Array<T> array;
        private final boolean allowRemove;
        int index;
        boolean valid = true;

        public ArrayIterator(Array<T> array){
            this(array, true);
        }

        public ArrayIterator(Array<T> array, boolean allowRemove){
            this.array = array;
            this.allowRemove = allowRemove;
        }

        public boolean hasNext(){
            if(!valid){
                throw new ArcRuntimeException("#iterator() cannot be used nested.");
            }
            return index < array.size;
        }

        public T next(){
            if(index >= array.size) throw new NoSuchElementException(String.valueOf(index));
            if(!valid){
                throw new ArcRuntimeException("#iterator() cannot be used nested.");
            }
            return array.items[index++];
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

    public static class ArrayIterable<T> implements Iterable<T>{
        private final Array<T> array;
        private final boolean allowRemove;
        private ArrayIterator iterator1, iterator2;

        public ArrayIterable(Array<T> array){
            this(array, true);
        }

        public ArrayIterable(Array<T> array, boolean allowRemove){
            this.array = array;
            this.allowRemove = allowRemove;
        }

        public Iterator<T> iterator(){
            if(iterator1 == null){
                iterator1 = new ArrayIterator(array, allowRemove);
                iterator2 = new ArrayIterator(array, allowRemove);
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
}
