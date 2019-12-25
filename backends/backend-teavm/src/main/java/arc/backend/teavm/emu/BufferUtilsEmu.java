package arc.backend.teavm.emu;

import com.badlogic.gdx.utils.BufferUtils;
import arc.backend.teavm.plugin.Annotations.*;
import arc.util.*;

import java.nio.*;


@Emulate(BufferUtils.class)
@SuppressWarnings("unused")
public class BufferUtilsEmu{

    private static void freeMemory(ByteBuffer buffer){
    }

    private static ByteBuffer newDisposableByteBuffer(int numBytes){
        return ByteBuffer.wrap(new byte[numBytes]);
    }

    private static void copyJni(float[] src, Buffer dst, int numFloats, int offset){
        dst.position(0);
        dst.limit(dst.capacity());
        FloatBuffer floatDst;
        if(dst instanceof FloatBuffer){
            floatDst = (FloatBuffer)dst;
            floatDst = floatDst.duplicate();
        }else if(dst instanceof ByteBuffer){
            ByteBuffer byteDst = (ByteBuffer)dst;
            floatDst = byteDst.asFloatBuffer();
        }else{
            throw new ArcRuntimeException("Target buffer of type " + dst.getClass().getName() + " is not supported");
        }
        floatDst.put(src, offset, numFloats);
    }

    private static void copyJni(float[] src, int srcOffset, Buffer dst, int numFloats, int offset){
        dst.position(0);
        dst.limit(dst.capacity());
        FloatBuffer floatDst;
        if(dst instanceof FloatBuffer){
            floatDst = (FloatBuffer)dst;
            floatDst = floatDst.duplicate();
        }else if(dst instanceof ByteBuffer){
            ByteBuffer byteDst = (ByteBuffer)dst;
            floatDst = byteDst.asFloatBuffer();
        }else{
            throw new ArcRuntimeException("Target buffer of type " + dst.getClass().getName() + " is not supported");
        }

        floatDst.put(src, offset, numFloats);
    }
}
