package arc.util.serialization;

import arc.files.*;
import arc.struct.*;
import arc.util.*;

import java.io.*;

/** An hsjon parser. Can be used as a standard json value.
 * Output can be converted to standard JSON. This class is heavily based upon the Hjson Java implementation.*/
public class Jval{
    public static final Jval
        TRUE = new Jval(true),
        FALSE = new Jval(false),
        NULL = new Jval(null);

    /** Internal value. May be a string, number, boolean, JsonArray, JsonMap or null. */
    private @Nullable Object value;

    Jval(Object value){
        this.value = value;
    }

    public static Jval newObject(){
        return new Jval(new JsonMap());
    }

    public static Jval newArray(){
        return new Jval(new JsonArray());
    }

    /**
     * Reads a Hjson value from the given reader.
     * <p>
     * Characters are read in chunks and buffered internally, therefore wrapping an existing reader in
     * an additional <code>BufferedReader</code> does <strong>not</strong> improve reading
     * performance.
     * </p>
     * @param reader the reader to read the Hjson value from
     * @return the Hjson value that has been read
     */
    public static Jval read(Reader reader){
        try{
            return new JvalReader(reader).parse();
        }catch(IOException e){
            throw new ArcRuntimeException(e);
        }
    }

    public static Jval read(byte[] bytes){
        try{
            return new JvalReader(new InputStreamReader(new ByteArrayInputStream(bytes))).parse();
        }catch(IOException e){
            throw new ArcRuntimeException(e);
        }
    }

    /**
     * Reads a Hjson value from the given string.
     * @param text the string that contains the Hjson value
     * @return the Hjson value that has been read
     */
    public static Jval read(String text){
        return new JvalReader(text).parse();
    }

    public static Jval read(Fi file){
        return read(file.reader());
    }

    public Jtype getType(){
        return value == null ? Jtype.nil :
                value instanceof Number ? Jtype.number :
                value instanceof String ? Jtype.string :
                value instanceof Boolean ? Jtype.bool :
                value instanceof JsonMap ? Jtype.object :
                value instanceof JsonArray ? Jtype.array : null;
    }

    public static Jval valueOf(int value){ return new Jval(value); }
    public static Jval valueOf(long value){ return new Jval(value); }
    public static Jval valueOf(float value){ return new Jval(value); }
    public static Jval valueOf(double value){ return new Jval(value); }
    public static Jval valueOf(String string){ return string == null ? NULL : new Jval(string); }
    public static Jval valueOf(boolean value){ return value ? TRUE : FALSE; }

    public boolean isObject(){ return value instanceof JsonMap; }
    public boolean isArray(){ return value instanceof JsonArray; }
    public boolean isNumber(){ return value instanceof Number; }
    public boolean isString(){ return value instanceof String; }
    public boolean isBoolean(){ return value instanceof Boolean; }
    public boolean isTrue(){ return value == Boolean.TRUE; }
    public boolean isFalse(){ return value == Boolean.FALSE; }
    public boolean isNull(){ return value == null; }

    public JsonMap asObject(){ if(!(value instanceof JsonMap)) throw new UnsupportedOperationException("Not an object: " + this); return (JsonMap)value; }
    public JsonArray asArray(){ if(!(value instanceof JsonArray)) throw new UnsupportedOperationException("Not an array: " + this); return (JsonArray)value; }

    public byte asByte(){
        if(value instanceof Number){
            return ((Number)value).byteValue();
        }else if(value instanceof String){
            return Byte.parseByte((String)value);
        }else{
            throw new UnsupportedOperationException("Not a number: " + this);
        }
    }

    public short asShort(){
        if(value instanceof Number){
            return ((Number)value).shortValue();
        }else if(value instanceof String){
            return Short.parseShort((String)value);
        }else{
            throw new UnsupportedOperationException("Not a number: " + this);
        }
    }

    public int asInt(){
        if(value instanceof Number){
            return ((Number)value).intValue();
        }else if(value instanceof String){
            return Integer.parseInt((String)value);
        }else{
            throw new UnsupportedOperationException("Not a number: " + this);
        }
    }

    public long asLong(){
        if(value instanceof Number){
            return ((Number)value).longValue();
        }else if(value instanceof String){
            return Long.parseLong((String)value);
        }else{
            throw new UnsupportedOperationException("Not a number: " + this);
        }
    }

