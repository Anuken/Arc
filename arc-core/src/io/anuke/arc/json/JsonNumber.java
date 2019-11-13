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

import java.math.*;

class JsonNumber extends JsonValue{
    private final double value;

    JsonNumber(double value){
        this.value = value;
    }

    @Override
    public String toString(){
        long l = (long)value;
        if(l == value) return Long.toString(l);
        String res = BigDecimal.valueOf(value).toEngineeringString();
        if(res.endsWith(".0")) return res.substring(0, res.length() - 2);
        else if(res.contains("E")){
            res = Double.toString(value);
            res = res.replace("E-", "e-").replace("E", "e+");
        }
        return res;
    }

    @Override
    public JsonType getType(){
        return JsonType.number;
    }

    @Override
    public boolean isNumber(){
        return true;
    }

    @Override
    public int asInt(){
        return (int)value;
    }

    @Override
    public long asLong(){
        return (long)value;
    }

    @Override
    public float asFloat(){
        return (float)value;
    }

    @Override
    public double asDouble(){
        return value;
    }

    @Override
    public int hashCode(){
        return Double.valueOf(value).hashCode();
    }

    @Override
    public boolean equals(Object object){
        if(this == object){
            return true;
        }
        if(object == null){
            return false;
        }
        if(getClass() != object.getClass()){
            return false;
        }
        JsonNumber other = (JsonNumber)object;
        return value == other.value;
    }
}
