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

class JsonLiteral extends JsonValue{
    static final JsonValue NULL = new JsonLiteral(Iv.N);
    static final JsonValue TRUE = new JsonLiteral(Iv.T);
    static final JsonValue FALSE = new JsonLiteral(Iv.F);

    private final Iv value;

    private JsonLiteral(Iv value){
        this.value = value;
    }

    @Override
    public String toString(){
        switch(value){
            case T:
                return "true";
            case F:
                return "false";
            case N:
                return "null";
            default:
                return null;
        }
    }

    @Override
    public int hashCode(){
        return value.hashCode();
    }

    @Override
    public JsonType getType(){
        return value == Iv.N ? JsonType.nil : JsonType.bool;
    }

    @Override
    public boolean isNull(){
        return value == Iv.N;
    }

    @Override
    public boolean isTrue(){
        return value == Iv.T;
    }

    @Override
    public boolean isFalse(){
        return value == Iv.F;
    }

    @Override
    public boolean isBoolean(){
        return value != Iv.N;
    }

    @Override
    public boolean asBoolean(){
        return value == Iv.N ? super.asBoolean() : value == Iv.T;
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
        JsonLiteral other = (JsonLiteral)object;
        return value == other.value;
    }

    enum Iv{T, F, N}
}
