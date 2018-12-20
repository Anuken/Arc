package io.anuke.arc.utils.io;

import io.anuke.arc.utils.io.StreamUtils.OptimizedByteArrayOutputStream;

/**
 * Extends {@link DataOutput} that writes bytes to a byte array.
 * @author Nathan Sweet
 */
public class DataBuffer extends DataOutput{
    private final OptimizedByteArrayOutputStream outStream;

    public DataBuffer(){
        this(32);
    }

    public DataBuffer(int initialSize){
        super(new OptimizedByteArrayOutputStream(initialSize));
        outStream = (OptimizedByteArrayOutputStream)out;
    }

    /** Returns the backing array, which has 0 to {@link #size()} items. */
    public byte[] getBuffer(){
        return outStream.getBuffer();
    }

    public byte[] toArray(){
        return outStream.toByteArray();
    }
}
