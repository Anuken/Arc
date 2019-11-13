/*******************************************************************************
 * Copyright (c) 2013, 2015 EclipseSource.
 * Copyright (c) 2015-2016 Christian Zangl
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package io.anuke.arc.json;

import io.anuke.arc.json.JsonObject.*;

import java.util.*;


/**
 * Represents a JSON object, a set of name/value pairs, where the names are strings and the values
 * are JSON values.
 * <p>
 * Members can be added using the <code>add(String, ...)</code> methods which accept instances of
 * {@link JsonValue}, strings, primitive numbers, and boolean values. To modify certain values of an
 * object, use the <code>set(String, ...)</code> methods. Please note that the <code>add</code>
 * methods are faster than <code>set</code> as they do not search for existing members. On the other
 * hand, the <code>add</code> methods do not prevent adding multiple members with the same name.
 * Duplicate names are discouraged but not prohibited by JSON.
 * </p>
 * <p>
 * Members can be accessed by their name using {@link #get(String)}. A list of all names can be
 * obtained from the method {@link #names()}. This class also supports iterating over the members in
 * document order using an {@link #iterator()} or an enhanced for loop:
 * </p>
 * <pre>
 * for (Member member : jsonObject) {
 *   String name=member.getName();
 *   JsonValue value=member.getValue();
 *   ...
 * }
 * </pre>
 * <p>
 * Even though JSON objects are unordered by definition, instances of this class preserve the order
 * of members to allow processing in document order and to guarantee a predictable output.
 * </p>
 * <p>
 * Note that this class is <strong>not thread-safe</strong>. If multiple threads access a
 * <code>JsonObject</code> instance concurrently, while at least one of these threads modifies the
 * contents of this object, access to the instance must be synchronized externally. Failure to do so
 * may lead to an inconsistent state.
 * </p>
 * <p>
 * This class is <strong>not supposed to be extended</strong> by clients.
 * </p>
 */
public class JsonObject extends JsonValue implements Iterable<Member>{
    private final List<String> names;
    private final List<JsonValue> values;
    private transient HashIndexTable table;

    /**
     * Creates a new empty JsonObject.
     */
    public JsonObject(){
        names = new ArrayList<>();
        values = new ArrayList<>();
        table = new HashIndexTable();
    }

    /**
     * Creates a new JsonObject, initialized with the contents of the specified JSON object.
     * @param object the JSON object to get the initial contents from, must not be <code>null</code>
     */
    public JsonObject(JsonObject object){
        this(object, false);
    }

    private JsonObject(JsonObject object, boolean unmodifiable){
        if(object == null) throw new NullPointerException("object is null");
        if(unmodifiable){
            names = Collections.unmodifiableList(object.names);
            values = Collections.unmodifiableList(object.values);
        }else{
            names = new ArrayList<>(object.names);
            values = new ArrayList<>(object.values);
        }
        table = new HashIndexTable();
        updateHashIndex();
    }

    public JsonObject add(String name, int value){
        add(name, valueOf(value));
        return this;
    }

    public JsonObject add(String name, long value){
        add(name, valueOf(value));
        return this;
    }

    public JsonObject add(String name, float value){
        add(name, valueOf(value));
        return this;
    }

    public JsonObject add(String name, double value){
        add(name, valueOf(value));
        return this;
    }

    public JsonObject add(String name, boolean value){
        add(name, valueOf(value));
        return this;
    }

    public JsonObject add(String name, String value){
        add(name, valueOf(value));
        return this;
    }

    public JsonObject add(String name, JsonValue value){
        if(name == null) throw new NullPointerException("name is null");
        if(value == null) throw new NullPointerException("value is null");

        table.add(name, names.size());
        names.add(name);
        values.add(value);
        return this;
    }

    public JsonObject set(String name, int value){
        set(name, valueOf(value));
        return this;
    }

    public JsonObject set(String name, long value){
        set(name, valueOf(value));
        return this;
    }

    public JsonObject set(String name, float value){
        set(name, valueOf(value));
        return this;
    }

    public JsonObject set(String name, double value){
        set(name, valueOf(value));
        return this;
    }

    public JsonObject set(String name, boolean value){
        set(name, valueOf(value));
        return this;
    }

    public JsonObject set(String name, String value){
        set(name, valueOf(value));
        return this;
    }

    public JsonObject set(String name, JsonValue value){
        if(name == null){
            throw new NullPointerException("name is null");
        }
        if(value == null){
            throw new NullPointerException("value is null");
        }
        int index = indexOf(name);
        if(index != -1){
            values.set(index, value);
        }else{
            table.add(name, names.size());
            names.add(name);
            values.add(value);
        }
        return this;
    }

