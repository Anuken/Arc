package arc.util;

import sun.misc.*;

import java.lang.reflect.*;
import java.nio.*;

public class UnsafeBuffers{
    private static final Unsafe unsafe;
    private static final long bufferOffset;

    static{
        try{
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (Unsafe)theUnsafe.get(null);

            Field addressField = Buffer.class.getDeclaredField("address");
            bufferOffset = unsafe.objectFieldOffset(addressField);
        }catch(Exception e){
            throw new ExceptionInInitializerError("Cannot access Unsafe");
        }
    }

    public static void copy(Buffer src, int srcPos, Buffer dst, int dstPos, int length){
        long addressSrc = unsafe.getLong(src, bufferOffset);
        long addressDst = unsafe.getLong(dst, bufferOffset);

        unsafe.copyMemory(addressSrc + srcPos, addressDst + dstPos, length);
    }

}