    public float asFloat(){
        if(value instanceof Number){
            return ((Number)value).floatValue();
        }else if(value instanceof String){
            return Float.parseFloat((String)value);
        }else{
            throw new UnsupportedOperationException("Not a number: " + this);
        }
    }

    public double asDouble(){
        if(value instanceof Number){
            return ((Number)value).doubleValue();
        }else if(value instanceof String){
            return Double.parseDouble((String)value);
        }else{
            throw new UnsupportedOperationException("Not a number: " + this);
        }
    }

    public @Nullable String asString(){
        if(value == null) return null;
        if(!(value instanceof String) && !(value instanceof Number)) throw new UnsupportedOperationException("Not a string: " + this);
        return String.valueOf(value);
    }

    public boolean asBool(){
        if(value instanceof Boolean){
            return (Boolean)value;
        }else if(value instanceof String){
            return Boolean.parseBoolean((String)value);
        }
        throw new UnsupportedOperationException("Not a bool: " + this);
    }

    public byte[] asByteArray(){
        JsonArray arr = asArray();
        byte[] out = new byte[arr.size];
        for(int i = 0; i < out.length; i++) out[i] = arr.get(i).asByte();
        return out;
    }

    public short[] asShortArray(){
        JsonArray arr = asArray();
        short[] out = new short[arr.size];
        for(int i = 0; i < out.length; i++) out[i] = arr.get(i).asShort();
        return out;
    }

    public int[] asIntArray(){
        JsonArray arr = asArray();
        int[] out = new int[arr.size];
        for(int i = 0; i < out.length; i++) out[i] = arr.get(i).asInt();
        return out;
    }

    public long[] asLongArray(){
        JsonArray arr = asArray();
        long[] out = new long[arr.size];
        for(int i = 0; i < out.length; i++) out[i] = arr.get(i).asLong();
        return out;
    }

    public float[] asFloatArray(){
        JsonArray arr = asArray();
        float[] out = new float[arr.size];
        for(int i = 0; i < out.length; i++) out[i] = arr.get(i).asFloat();
        return out;
    }

    public double[] asDoubleArray(){
        JsonArray arr = asArray();
        double[] out = new double[arr.size];
        for(int i = 0; i < out.length; i++) out[i] = arr.get(i).asDouble();
        return out;
    }

    public boolean[] asBooleanArray(){
        JsonArray arr = asArray();
        boolean[] out = new boolean[arr.size];
        for(int i = 0; i < out.length; i++) out[i] = arr.get(i).asBool();
        return out;
    }

    public String[] asStringArray(){
        JsonArray arr = asArray();
        String[] out = new String[arr.size];
        for(int i = 0; i < out.length; i++) out[i] = arr.get(i).asString();
        return out;
    }

    public Jval get(int i){
        return asArray().get(i);
    }

    public Jval get(String name){
        if(name == null) throw new NullPointerException("name is null");
        return asObject().get(name);
    }

    public void add(String name, Jval val){
        if(name == null) throw new NullPointerException("name is null");
        asObject().put(name, val == null ? NULL : val);
    }

    public void add(String name, String val){
        add(name, valueOf(val));
    }

    public Jval add(Jval value){
        asArray().add(value);
        return this;
    }

    public Jval add(String value){
        asArray().add(new Jval(value));
        return this;
    }

    public Jval add(Number value){
        asArray().add(new Jval(value));
        return this;
    }

    public Jval add(boolean value){
        asArray().add(new Jval(value));
        return this;
    }

    public Jval put(String name, Jval val){
        if(val != null) add(name, val);
        return this;
    }

    public Jval put(String name, String val){
        if(val != null) add(name, val);
        return this;
    }

    public Jval put(String name, Number val){
        if(val != null) add(name, new Jval(val));
        return this;
    }

    public Jval put(String name, boolean val){
        add(name, new Jval(val));
        return this;
    }

    public Jval clear(){
        asObject().clear();
        return this;
    }

    public Jval remove(String name){
        if(name == null) throw new NullPointerException("name is null");
        return asObject().removeKey(name);
    }

    public boolean has(String name){
        if(name == null) throw new NullPointerException("name is null");
        return asObject().containsKey(name);
    }

    public int getInt(String name){
        return getInt(name, 0);
    }

    public int getInt(String name, int defaultValue){
        Jval value = get(name);
        return value != null ? value.asInt() : defaultValue;
    }

    public long getLong(String name, long defaultValue){
        Jval value = get(name);
        return value != null ? value.asLong() : defaultValue;
    }

