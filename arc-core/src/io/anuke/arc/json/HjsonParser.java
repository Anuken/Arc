/*******************************************************************************
 * Copyright (c) 2013, 2015 EclipseSource.
 * Copyright (c) 2015-2017 Christian Zangl
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

import java.io.*;

class HjsonParser{
    private final String buffer;
    private Reader reader;
    private int index;
    private int line;
    private int lineOffset;
    private int current;
    private StringBuilder captureBuffer, peek;
    private boolean capture;
    private HjsonDsfProvider[] dsfProviders;

    HjsonParser(String string, HjsonOptions options){
        buffer = string;
        reset();
        if(options != null){
            dsfProviders = options.dsf.clone();
        }else{
            dsfProviders = new HjsonDsfProvider[0];
        }
    }

    HjsonParser(Reader reader, HjsonOptions options) throws IOException{
        this(readToEnd(reader), options);
    }

    static String readToEnd(Reader reader) throws IOException{
        // read everything into a buffer
        int n;
        char[] part = new char[8 * 1024];
        StringBuilder sb = new StringBuilder();
        while((n = reader.read(part, 0, part.length)) != -1) sb.append(part, 0, n);
        return sb.toString();
    }

    void reset(){
        index = lineOffset = current = 0;
        line = 1;
        peek = new StringBuilder();
        reader = new StringReader(buffer);
        capture = false;
        captureBuffer = null;
    }

    JsonValue parse() throws IOException{
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

    JsonValue checkTrailing(JsonValue v) throws ParseException, IOException{
        skipWhiteSpace();
        if(!isEndOfText()) throw error("Extra characters in input: " + current);
        return v;
    }

    private JsonValue readValue() throws IOException{
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

    private JsonValue readTfnns() throws IOException{
        // Hjson strings can be quoteless
        // returns string, true, false, or null.
        StringBuilder value = new StringBuilder();
        int first = current;
        if(JsonValue.isPunctuatorChar(first))
            throw error("Found a punctuator character '" + (char)first + "' when expecting a quoteless string (check your syntax)");
        value.append((char)current);
        for(; ; ){
            read();
            boolean isEol = current < 0 || current == '\r' || current == '\n';
            if(isEol || current == ',' ||
            current == '}' || current == ']' ||
            current == '#' ||
            current == '/' && (peek() == '/' || peek() == '*')
            ){
                switch(first){
                    case 'f':
                    case 'n':
                    case 't':
                        String svalue = value.toString().trim();
                        if(svalue.equals("false")) return JsonValue.FALSE;
                        else if(svalue.equals("null")) return JsonValue.NULL;
                        else if(svalue.equals("true")) return JsonValue.TRUE;
                        break;
                    default:
                        if(first == '-' || first >= '0' && first <= '9'){
                            JsonValue n = tryParseNumber(value, false);
                            if(n != null) return n;
                        }
                }
                if(isEol){
                    // remove any whitespace at the end (ignored in quoteless strings)
                    return HjsonDsf.parse(dsfProviders, value.toString().trim());
                }
            }
            value.append((char)current);
        }
    }

    private JsonArray readArray() throws IOException{
        read();
        JsonArray array = new JsonArray();
        skipWhiteSpace();
        if(readIf(']')){
            return array;
        }
        while(true){
            skipWhiteSpace();
            array.add(readValue());
            skipWhiteSpace();
            if(readIf(',')) skipWhiteSpace(); // , is optional
            if(readIf(']')) break;
            else if(isEndOfText()) throw error("End of input while parsing an array (did you forget a closing ']'?)");
        }
        return array;
    }

    private JsonObject readObject(boolean objectWithoutBraces) throws IOException{
        if(!objectWithoutBraces) read();
        JsonObject object = new JsonObject();
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
            object.add(name, readValue());
            skipWhiteSpace();
            if(readIf(',')) skipWhiteSpace(); // , is optional
        }
        return object;
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
            }else if(JsonValue.isPunctuatorChar(current)){
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

    private JsonValue readString() throws IOException{
        return new JsonString(readStringInternal(true));
    }

    private String readStringInternal(boolean allowML) throws IOException{
        // callees make sure that (current=='"' || current=='\'')
        int exitCh = current;
        read();
        startCapture();
        while(current != exitCh){
            if(current == '\\') readEscape();
            else if(current < 0x20) throw expected("valid string character");
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

    static JsonValue tryParseNumber(StringBuilder value, boolean stopAtNext) throws IOException{
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

        return new JsonNumber(Double.parseDouble(value.substring(0, last)));
    }

    static JsonValue tryParseNumber(String value, boolean stopAtNext) throws IOException{
        return tryParseNumber(new StringBuilder(value), stopAtNext);
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

    private ParseException expected(String expected){
        if(isEndOfText()){
            return error("Unexpected end of input");
        }
        return error("Expected " + expected);
    }

    private ParseException error(String message){
        int column = index - lineOffset;
        int offset = isEndOfText() ? index : index - 1;
        return new ParseException(message, offset, line, column - 1);
    }

    static boolean isWhiteSpace(int ch){
        return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r';
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
