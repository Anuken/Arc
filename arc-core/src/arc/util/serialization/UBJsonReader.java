package arc.util.serialization;

import arc.files.*;
import arc.util.*;

import java.io.*;

/** Reads UBJSON from a stream into a {@link Jval} tree. */
public class UBJsonReader{

    public static Jval read(Fi file){
        try(InputStream in = file.read(8192)){
            return read(in);
        }catch(IOException e){
            throw new ArcRuntimeException("Error parsing file: " + file, e);
        }
    }

    public static Jval read(InputStream input) throws IOException{
        DataInputStream in = input instanceof DataInputStream ? (DataInputStream)input : new DataInputStream(input);
        return readValue(in, in.readByte());
    }

    private static Jval readValue(DataInputStream in, byte type) throws IOException{
        switch(type){
            case '[':
                return readArray(in);
            case '{':
                return readObject(in);
            case 'Z':
                return Jval.NULL;
            case 'T':
                return Jval.TRUE;
            case 'F':
                return Jval.FALSE;
            case 'B':
            case 'U':
                return Jval.valueOf(readUChar(in));
            case 'i':
                return Jval.valueOf(in.readByte());
            case 'I':
                return Jval.valueOf(in.readShort());
            case 'l':
                return Jval.valueOf(in.readInt());
            case 'L':
                return Jval.valueOf(in.readLong());
            case 'd':
                return Jval.valueOf(in.readFloat());
            case 'D':
                return Jval.valueOf(in.readDouble());
            case 's':
            case 'S':
                return Jval.valueOf(readString(in, type));
            case 'C':
                return Jval.valueOf(String.valueOf(in.readChar()));
            default:
                throw new ArcRuntimeException("Unrecognized data type: " + (char)type);
        }
    }

    private static Jval readArray(DataInputStream in) throws IOException{
        Jval result = Jval.newArray();
        byte type = in.readByte();
        while(type != ']'){
            result.add(readValue(in, type));
            type = in.readByte();
        }
        return result;
    }

    private static Jval readObject(DataInputStream in) throws IOException{
        Jval result = Jval.newObject();
        byte type = in.readByte();
        while(type != '}'){
            String key = readBytes(in, readSize(in, type));
            result.add(key, readValue(in, in.readByte()));
            type = in.readByte();
        }
        return result;
    }

    private static String readString(DataInputStream in, byte type) throws IOException{
        long size = type == 's' ? readUChar(in) : readSize(in, in.readByte());
        return readBytes(in, size);
    }

    private static String readBytes(DataInputStream in, long size) throws IOException{
        byte[] data = new byte[(int)size];
        in.readFully(data);
        return new String(data, Strings.utf8);
    }

    private static long readSize(DataInputStream in, byte type) throws IOException{
        switch(type){
            case 'i':
                return readUChar(in);
            case 'I':
                return readUShort(in);
            case 'l':
                return in.readInt() & 0xFFFFFFFFL;
            case 'L':
                return in.readLong();
            default:
                throw new ArcRuntimeException("Unrecognized size type: " + (char)type);
        }
    }

    private static int readUChar(DataInputStream in) throws IOException{
        return in.readByte() & 0xFF;
    }

    private static int readUShort(DataInputStream in) throws IOException{
        return in.readShort() & 0xFFFF;
    }
}