/*******************************************************************************
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

import java.util.regex.*;

/**
 * Provides standard DSF providers.
 */
public class HjsonDsf{
    private HjsonDsf(){
    }

    /**
     * Returns a math DSF provider
     * @return DSF provider
     */
    public static HjsonDsfProvider math(){
        return new DsfMath();
    }

    /**
     * Returns a hex DSF provider
     * @param stringify true to output all integers as hex values
     * @return DSF provider
     */
    public static HjsonDsfProvider hex(boolean stringify){
        return new DsfHex(stringify);
    }

    static boolean isInvalidDsfChar(char c){
        return c == '{' || c == '}' || c == '[' || c == ']' || c == ',';
    }

    static JsonValue parse(HjsonDsfProvider[] dsfProviders, String value){
        for(HjsonDsfProvider dsf : dsfProviders){
            try{
                JsonValue res = dsf.parse(value);
                if(res != null) return res;
            }catch(Exception exception){
                throw new RuntimeException("DSF-" + dsf.getName() + " failed; " + exception.getMessage());
            }
        }
        return new JsonString(value);
    }

    static String stringify(HjsonDsfProvider[] dsfProviders, JsonValue value){
        for(HjsonDsfProvider dsf : dsfProviders){
            try{
                String text = dsf.stringify(value);
                if(text != null){
                    boolean isInvalid = false;
                    char[] textc = text.toCharArray();
                    for(char ch : textc){
                        if(isInvalidDsfChar(ch)){
                            isInvalid = true;
                            break;
                        }
                    }
                    if(isInvalid || text.length() == 0 || textc[0] == '"')
                        throw new Exception("value may not be empty, start with a quote or contain a punctuator character except colon: " + text);
                    return text;
                }
            }catch(Exception exception){
                throw new RuntimeException("DSF-" + dsf.getName() + " failed; " + exception.getMessage());
            }
        }
        return null;
    }

}

class DsfMath implements HjsonDsfProvider{
    @Override
    public String getName(){
        return "math";
    }

    @Override
    public String getDescription(){
        return "support for Inf/inf, -Inf/-inf, Nan/naN and -0";
    }

    @Override
    public JsonValue parse(String text){
        switch(text){
            case "+inf":
            case "inf":
            case "+Inf":
            case "Inf":
                return new JsonNumber(Double.POSITIVE_INFINITY);
            case "-inf":
            case "-Inf":
                return new JsonNumber(Double.NEGATIVE_INFINITY);
            case "nan":
            case "NaN":
                return new JsonNumber(Double.NaN);
            default:
                return null;
        }
    }

    @Override
    public String stringify(JsonValue value){
        if(!value.isNumber()) return null;
        double val = value.asDouble();
        if(val == Double.POSITIVE_INFINITY) return "Inf";
        else if(val == Double.NEGATIVE_INFINITY) return "-Inf";
        else if(Double.isNaN(val)) return "NaN";
        else if(val == 0.0 && 1 / val == Double.NEGATIVE_INFINITY) return "-0";
        else return null;
    }
}

class DsfHex implements HjsonDsfProvider{
    boolean stringify;
    static Pattern isHex = Pattern.compile("^0x[0-9A-Fa-f]+$");

    public DsfHex(boolean stringify){
        this.stringify = stringify;
    }

    @Override
    public String getName(){
        return "hex";
    }

    @Override
    public String getDescription(){
        return "parse hexadecimal numbers prefixed with 0x";
    }

    @Override
    public JsonValue parse(String text){
        if(isHex.matcher(text).find())
            return new JsonNumber(Long.parseLong(text.substring(2), 16));
        else
            return null;
    }

    @Override
    public String stringify(JsonValue value){
        if(stringify && value.isNumber() && value.asLong() == value.asDouble()){
            return "0x" + Long.toHexString(value.asLong());
        }else{
            return null;
        }
    }
}
