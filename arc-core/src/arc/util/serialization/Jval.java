package arc.util.serialization;

import arc.struct.*;
import arc.util.*;

import java.io.*;
import java.util.regex.*;

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

        if(getType() == null) throw new IllegalArgumentException("Invalid JSON value: " + value);
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
            return new Hparser(reader).parse();
        }catch(IOException e){
            throw new ArcRuntimeException(e);
        }
    }

    public static Jval read(byte[] bytes){
        try{
            return new Hparser(new InputStreamReader(new ByteArrayInputStream(bytes))).parse();
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
        try{
            return new Hparser(text).parse();
        }catch(IOException exception){
            // JsonParser does not throw IOException for String
            throw new RuntimeException(exception);
        }
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
    public int asInt(){ return asNumber().intValue(); }
    public long asLong(){ return asNumber().longValue(); }
    public float asFloat(){ return asNumber().floatValue(); }
    public double asDouble(){ return asNumber().doubleValue(); }
    public String asString(){ if(!(value instanceof String) && !(value instanceof Number)) throw new UnsupportedOperationException("Not a string: " + this); return String.valueOf(value); }
    public boolean asBool(){ if(!(value instanceof Boolean)) throw new UnsupportedOperationException("Not a bool: " + this); return (Boolean)value; }
    public Number asNumber(){ if(!(value instanceof Number)) throw new UnsupportedOperationException("Not a number: " + this); return ((Number)value); }

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

    public Jval remove(String name){
        if(name == null) throw new NullPointerException("name is null");
        return asObject().removeKey(name);
    }

    public boolean has(String name){
        if(name == null) throw new NullPointerException("name is null");
        return asObject().containsKey(name);
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
                new Jwriter(false).save(this, buffer, 0);
                break;
            case formatted:
                new Jwriter(true).save(this, buffer, 0);
                break;
            case hjson:
                new Hwriter().save(this, buffer, -1, "", true);
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
            case number: return (value.toString().endsWith(".0") ? value.toString().replace(".0", "") : value.toString()).replace('E', 'e');
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

    }

    /** Alias class of json arrays. */
    public static class JsonArray extends Seq<Jval>{

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

        /**  Flushes the internal buffer but does not flush the wrapped writer.*/
        @Override
        public void flush() throws IOException{
            writer.write(buffer, 0, fill);
            fill = 0;
        }

        /** Does not close or flush the wrapped writer.*/
        @Override
        public void close(){}
    }

    /** The ToString format. */
    public enum Jformat{
        /** JSON (no whitespace). */
        plain,
        /** Formatted JSON. */
        formatted,
        /** Hjson. */
        hjson,
    }

    /**
     * Defines the known json types.
     * There is no null type as the primitive will be null instead of the JsonValue containing null.
     */
    public enum Jtype{
        string, number, object, array, bool, nil,
    }

    static class Hparser{
        private final String buffer;
        private Reader reader;
        private int index;
        private int line;
        private int lineOffset;
        private int current;
        private StringBuilder captureBuffer, peek;
        private boolean capture;
        private boolean isArray;

        Hparser(String string){
            buffer = string;
            reset();
        }

        Hparser(Reader reader) throws IOException{
            this(readToEnd(reader));
        }

        static String readToEnd(Reader reader) throws IOException{
            // read everything into a buffer
            int n;
            char[] part = new char[8 * 1024];
            StringBuilder sb = new StringBuilder();
            while((n = reader.read(part, 0, part.length)) != -1) sb.append(part, 0, n);
            return sb.toString();
        }

        static boolean isWhiteSpace(int ch){
            return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r';
        }

        void reset(){
            index = lineOffset = current = 0;
            line = 1;
            peek = new StringBuilder();
            reader = new StringReader(buffer);
            capture = false;
            captureBuffer = null;
        }

        Jval parse() throws IOException{
            //braces for the root object are optional

            read();
            skipWhiteSpace();

            switch(current){
                case '[':
                case '{':
                    return checkTrailing(readValue());
                default:
                    try{
                        // assume we have a root object without braces
                        return checkTrailing(readObject(true));
                    }catch(Exception exception){
                        // test if we are dealing with a single JSON value instead (true/false/null/num/"")
                        reset();
                        read();
                        skipWhiteSpace();
                        try{
                            return checkTrailing(readValue());
                        }catch(Exception ignored){
                        }
                        throw exception; // throw original error
                    }
            }
        }

        Jval checkTrailing(Jval v) throws JsonParseException, IOException{
            skipWhiteSpace();
            if(!isEndOfText()) throw error("Extra characters in input: " + current);
            return v;
        }

        private Jval readValue() throws IOException{
            switch(current){
                case '\'':
                case '"':
                    return readString();
                case '[':
                    return readArray();
                case '{':
                    return readObject(false);
                default:
                    return readTfnns();
            }
        }

        private Jval readTfnns() throws IOException{
            // Hjson strings can be quoteless
            // returns string, true, false, or null.
            StringBuilder value = new StringBuilder();
            int first = current;
            if(Hwriter.isPunctuatorChar(first))
                throw error("Found a punctuator character '" + (char)first + "' when expecting a quoteless string (check your syntax)");
            value.append((char)current);
            while(true){
                read();
                boolean isEol = current < 0 || current == '\r' || current == '\n' || (current == ',' && isArray) || current == ']';
                if(isEol || current == ',' || current == '}' || current == '#' || current == '/' && (peek() == '/' || peek() == '*')
                ){
                    switch(first){
                        case 'f':
                        case 'n':
                        case 't':
                            String svalue = value.toString().trim();
                            switch(svalue){
                                case "false": return FALSE;
                                case "null": return NULL;
                                case "true": return TRUE;
                            }
                            break;
                        default:
                            if(first == '-' || first >= '0' && first <= '9'){
                                Jval n = tryParseNumber(value, false);
                                if(n != null) return n;
                            }
                    }
                    if(isEol){
                        //remove trailing commas
                        if(value.length() > 0 && value.charAt(value.length() - 1) == ','){
                            value.setLength(value.length() - 1);
                        }
                        //remove any whitespace at the end (ignored in quoteless strings)
                        return new Jval(value.toString().trim());
                    }
                }
                value.append((char)current);
            }
        }

        private Jval readArray() throws IOException{
            isArray = true;
            read();
            JsonArray array = new JsonArray();
            skipWhiteSpace();
            if(readIf(']')){
                return new Jval(array);
            }
            while(true){
                skipWhiteSpace();
                array.add(readValue());
                skipWhiteSpace();
                if(readIf(',')) skipWhiteSpace(); // , is optional
                if(readIf(']')) break;
                else if(isEndOfText()) throw error("End of input while parsing an array (did you forget a closing ']'?)");
            }
            isArray = false;
            return new Jval(array);
        }

        private Jval readObject(boolean objectWithoutBraces) throws IOException{
            if(!objectWithoutBraces) read();
            JsonMap object = new JsonMap();
            skipWhiteSpace();
            while(true){
                if(objectWithoutBraces){
                    if(isEndOfText()) break;
                }else{
                    if(isEndOfText()) throw error("End of input while parsing an object (did you forget a closing '}'?)");
                    if(readIf('}')) break;
                }
                String name = readName();
                skipWhiteSpace();
                if(!readIf(':')){
                    throw expected("':'");
                }
                skipWhiteSpace();
                object.put(name, readValue());
                skipWhiteSpace();
                if(readIf(',')) skipWhiteSpace(); // , is optional
            }
            return new Jval(object);
        }

        private String readName() throws IOException{
            if(current == '"' || current == '\'') return readStringInternal(false);

            StringBuilder name = new StringBuilder();
            int space = -1, start = index;
            while(true){
                if(current == ':'){
                    if(name.length() == 0) throw error("Found ':' but no key name (for an empty key name use quotes)");
                    else if(space >= 0 && space != name.length()){
                        index = start + space;
                        throw error("Found whitespace in your key name (use quotes to include)");
                    }
                    return name.toString();
                }else if(isWhiteSpace(current)){
                    if(space < 0) space = name.length();
                }else if(current < ' '){
                    throw error("Name is not closed");
                }else if(Hwriter.isPunctuatorChar(current)){
                    throw error("Found '" + (char)current + "' where a key name was expected (check your syntax or use quotes if the key name includes {}[],: or whitespace)");
                }else name.append((char)current);
                read();
            }
        }

        private String readMlString() throws IOException{

            // Parse a multiline string value.
            StringBuilder sb = new StringBuilder();
            int triple = 0;

            // we are at '''
            int indent = index - lineOffset - 4;

            // skip white/to (newline)
            while(true){
                if(isWhiteSpace(current) && current != '\n') read();
                else break;
            }
            if(current == '\n'){
                read();
                skipIndent(indent);
            }

            // When parsing for string values, we must look for " and \ characters.
            while(true){
                if(current < 0) throw error("Bad multiline string");
                else if(current == '\''){
                    triple++;
                    read();
                    if(triple == 3){
                        if(sb.charAt(sb.length() - 1) == '\n') sb.deleteCharAt(sb.length() - 1);

                        return sb.toString();
                    }else continue;
                }else{
                    while(triple > 0){
                        sb.append('\'');
                        triple--;
                    }
                }
                if(current == '\n'){
                    sb.append('\n');
                    read();
                    skipIndent(indent);
                }else{
                    if(current != '\r') sb.append((char)current);
                    read();
                }
            }
        }

        private void skipIndent(int indent) throws IOException{
            while(indent-- > 0){
                if(isWhiteSpace(current) && current != '\n') read();
                else break;
            }
        }

        private Jval readString() throws IOException{
            return new Jval(readStringInternal(true));
        }

        private String readStringInternal(boolean allowML) throws IOException{
            // callees make sure that (current=='"' || current=='\'')
            int exitCh = current;
            read();
            startCapture();
            while(current >= 0 && current != exitCh){
                if(current == '\\') readEscape();
                //else if(current < 0x20) throw expected("valid string character");
                else read();
            }
            String string = endCapture();
            read();

            if(allowML && exitCh == '\'' && current == '\'' && string.length() == 0){
                // ''' indicates a multiline string
                read();
                return readMlString();
            }else return string;
        }

        private void readEscape() throws IOException{
            pauseCapture();
            read();
            switch(current){
                case '"':
                case '\'':
                case '#':
                case '/':
                case '\\':
                    captureBuffer.append((char)current);
                    break;
                case 'b':
                    captureBuffer.append('\b');
                    break;
                case 'f':
                    captureBuffer.append('\f');
                    break;
                case 'n':
                    captureBuffer.append('\n');
                    break;
                case 'r':
                    captureBuffer.append('\r');
                    break;
                case 't':
                    captureBuffer.append('\t');
                    break;
                case 'u':
                    char[] hexChars = new char[4];
                    for(int i = 0; i < 4; i++){
                        read();
                        if(!isHexDigit()){
                            throw expected("hexadecimal digit");
                        }
                        hexChars[i] = (char)current;
                    }
                    captureBuffer.append((char)Integer.parseInt(new String(hexChars), 16));
                    break;
                default:
                    throw expected("valid escape sequence");
            }
            capture = true;
            read();
        }

        private static boolean isDigit(char ch){
            return ch >= '0' && ch <= '9';
        }

        static Jval tryParseNumber(StringBuilder value, boolean stopAtNext){
            int idx = 0, len = value.length();
            if(idx < len && value.charAt(idx) == '-') idx++;

            if(idx >= len) return null;
            char first = value.charAt(idx++);
            if(!isDigit(first)) return null;

            if(first == '0' && idx < len && isDigit(value.charAt(idx)))
                return null; // leading zero is not allowed

            while(idx < len && isDigit(value.charAt(idx))) idx++;

            // frac
            if(idx < len && value.charAt(idx) == '.'){
                idx++;
                if(idx >= len || !isDigit(value.charAt(idx++))) return null;
                while(idx < len && isDigit(value.charAt(idx))) idx++;
            }

            // exp
            if(idx < len && Character.toLowerCase(value.charAt(idx)) == 'e'){
                idx++;
                if(idx < len && (value.charAt(idx) == '+' || value.charAt(idx) == '-')) idx++;

                if(idx >= len || !isDigit(value.charAt(idx++))) return null;
                while(idx < len && isDigit(value.charAt(idx))) idx++;
            }

            int last = idx;
            while(idx < len && isWhiteSpace(value.charAt(idx))) idx++;

            boolean foundStop = false;
            if(idx < len && stopAtNext){
                // end scan if we find a control character like ,}] or a comment
                char ch = value.charAt(idx);
                if(ch == ',' || ch == '}' || ch == ']' || ch == '#' || ch == '/' && (len > idx + 1 && (value.charAt(idx + 1) == '/' || value.charAt(idx + 1) == '*')))
                    foundStop = true;
            }

            if(idx < len && !foundStop) return null;
            String str = value.substring(0, last);

            if(!str.contains(".") && !str.contains(",") && !str.contains("e")){
                try{
                    return new Jval(Long.parseLong(str));
                }catch(NumberFormatException ignored){
                }
            }

            return new Jval(Double.parseDouble(str));
        }

        static Jval tryParseNumber(String value) throws IOException{
            return tryParseNumber(new StringBuilder(value), true);
        }

        private boolean readIf(char ch) throws IOException{
            if(current != ch){
                return false;
            }
            read();
            return true;
        }

        private void skipWhiteSpace() throws IOException{
            while(!isEndOfText()){
                while(isWhiteSpace()) read();
                if(current == '#' || current == '/' && peek() == '/'){
                    do{
                        read();
                    }while(current >= 0 && current != '\n');
                }else if(current == '/' && peek() == '*'){
                    read();
                    do{
                        read();
                    }while(current >= 0 && !(current == '*' && peek() == '/'));
                    read();
                    read();
                }else break;
            }
        }

        private int peek(int idx) throws IOException{
            while(idx >= peek.length()){
                int c = reader.read();
                if(c < 0) return c;
                peek.append((char)c);
            }
            return peek.charAt(idx);
        }

        private int peek() throws IOException{
            return peek(0);
        }

        private boolean read() throws IOException{

            if(current == '\n'){
                line++;
                lineOffset = index;
            }

            if(peek.length() > 0){
                // normally peek will only hold not more than one character so this should not matter for performance
                current = peek.charAt(0);
                peek.deleteCharAt(0);
            }else current = reader.read();

            if(current < 0) return false;

            index++;
            if(capture) captureBuffer.append((char)current);

            return true;
        }

        private void startCapture(){
            if(captureBuffer == null)
                captureBuffer = new StringBuilder();
            capture = true;
            captureBuffer.append((char)current);
        }

        private void pauseCapture(){
            int len = captureBuffer.length();
            if(len > 0) captureBuffer.deleteCharAt(len - 1);
            capture = false;
        }

        private String endCapture(){
            pauseCapture();
            String captured;
            if(captureBuffer.length() > 0){
                captured = captureBuffer.toString();
                captureBuffer.setLength(0);
            }else{
                captured = "";
            }
            capture = false;
            return captured;
        }

        private JsonParseException expected(String expected){
            if(isEndOfText()){
                return error("Unexpected end of input");
            }
            return error("Expected " + expected);
        }

        private JsonParseException error(String message){
            int column = index - lineOffset;
            int offset = isEndOfText() ? index : index - 1;
            return new JsonParseException(message, offset, line, column - 1);
        }

        private boolean isWhiteSpace(){
            return isWhiteSpace((char)current);
        }

        private boolean isHexDigit(){
            return current >= '0' && current <= '9'
            || current >= 'a' && current <= 'f'
            || current >= 'A' && current <= 'F';
        }

        private boolean isEndOfText(){
            return current == -1;
        }
    }

    /** An unchecked exception to indicate that an input does not qualify as valid JSON.*/
    public static class JsonParseException extends RuntimeException{
        public final int offset;
        public final int line;
        public final int column;

        JsonParseException(String message, int offset, int line, int column){
            super(message + " at " + line + ":" + column);
            this.offset = offset;
            this.line = line;
            this.column = column;
        }
    }

    static class Hwriter{
        static Pattern needsEscapeName = Pattern.compile("[,\\{\\[\\}\\]\\s:#\"']|//|/\\*");

        void nl(Writer tw, int level) throws IOException{
            tw.write('\n');
            for(int i = 0; i < level; i++) tw.write("  ");
        }

        public void save(Jval value, Writer tw, int level, String separator, boolean noIndent) throws IOException{
            if(value == null){
                tw.write(separator);
                tw.write("null");
                return;
            }

            switch(value.getType()){
                case object:
                    JsonMap obj = value.asObject();
                    if(!noIndent){
                        tw.write(" ");
                    }
                    if(level >= 0) tw.write('{');
                    int index = 0;

                    for(ObjectMap.Entry<String, Jval> pair : obj){
                        if(!(index++ == 0 && level < 0)) nl(tw, level + 1);
                        tw.write(escapeName(pair.key));
                        tw.write(":");
                        save(pair.value, tw, level + 1, " ", false);
                    }

                    if(obj.size > 0) nl(tw, level);
                    if(level >= 0) tw.write('}');
                    break;
                case array:
                    JsonArray arr = value.asArray();
                    int n = arr.size;
                    if(!noIndent){
                        tw.write(" ");
                    }
                    tw.write('[');
                    for(int i = 0; i < n; i++){
                        nl(tw, level + 1);
                        save(arr.get(i), tw, level + 1, "", true);
                    }
                    if(n > 0) nl(tw, level);
                    tw.write(']');
                    break;
                case bool:
                    tw.write(separator);
                    tw.write(value.isTrue() ? "true" : "false");
                    break;
                case string:
                    writeString(value.asString(), tw, level, separator);
                    break;
                default:
                    tw.write(separator);
                    tw.write(value.toString());
                    break;
            }
        }

        static String escapeName(String name){
            if(name.length() == 0 || needsEscapeName.matcher(name).find())
                return "\"" + Jwriter.escapeString(name) + "\"";
            else
                return name;
        }

        void writeString(String value, Writer tw, int level, String separator) throws IOException{
            if(value.length() == 0){
                tw.write(separator + "\"\"");
                return;
            }

            char left = value.charAt(0), right = value.charAt(value.length() - 1);
            char left1 = value.length() > 1 ? value.charAt(1) : '\0';
            boolean doEscape = false;
            char[] valuec = value.toCharArray();
            for(char ch : valuec){
                if(needsQuotes(ch)){
                    doEscape = true;
                    break;
                }
            }

            if(doEscape ||
            Hparser.isWhiteSpace(left) || Hparser.isWhiteSpace(right) ||
            left == '"' ||
            left == '\'' ||
            left == '#' ||
            left == '/' && (left1 == '*' || left1 == '/') ||
            isPunctuatorChar(left) ||
            Hparser.tryParseNumber(value) != null ||
            startsWithKeyword(value)){

                boolean noEscape = true;
                for(char ch : valuec){
                    if(needsEscape(ch)){
                        noEscape = false;
                        break;
                    }
                }
                if(noEscape){
                    tw.write(separator + "\"" + value + "\"");
                    return;
                }

                boolean noEscapeML = true, allWhite = true;
                for(char ch : valuec){
                    if(needsEscapeML(ch)){
                        noEscapeML = false;
                        break;
                    }else if(!Hparser.isWhiteSpace(ch)) allWhite = false;
                }
                if(noEscapeML && !allWhite && !value.contains("'''")) writeMLString(value, tw, level, separator);
                else tw.write(separator + "\"" + Jwriter.escapeString(value) + "\"");
            }else tw.write(separator + value);
        }

        void writeMLString(String value, Writer tw, int level, String separator) throws IOException{
            String[] lines = value.replace("\r", "").split("\n", -1);

            if(lines.length == 1){
                tw.write(separator + "'''");
                tw.write(lines[0]);
                tw.write("'''");
            }else{
                level++;
                nl(tw, level);
                tw.write("'''");

                for(String line : lines){
                    nl(tw, line.length() > 0 ? level : 0);
                    tw.write(line);
                }
                nl(tw, level);
                tw.write("'''");
            }
        }

        static boolean startsWithKeyword(String text){
            int p;
            if(text.startsWith("true") || text.startsWith("null")) p = 4;
            else if(text.startsWith("false")) p = 5;
            else return false;
            while(p < text.length() && Hparser.isWhiteSpace(text.charAt(p))) p++;
            if(p == text.length()) return true;
            char ch = text.charAt(p);
            return ch == ',' || ch == '}' || ch == ']' || ch == '#' || ch == '/' && (text.length() > p + 1 && (text.charAt(p + 1) == '/' || text.charAt(p + 1) == '*'));
        }

        static boolean isPunctuatorChar(int c){
            return c == '{' || c == '}' || c == '[' || c == ']' || c == ',' || c == ':';
        }

        static boolean needsQuotes(char c){
            return c == '\t' || c == '\f' || c == '\b' || c == '\n' || c == '\r'/* || c == ']' || c == ','*/;
        }

        static boolean needsEscape(char c){
            return c == '\"' || c == '\\' || needsQuotes(c);
        }

        static boolean needsEscapeML(char c){
            switch(c){
                case '\n':
                case '\r':
                case '\t':
                    return false;
                default:
                    return needsQuotes(c);
            }
        }
    }

    static class Jwriter{
        boolean format;

        public Jwriter(boolean format){
            this.format = format;
        }

        void nl(Writer tw, int level) throws IOException{
            if(format){
                tw.write('\n');
                for(int i = 0; i < level; i++) tw.write("  ");
            }
        }

        public void save(Jval value, Writer tw, int level) throws IOException{
            boolean following = false;
            switch(value.getType()){
                case object:
                    JsonMap obj = value.asObject();
                    tw.write('{');
                    for(ObjectMap.Entry<String, Jval> pair : obj){
                        if(following) tw.write(",");
                        nl(tw, level + 1);
                        tw.write('\"');
                        tw.write(escapeString(pair.key));
                        tw.write("\":");
                        Jval v = pair.value;
                        Jtype vType = v.getType();
                        if(format && vType != Jtype.array && vType != Jtype.object) tw.write(" ");
                        save(v, tw, level + 1);
                        following = true;
                    }
                    if(following) nl(tw, level);
                    tw.write('}');
                    break;
                case array:
                    JsonArray arr = value.asArray();
                    int n = arr.size;
                    if(level != 0) tw.write(' ');
                    tw.write('[');
                    for(int i = 0; i < n; i++){
                        if(following) tw.write(",");
                        Jval v = arr.get(i);
                        Jtype vType = v.getType();
                        if(vType != Jtype.array) nl(tw, level + 1);
                        save(v, tw, level + 1);
                        following = true;
                    }
                    if(following) nl(tw, level);
                    tw.write(']');
                    break;
                case bool:
                    tw.write(value.isTrue() ? "true" : "false");
                    break;
                case string:
                    tw.write('"');
                    tw.write(escapeString(value.asString()));
                    tw.write('"');
                    break;
                default:
                    tw.write(value.toString());
                    break;
            }
        }

        static String escapeString(String src){
            if(src == null) return null;

            for(int i = 0; i < src.length(); i++){
                if(getEscapedChar(src.charAt(i)) != null){
                    StringBuilder sb = new StringBuilder();
                    if(i > 0) sb.append(src, 0, i);
                    return doEscapeString(sb, src, i);
                }
            }
            return src;
        }

        private static String doEscapeString(StringBuilder sb, String src, int cur){
            int start = cur;
            for(int i = cur; i < src.length(); i++){
                String escaped = getEscapedChar(src.charAt(i));
                if(escaped != null){
                    sb.append(src, start, i);
                    sb.append(escaped);
                    start = i + 1;
                }
            }
            sb.append(src, start, src.length());
            return sb.toString();
        }

        private static String getEscapedChar(char c){
            switch(c){
                case '\"': return "\\\"";
                case '\t': return "\\t";
                case '\n': return "\\n";
                case '\r': return "\\r";
                case '\f': return "\\f";
                case '\b': return "\\b";
                case '\\': return "\\\\";
                default: return null;
            }
        }
    }
}
