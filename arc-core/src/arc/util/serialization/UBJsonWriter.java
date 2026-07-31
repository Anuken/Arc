package arc.util.serialization;

import arc.struct.*;
import arc.util.*;

import java.io.*;

/** Writes a {@link Jval} tree as UBJSON to a stream. */
public class UBJsonWriter{

    public static void write(Jval value, OutputStream output) throws IOException{
        DataOutputStream out = output instanceof DataOutputStream ? (DataOutputStream)output : new DataOutputStream(output);
        writeValue(value, out);
        out.flush();
    }

    private static void writeValue(Jval value, DataOutputStream out) throws IOException{
        switch(value.getType()){
            case object:
                writeObject(value, out);
                break;
            case array:
                writeArray(value, out);
                break;
            case string:
                writeString(value.asString(), out);
                break;
            case number:
                writeNumber(value.asDouble(), out);
                break;
            case bool:
                out.writeByte(value.isTrue() ? 'T' : 'F');
                break;
            default:
                out.writeByte('Z');
                break;
        }
    }

    private static void writeObject(Jval value, DataOutputStream out) throws IOException{
        out.writeByte('{');
        for(ObjectMap.Entry<String, Jval> entry : value.asObject()){
            writeSize(entry.key.getBytes(Strings.utf8).length, out);
            out.write(entry.key.getBytes(Strings.utf8));
            writeValue(entry.value, out);
        }
        out.writeByte('}');
    }

    private static void writeArray(Jval value, DataOutputStream out) throws IOException{
        out.writeByte('[');
        Jval.JsonArray arr = value.asArray();
        for(int i = 0; i < arr.size; i++) writeValue(arr.get(i), out);
        out.writeByte(']');
    }

    private static void writeString(String value, DataOutputStream out) throws IOException{
        byte[] bytes = value.getBytes(Strings.utf8);
        out.writeByte('S');
        writeSize(bytes.length, out);
        out.write(bytes);
    }

    private static void writeSize(int size, DataOutputStream out) throws IOException{
        if(size <= Byte.MAX_VALUE){
            out.writeByte('i');
            out.writeByte(size);
        }else if(size <= Short.MAX_VALUE){
            out.writeByte('I');
            out.writeShort(size);
        }else{
            out.writeByte('l');
            out.writeInt(size);
        }
    }

    private static void writeNumber(double value, DataOutputStream out) throws IOException{
        if(value == Math.rint(value) && !Double.isInfinite(value)){
            long l = (long)value;
            if(l >= Byte.MIN_VALUE && l <= Byte.MAX_VALUE){
                out.writeByte('i');
                out.writeByte((int)l);
            }else if(l >= Short.MIN_VALUE && l <= Short.MAX_VALUE){
                out.writeByte('I');
                out.writeShort((int)l);
            }else if(l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE){
                out.writeByte('l');
                out.writeInt((int)l);
            }else{
                out.writeByte('L');
                out.writeLong(l);
            }
        }else{
            out.writeByte('D');
            out.writeDouble(value);
        }
    }
}