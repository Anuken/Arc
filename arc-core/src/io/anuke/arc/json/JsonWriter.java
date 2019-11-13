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

import java.io.*;


class JsonWriter{

    boolean format;

    public JsonWriter(boolean format){
        this.format = format;
    }

    void nl(Writer tw, int level) throws IOException{
        if(format){
            tw.write(JsonValue.eol);
            for(int i = 0; i < level; i++) tw.write("  ");
        }
    }

    public void save(JsonValue value, Writer tw, int level) throws IOException{
        boolean following = false;
        switch(value.getType()){
            case object:
                JsonObject obj = value.asObject();
                if(obj.size() > 0) nl(tw, level);
                tw.write('{');
                for(JsonObject.Member pair : obj){
                    if(following) tw.write(",");
                    nl(tw, level + 1);
                    tw.write('\"');
                    tw.write(escapeString(pair.getName()));
                    tw.write("\":");
                    //save(, tw, level+1, " ", false);
                    JsonValue v = pair.getValue();
                    JsonType vType = v.getType();
                    if(format && vType != JsonType.array && vType != JsonType.object) tw.write(" ");
                    if(v == null) tw.write("null");
                    else save(v, tw, level + 1);
                    following = true;
                }
                if(following) nl(tw, level);
                tw.write('}');
                break;
            case array:
                JsonArray arr = value.asArray();
                int n = arr.size();
                if(n > 0) nl(tw, level);
                tw.write('[');
                for(int i = 0; i < n; i++){
                    if(following) tw.write(",");
                    JsonValue v = arr.get(i);
                    JsonType vType = v.getType();
                    if(vType != JsonType.array && vType != JsonType.object) nl(tw, level + 1);
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

    static String escapeName(String name){
        boolean needsEscape = name.length() == 0;
        for(char ch : name.toCharArray()){
            if(HjsonParser.isWhiteSpace(ch) || ch == '{' || ch == '}' || ch == '[' || ch == ']' || ch == ',' || ch == ':'){
                needsEscape = true;
                break;
            }
        }
        if(needsEscape) return "\"" + JsonWriter.escapeString(name) + "\"";
        else return name;
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
            case '\"':
                return "\\\"";
            case '\t':
                return "\\t";
            case '\n':
                return "\\n";
            case '\r':
                return "\\r";
            case '\f':
                return "\\f";
            case '\b':
                return "\\b";
            case '\\':
                return "\\\\";
            default:
                return null;
        }
    }
}
