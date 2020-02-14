package arc.util.io;

import java.io.DataOutput;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/** DataOutput wrapper of ByteBuffer. */
public class ByteBufferOutput implements DataOutput{
    public ByteBuffer buffer;

    /** Wraps the specified ByteBuffer. */
    public ByteBufferOutput(ByteBuffer buffer){
        this.buffer = buffer;
    }

    /** {@link #setBuffer} must be called before this object can be used. */
    public ByteBufferOutput(){
    }

    public void setBuffer(ByteBuffer buffer){
        this.buffer = buffer;
    }

    @Override
    public void write(int i){
        buffer.put((byte) i);
    }

    @Override
    public void write(byte[] bytes){
        buffer.put(bytes);
    }

    @Override
    public void write(byte[] bytes, int i, int i1){
        buffer.put(bytes, i, i1);
    }

    @Override
    public void writeBoolean(boolean b){
        buffer.put(b ? (byte) 1 : 0);
    }

    @Override
    public void writeByte(int i){
        buffer.put((byte) i);
    }

    @Override
    public void writeShort(int i){
        buffer.putShort((short) i);
    }

    @Override
    public void writeChar(int i){
        buffer.putChar((char) i);
    }

    @Override
    public void writeInt(int i){
        buffer.putInt(i);
    }

    @Override
    public void writeLong(long l){
        buffer.putLong(l);
    }

    @Override
    public void writeFloat(float v){
        buffer.putFloat(v);
    }

    @Override
    public void writeDouble(double v){
        buffer.putDouble(v);
    }

    @Override
    public void writeBytes(String s){
        throw new RuntimeException("Stub!");
    }

    @Override
    public void writeChars(String s){
        throw new RuntimeException("Stub!");
    }

    @Override
    public void writeUTF(String s){
        try{
            byte[] bytes = s.getBytes("UTF-8");
            if(bytes.length >= Short.MAX_VALUE) throw new IllegalArgumentException("Input string is too long!");
            buffer.putShort((short)bytes.length);
            buffer.put(bytes);
        }catch(UnsupportedEncodingException e){
            throw new RuntimeException(e);
        }
    }
}
