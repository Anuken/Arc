package arc.util.io;

import arc.util.ArcAnnotate.*;

import java.io.*;

/** A wrapper for DataInput with more concise method names and no IOExceptions. */
public class Reads implements Closeable{
    private static Reads instance = new Reads(null);

    public @NonNull DataInput input;

    public Reads(DataInput input){
        this.input = input;
    }

    public static Reads get(DataInput input){
        instance.input = input;
        return instance;
    }

    /** read long */
    public long l(){
        try{
            return input.readLong();
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    /** read int */
    public int i(){
        try{
            return input.readInt();
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    /** read short */
    public short s(){
        try{
            return input.readShort();
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    /** read unsigned short */
    public int us(){
        try{
            return input.readUnsignedShort();
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    /** read byte */
    public byte b(){
        try{
            return input.readByte();
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    /** read byte array */
    public byte[] b(byte[] array){
        return b(array, 0, array.length);
    }

    /** read byte array w/ offset */
    public byte[] b(byte[] array, int offset, int length){
        try{
            input.readFully(array, offset, length);
            return array;
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    /** read unsigned byte */
    public int ub(){
        try{
            return input.readUnsignedByte();
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    /** read boolean */
    public boolean bool(){
        try{
            return input.readBoolean();
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    /** read float */
    public float f(){
        try{
            return input.readFloat();
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    /** read double */
    public double d(){
        try{
            return input.readDouble();
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    /** read string (UTF) */
    public String str(){
        try{
            return input.readUTF();
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close(){
        if(input instanceof Closeable){
            try{
                ((Closeable)input).close();
            }catch(IOException e){
                throw new RuntimeException(e);
            }
        }
    }
}
