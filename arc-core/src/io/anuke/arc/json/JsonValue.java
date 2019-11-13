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

import io.anuke.arc.json.HjsonParser.*;
import io.anuke.arc.util.ArcAnnotate.*;
import io.anuke.arc.util.*;

import java.io.*;

/**
 * Represents a JSON value. This can be a JSON <strong>object</strong>, an <strong> array</strong>,
 * a <strong>number</strong>, a <strong>string</strong>, or one of the literals
 * <strong>true</strong>, <strong>false</strong>, and <strong>null</strong>.
 * <p>
 * The literals <strong>true</strong>, <strong>false</strong>, and <strong>null</strong> are
 * represented by the constants {@link #TRUE}, {@link #FALSE}, and {@link #NULL}.
 * </p>
 * <p>
 * JSON <strong>objects</strong> and <strong>arrays</strong> are represented by the subtypes
 * {@link JsonObject} and {@link JsonArray}. Instances of these types can be created using the
 * public constructors of these classes.
 * </p>
 * <p>
 * Instances that represent JSON <strong>numbers</strong>, <strong>strings</strong> and
 * <strong>boolean</strong> values can be created using the static factory methods
 * {@link #valueOf(String)}, {@link #valueOf(long)}, {@link #valueOf(double)}, etc.
 * </p>
 * <p>
 * In order to find out whether an instance of this class is of a certain type, the methods
 * {@link #isObject()}, {@link #isArray()}, {@link #isString()}, {@link #isNumber()} etc. can be
 * used.
 * </p>
 * <p>
 * If the type of a JSON value is known, the methods {@link #asObject()}, {@link #asArray()},
 * {@link #asString()}, {@link #asInt()}, etc. can be used to get this value directly in the
 * appropriate target type.
 * </p>
 * <p>
 * This class is <strong>not supposed to be extended</strong> by clients.
 * </p>
 */
public class JsonValue{
    /** Represents the JSON literal <code>true</code>.*/
    public static final JsonValue TRUE = new JsonValue(true);
    /** Represents the JSON literal <code>false</code>.*/
    public static final JsonValue FALSE = new JsonValue(false);
    /** Represents the JSON literal <code>null</code>.*/
    public static final JsonValue NULL = new JsonValue(null);

    /** Internal value. May be a string, number, boolean or null. */
    private @Nullable Object value;

    JsonValue(){

    }

