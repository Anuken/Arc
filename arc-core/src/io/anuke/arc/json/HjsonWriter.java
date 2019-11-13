/*******************************************************************************
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
import java.util.regex.*;

class HjsonWriter{
    private HjsonDsfProvider[] dsfProviders;

    static Pattern needsEscapeName = Pattern.compile("[,\\{\\[\\}\\]\\s:#\"']|//|/\\*");

    public HjsonWriter(HjsonOptions options){
        if(options != null){
            dsfProviders = options.getDsfProviders();
        }else{
            dsfProviders = new HjsonDsfProvider[0];
        }
    }

    void nl(Writer tw, int level) throws IOException{
        tw.write(JsonValue.eol);
        for(int i = 0; i < level; i++) tw.write("  ");
    }

    public void save(JsonValue value, Writer tw, int level, String separator, boolean noIndent) throws IOException{
        if(value == null){
            tw.write(separator);
            tw.write("null");
            return;
        }

        // check for DSF
        String dsfValue = HjsonDsf.stringify(dsfProviders, value);
        if(dsfValue != null){
            tw.write(separator);
            tw.write(dsfValue);
            return;
        }

        switch(value.getType()){
            case object:
                JsonObject obj = value.asObject();
                if(!noIndent){
                    if(obj.size() > 0) nl(tw, level);
                    else tw.write(separator);
                }
                tw.write('{');

                for(JsonObject.Member pair : obj){
                    nl(tw, level + 1);
                    tw.write(escapeName(pair.getName()));
                    tw.write(":");
                    save(pair.getValue(), tw, level + 1, " ", false);
                }

                if(obj.size() > 0) nl(tw, level);
                tw.write('}');
                break;
            case array:
                JsonArray arr = value.asArray();
                int n = arr.size();
                if(!noIndent){
                    if(n > 0) nl(tw, level);
                    else tw.write(separator);
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
            return "\"" + JsonWriter.escapeString(name) + "\"";
        else
            return name;
    }

    void writeString(String value, Writer tw, int level, String separator) throws IOException{
        if(value.length() == 0){
            tw.write(separator + "\"\"");
            return;
        }

        char left = value.charAt(0), right = value.charAt(value.length() - 1);
        char left1 = value.length() > 1 ? value.charAt(1) : '\0', left2 = value.length() > 2 ? value.charAt(2) : '\0';
        boolean doEscape = false;
        char[] valuec = value.toCharArray();
        for(char ch : valuec){
            if(needsQuotes(ch)){
                doEscape = true;
                break;
            }
        }

        if(doEscape ||
        HjsonParser.isWhiteSpace(left) || HjsonParser.isWhiteSpace(right) ||
        left == '"' ||
        left == '\'' ||
        left == '#' ||
        left == '/' && (left1 == '*' || left1 == '/') ||
        JsonValue.isPunctuatorChar(left) ||
        HjsonParser.tryParseNumber(value, true) != null ||
        startsWithKeyword(value)){
            // If the String contains no control characters, no quote characters, and no
            // backslash characters, then we can safely slap some quotes around it.
            // Otherwise we first check if the String can be expressed in multiline
            // format or we must replace the offending characters with safe escape
            // sequences.

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
                }else if(!HjsonParser.isWhiteSpace(ch)) allWhite = false;
            }
            if(noEscapeML && !allWhite && !value.contains("'''")) writeMLString(value, tw, level, separator);
            else tw.write(separator + "\"" + JsonWriter.escapeString(value) + "\"");
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
        while(p < text.length() && HjsonParser.isWhiteSpace(text.charAt(p))) p++;
        if(p == text.length()) return true;
        char ch = text.charAt(p);
        return ch == ',' || ch == '}' || ch == ']' || ch == '#' || ch == '/' && (text.length() > p + 1 && (text.charAt(p + 1) == '/' || text.charAt(p + 1) == '*'));
    }

    static boolean needsQuotes(char c){
        switch(c){
            case '\t':
            case '\f':
            case '\b':
            case '\n':
            case '\r':
                return true;
            default:
                return false;
        }
    }

    static boolean needsEscape(char c){
        switch(c){
            case '\"':
            case '\\':
                return true;
            default:
                return needsQuotes(c);
        }
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