    public float getFloat(String name, float defaultValue){
        Jval value = get(name);
        return value != null ? value.asFloat() : defaultValue;
    }

    public double getDouble(String name, double defaultValue){
        Jval value = get(name);
        return value != null ? value.asDouble() : defaultValue;
    }

    public boolean getBool(String name, boolean defaultValue){
        Jval value = get(name);
        return value != null ? value.asBool() : defaultValue;
    }

    public long getLong(String name){
        return getLong(name, 0L);
    }

    public float getFloat(String name){
        return getFloat(name, 0f);
    }

    public double getDouble(String name){
        return getDouble(name, 0);
    }

    public boolean getBool(String name){
        return getBool(name, false);
    }

    public @Nullable String getString(String name){
        return getString(name, "");
    }

    public String getString(String name, String defaultValue){
        Jval value = get(name);
        return value != null && !value.isNull() ? value.asString() : defaultValue;
    }

    /**
     * Writes the JSON representation of this value to the given writer in its minimal form, without
     * any additional whitespace.
     * <p>
     * Writing performance can be improved by using a {@link java.io.BufferedWriter BufferedWriter}.
     * </p>
     * @param writer the writer to write this value to
     * @throws IOException if an I/O error occurs in the writer
     */
    public void writeTo(Writer writer) throws IOException{
        writeTo(writer, Jformat.plain);
    }

    /**
     * Writes the JSON/Hjson representation of this value to the given writer using the given formatting.
     * <p>
     * Writing performance can be improved by using a {@link java.io.BufferedWriter BufferedWriter}.
     * </p>
     * @param writer the writer to write this value to
     * @param format controls the formatting
     * @throws IOException if an I/O error occurs in the writer
     */
    public void writeTo(Writer writer, Jformat format) throws IOException{
        WritingBuffer buffer = new WritingBuffer(writer, 128);
        switch(format){
            case plain:
                JvalWriter.writeJson(this, false, true, buffer, 0);
                break;
            case minimal:
                JvalWriter.writeJson(this, false, false, buffer, 0);
                break;
            case formatted:
                JvalWriter.writeJson(this, true, true, buffer, 0);
                break;
            case hjson:
                JvalWriter.writeHjson(this, buffer, -1, "", true);
                break;
        }
        buffer.flush();
    }

    /** Returns the JSON string for this value in its minimal form, without any additional whitespace.*/
    @Override
    public String toString(){
        Jtype type = getType();
        switch(type){
            case nil: return "null";
            case number:{
                String s = value.toString();
                if(s.endsWith(".0")) s = s.substring(0, s.length() - 2);
                return s.replace('E', 'e');
            }
            case string:
            case bool: return value.toString();
        }

        return toString(Jformat.plain);
    }

    /**
     * Returns the JSON/Hjson string for this value using the given formatting.
     * @param format controls the formatting
     * @return a JSON/Hjson string that represents this value
     */
    public String toString(Jformat format){
        StringWriter writer = new StringWriter();
        try{
            writeTo(writer, format);
        }catch(IOException exception){
            // StringWriter does not throw IOExceptions
            throw new RuntimeException(exception);
        }
        return writer.toString();
    }

    @Override
    public boolean equals(Object object){
        return object != null && object.getClass() == getClass() &&
            ((value == null && ((Jval)object).value == null)
            || (((Jval)object).value != null && value != null && value.equals(((Jval)object).value)));
    }

    /** Alias class of whatever is used to store json maps (objects). */
    public static class JsonMap extends ArrayMap<String, Jval>{

        /** Puts a value without checking if it's in the map first. This is unsafe, but prevents O(n) put. */
        protected void putAdd(String key, Jval value){
            if(size == ((Object[])keys).length) resize(Math.max(8, (int)(size * 1.75f)));
            int index = size++;
            ((Object[])keys)[index] = key;
            ((Object[])values)[index] = value;
        }
    }

    /** Alias class of json arrays. */
    public static class JsonArray extends Seq<Jval>{

    }

    /** The ToString format. */
    public enum Jformat{
        /** JSON (no whitespace). */
        plain,
        /** Minimal quote-less JSON. Equivalent to libGDX's minimal output type. */
        minimal,
        /** Formatted JSON. */
        formatted,
        /** Formatted HJSON. */
        hjson,
    }

    /**
     * Defines the known json types.
     * There is no null type as the primitive will be null instead of the Jval containing null.
     */
    public enum Jtype{
        string, number, object, array, bool, nil,
    }

}