    public JsonObject remove(String name){
        if(name == null){
            throw new NullPointerException("name is null");
        }
        int index = indexOf(name);
        if(index != -1){
            table.remove(index);
            names.remove(index);
            values.remove(index);
        }
        return this;
    }

    public JsonValue get(String name){
        if(name == null){
            throw new NullPointerException("name is null");
        }
        int index = indexOf(name);
        return index != -1 ? values.get(index) : null;
    }

    public int getInt(String name, int defaultValue){
        JsonValue value = get(name);
        return value != null ? value.asInt() : defaultValue;
    }

    public long getLong(String name, long defaultValue){
        JsonValue value = get(name);
        return value != null ? value.asLong() : defaultValue;
    }

    public float getFloat(String name, float defaultValue){
        JsonValue value = get(name);
        return value != null ? value.asFloat() : defaultValue;
    }

    public double getDouble(String name, double defaultValue){
        JsonValue value = get(name);
        return value != null ? value.asDouble() : defaultValue;
    }

    public boolean getBool(String name, boolean defaultValue){
        JsonValue value = get(name);
        return value != null ? value.asBool() : defaultValue;
    }

    public String getString(String name, String defaultValue){
        JsonValue value = get(name);
        return value != null ? value.asString() : defaultValue;
    }

    public int size(){
        return names.size();
    }

    public boolean isEmpty(){
        return names.isEmpty();
    }

    /**
     * Returns a list of the names in this object in document order. The returned list is backed by
     * this object and will reflect subsequent changes. It cannot be used to modify this object.
     * Attempts to modify the returned list will result in an exception.
     * @return a list of the names in this object
     */
    public List<String> names(){
        return Collections.unmodifiableList(names);
    }

    /**
     * Returns an iterator over the members of this object in document order. The returned iterator
     * cannot be used to modify this object.
     * @return an iterator over the members of this object
     */
    @Override
    public Iterator<Member> iterator(){
        final Iterator<String> namesIterator = names.iterator();
        final Iterator<JsonValue> valuesIterator = values.iterator();
        return new Iterator<JsonObject.Member>(){

            @Override
            public boolean hasNext(){
                return namesIterator.hasNext();
            }

            @Override
            public Member next(){
                String name = namesIterator.next();
                JsonValue value = valuesIterator.next();
                return new Member(name, value);
            }

            @Override
            public void remove(){
                throw new UnsupportedOperationException();
            }

        };
    }

    @Override
    public JsonType getType(){
        return JsonType.object;
    }

    @Override
    public boolean isObject(){
        return true;
    }

    @Override
    public JsonObject asObject(){
        return this;
    }

    @Override
    public int hashCode(){
        int result = 1;
        result = 31 * result + names.hashCode();
        result = 31 * result + values.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(getClass() != obj.getClass()){
            return false;
        }
        JsonObject other = (JsonObject)obj;
        return names.equals(other.names) && values.equals(other.values);
    }

    int indexOf(String name){
        int index = table.get(name);
        if(index != -1 && name.equals(names.get(index))){
            return index;
        }
        return names.lastIndexOf(name);
    }

    private void updateHashIndex(){
        int size = names.size();
        for(int i = 0; i < size; i++){
            table.add(names.get(i), i);
        }
    }

    /** Represents a member of a JSON object, a pair of a name and a value.*/
    public static class Member{
        public final String name;
        public final JsonValue value;

        Member(String name, JsonValue value){
            this.name = name;
            this.value = value;
        }

        @Override
        public int hashCode(){
            int result = 1;
            result = 31 * result + name.hashCode();
            result = 31 * result + value.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj){
            if(this == obj){
                return true;
            }
            if(obj == null){
                return false;
            }
            if(getClass() != obj.getClass()){
                return false;
            }
            Member other = (Member)obj;
            return name.equals(other.name) && value.equals(other.value);
        }

    }

    static class HashIndexTable{
        private final byte[] hashTable = new byte[32]; // must be a power of two

        public HashIndexTable(){
        }

        void add(String name, int index){
            int slot = hashSlotfor(name);
            if(index < 0xff){
                // increment by 1, 0 stands for empty
                hashTable[slot] = (byte)(index + 1);
            }else{
                hashTable[slot] = 0;
            }
        }

        void remove(int index){
            for(int i = 0; i < hashTable.length; i++){
                if(hashTable[i] == index + 1){
                    hashTable[i] = 0;
                }else if(hashTable[i] > index + 1){
                    hashTable[i]--;
                }
            }
        }

        int get(Object name){
            int slot = hashSlotfor(name);
            // subtract 1, 0 stands for empty
            return (hashTable[slot] & 0xff) - 1;
        }

        private int hashSlotfor(Object element){
            return element.hashCode() & hashTable.length - 1;
        }
    }
}