    JsonValue(Object value){
        this.value = value;
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
     * @throws IOException if an I/O error occurs in the reader
     * @throws JsonParseException if the input is not valid Hjson
     */
    public static JsonValue read(Reader reader) throws IOException{
        return new HjsonParser(reader).parse();
    }

    /**
     * Reads a Hjson value from the given string.
     * @param text the string that contains the Hjson value
     * @return the Hjson value that has been read
     * @throws JsonParseException if the input is not valid Hjson
     */
    public static JsonValue read(String text){
        try{
            return new HjsonParser(text).parse();
        }catch(IOException exception){
            // JsonParser does not throw IOException for String
            throw new RuntimeException(exception);
        }
    }

    public JsonType getType(){
        return value == null ? JsonType.nil : value instanceof Number ? JsonType.number : value instanceof String ? JsonType.string : value instanceof Boolean ? JsonType.bool : JsonType.object;
    }

    public static JsonValue valueOf(int value){ return new JsonValue(value); }
    public static JsonValue valueOf(long value){ return new JsonValue(value); }
    public static JsonValue valueOf(float value){ return new JsonValue(value); }
    public static JsonValue valueOf(double value){ return new JsonValue(value); }
    public static JsonValue valueOf(String string){ return string == null ? NULL : new JsonValue(string); }
    public static JsonValue valueOf(boolean value){ return value ? TRUE : FALSE; }

    public boolean isObject(){ return false; }
    public boolean isArray(){ return false; }
    public boolean isNumber(){ return value instanceof Number; }
    public boolean isString(){ return value instanceof Structs; }
    public boolean isBoolean(){ return value instanceof Boolean; }
    public boolean isTrue(){ return value == Boolean.TRUE; }
    public boolean isFalse(){ return value == Boolean.FALSE; }
    public boolean isNull(){ return value == null; }

    public JsonObject asObject(){ throw new UnsupportedOperationException("Not an object: " + toString()); }
    public JsonArray asArray(){ throw new UnsupportedOperationException("Not an array: " + toString()); }

    public int asInt(){ return asNumber().intValue(); }
    public long asLong(){ return asNumber().longValue(); }
    public float asFloat(){ return asNumber().floatValue(); }
    public double asDouble(){ return asNumber().doubleValue(); }
    public String asString(){ if(!(value instanceof String)) throw new UnsupportedOperationException("Not a string: " + toString()); return (String)value; }
    public boolean asBool(){ if(!(value instanceof Boolean)) throw new UnsupportedOperationException("Not a bool: " + toString()); return (Boolean)value; }
    public Number asNumber(){ if(!(value instanceof Number)) throw new UnsupportedOperationException("Not a number: " + toString());return ((Number)value); }

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
        writeTo(writer, Stringify.plain);
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
    public void writeTo(Writer writer, Stringify format) throws IOException{
        WritingBuffer buffer = new WritingBuffer(writer, 128);
        switch(format){
            case plain:
                new JsonWriter(false).save(this, buffer, 0);
                break;
            case formatted:
                new JsonWriter(true).save(this, buffer, 0);
                break;
            case hjson:
                new HjsonWriter().save(this, buffer, 0, "", true);
                break;
        }
        buffer.flush();
    }

    /**
     * Returns the JSON string for this value in its minimal form, without any additional whitespace.
     * The result is guaranteed to be a valid input for the method {@link #readJSON(String)} and to
     * create a value that is <em>equal</em> to this object.
     * @return a JSON string that represents this value
     */
    @Override
    public String toString(){
        return toString(Stringify.plain);
    }

    /**
     * Returns the JSON/Hjson string for this value using the given formatting.
     * @param format controls the formatting
     * @return a JSON/Hjson string that represents this value
     */
    public String toString(Stringify format){
        StringWriter writer = new StringWriter();
        try{
            writeTo(writer, format);
        }catch(IOException exception){
            // StringWriter does not throw IOExceptions
            throw new RuntimeException(exception);
        }
        return writer.toString();
    }

    /**
     * Indicates whether some other object is "equal to" this one according to the contract specified
     * in {@link Object#equals(Object)}.
     * <p>
     * Two JsonValues are considered equal if and only if they represent the same JSON text. As a
     * consequence, two given JsonObjects may be different even though they contain the same set of
     * names with the same values, but in a different order.
     * </p>
     * @param object the reference object with which to compare
     * @return true if this object is the same as the object argument; false otherwise
     */
    @Override
    public boolean equals(Object object){
        return super.equals(object);
    }

    @Override
    public int hashCode(){
        return super.hashCode();
    }

    static boolean isPunctuatorChar(int c){
        return c == '{' || c == '}' || c == '[' || c == ']' || c == ',' || c == ':';
    }

    /** The ToString format. */
    public enum Stringify{
        /** JSON (no whitespace). */
        plain,
        /** Formatted JSON. */
        formatted,
        /** Hjson. */
        hjson,
    }

    /**
     * A lightweight writing buffer to reduce the amount of write operations to be performed on the
     * underlying writer. This implementation is not thread-safe. It deliberately deviates from the
     * contract of Writer. In particular, it does not flush or close the wrapped writer nor does it
     * ensure that the wrapped writer is open.
     */
    static class WritingBuffer extends Writer{
        private final Writer writer;
        private final char[] buffer;
        private int fill = 0;

        WritingBuffer(Writer writer, int bufferSize){
            this.writer = writer;
            buffer = new char[bufferSize];
        }

        @Override
        public void write(int c) throws IOException{
            if(fill > buffer.length - 1){
                flush();
            }
            buffer[fill++] = (char)c;
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException{
            if(fill > buffer.length - len){
                flush();
                if(len > buffer.length){
                    writer.write(cbuf, off, len);
                    return;
                }
            }
            System.arraycopy(cbuf, off, buffer, fill, len);
            fill += len;
        }

        @Override
        public void write(String str, int off, int len) throws IOException{
            if(fill > buffer.length - len){
                flush();
                if(len > buffer.length){
                    writer.write(str, off, len);
                    return;
                }
            }
            str.getChars(off, off + len, buffer, fill);
            fill += len;
        }

        /**
         * Flushes the internal buffer but does not flush the wrapped writer.
         */
        @Override
        public void flush() throws IOException{
            writer.write(buffer, 0, fill);
            fill = 0;
        }

        /**
         * Does not close or flush the wrapped writer.
         */
        @Override
        public void close(){}
    }

    /**
     * Defines the known json types.
     * There is no null type as the primitive will be null instead of the JsonValue containing null.
     */
    public enum JsonType{
        string, number, object, array, bool, nil,
    }
}
