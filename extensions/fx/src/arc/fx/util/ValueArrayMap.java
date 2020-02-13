package arc.fx.util;

import arc.struct.*;

import java.util.*;

/**
 * Almost like regular ArrayMap, but supports value sorting and access to values by indices.
 */
public class ValueArrayMap<K, V>{
    private final Map<K, V> map;
    private final Array<V> values;

    private final Array<K> tmpKeyArray;

    public ValueArrayMap(){
        this(16);
    }

    public ValueArrayMap(int capacity){
        map = new HashMap<>(capacity);
        values = new Array<>(true, capacity);

        tmpKeyArray = new Array<>(capacity);
    }

    public void put(K key, V value){
        map.put(key, value);
        values.add(value);
    }

    public V get(K key){
        return map.get(key);
    }

    public V getValueAt(int valueIndex){
        return values.get(valueIndex);
    }

    public V remove(K key){
        V value = map.remove(key);
        if(value != null){
            values.remove(value, true);
        }
        return value;
    }

    public V removeByValue(V value){
        K key = findKey(value);
        return remove(key);
    }

    public K findKey(V value){
        for(Map.Entry<K, V> entry : map.entrySet()){
            if(entry.getValue() == value){
                return entry.getKey();
            }
        }
        return null;
    }

    public void clear(){
        map.clear();
        values.clear();
    }

    public boolean contains(K key){
        return map.containsKey(key);
    }

    public int size(){
        return map.size();
    }

    public void sort(Comparator<V> comparator){
        values.sort(comparator);
    }

    public Array<V> getValues(){
        return values;
    }

    /** Warning: returned array will be reused! */
    public Array<K> getKeys(){
        Array<K> result = tmpKeyArray;
        result.clear();

        for(K key : map.keySet()){
            result.add(key);
        }
        return result;
    }

    @Override
    public String toString(){
        return values.toString();
    }

    public String toString(String separator){
        return values.toString(separator);
    }
}
