package arc.util;

import java.nio.*;

public class Java16Buffers{
    public static void copy(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int len){
        dst.put(dstPos, src, srcPos, len);
    }
}

