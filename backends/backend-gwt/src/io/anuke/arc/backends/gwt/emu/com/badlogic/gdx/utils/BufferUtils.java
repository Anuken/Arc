package io.anuke.arc.utils;

import io.anuke.arc.math.Matrix3;
import com.google.gwt.core.client.GWT;

import java.nio.*;

/**
 * Class with static helper methods to increase the speed of array/direct buffer and direct buffer/direct buffer transfers
 * @author mzechner
 */
public final class BufferUtils{
    /**
     * Copies numFloats floats from src starting at offset to dst. Dst is assumed to be a direct {@link Buffer}. The method will
     * crash if that is not the case. The position and limit of the buffer are ignored, the copy is placed at position 0 in the
     * buffer. After the copying process the position of the buffer is set to 0 and its limit is set to numFloats * 4 if it is a
     * ByteBuffer and numFloats if it is a FloatBuffer. In case the Buffer is neither a ByteBuffer nor a FloatBuffer the limit is
     * not set. This is an expert method, use at your own risk.
     * @param src the source array
     * @param dst the destination buffer, has to be a direct Buffer
     * @param numFloats the number of floats to copy
     * @param offset the offset in src to start copying from
     */
    public static void copy(float[] src, Buffer dst, int numFloats, int offset){
        FloatBuffer floatBuffer = asFloatBuffer(dst);

        floatBuffer.clear();
        dst.position(0);
        floatBuffer.put(src, offset, numFloats);
        dst.position(0);
        if(dst instanceof ByteBuffer)
            dst.limit(numFloats << 2);
        else
            dst.limit(numFloats);
    }

    /**
     * Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer} instance's
     * {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same, the limit
     * will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error checking is
     * performed</b>.
     * @param src the source array.
     * @param srcOffset the offset into the source array.
     * @param dst the destination Buffer, its position is used as an offset.
     * @param numElements the number of elements to copy.
     */
    public static void copy(byte[] src, int srcOffset, Buffer dst, int numElements){
        if(!(dst instanceof ByteBuffer)) throw new ArcRuntimeException("dst must be a ByteBuffer");

        ByteBuffer byteBuffer = (ByteBuffer)dst;
        int oldPosition = byteBuffer.position();
        byteBuffer.limit(oldPosition + numElements);
        byteBuffer.put(src, srcOffset, numElements);
        byteBuffer.position(oldPosition);
    }

    /**
     * Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer} instance's
     * {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same, the limit
     * will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error checking is
     * performed</b>.
     * @param src the source array.
     * @param srcOffset the offset into the source array.
     * @param dst the destination Buffer, its position is used as an offset.
     * @param numElements the number of elements to copy.
     */
    public static void copy(short[] src, int srcOffset, Buffer dst, int numElements){
        ShortBuffer buffer = null;
        if(dst instanceof ByteBuffer)
            buffer = ((ByteBuffer)dst).asShortBuffer();
        else if(dst instanceof ShortBuffer) buffer = (ShortBuffer)dst;
        if(buffer == null) throw new ArcRuntimeException("dst must be a ByteBuffer or ShortBuffer");

        int oldPosition = buffer.position();
        buffer.limit(oldPosition + numElements);
        buffer.put(src, srcOffset, numElements);
        buffer.position(oldPosition);
    }

    /**
     * Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer} instance's
     * {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same, the limit
     * will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error checking is
     * performed</b>.
     * @param src the source array.
     * @param srcOffset the offset into the source array.
     * @param dst the destination Buffer, its position is used as an offset.
     * @param numElements the number of elements to copy.
     */
    public static void copy(char[] src, int srcOffset, Buffer dst, int numElements){
        CharBuffer buffer = null;
        if(dst instanceof ByteBuffer)
            buffer = ((ByteBuffer)dst).asCharBuffer();
        else if(dst instanceof CharBuffer) buffer = (CharBuffer)dst;
        if(buffer == null) throw new ArcRuntimeException("dst must be a ByteBuffer or CharBuffer");

        int oldPosition = buffer.position();
        buffer.limit(oldPosition + numElements);
        buffer.put(src, srcOffset, numElements);
        buffer.position(oldPosition);
    }

    /**
     * Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer} instance's
     * {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same, the limit
     * will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error checking is
     * performed</b>.
     * @param src the source array.
     * @param srcOffset the offset into the source array.
     * @param dst the destination Buffer, its position is used as an offset.
     * @param numElements the number of elements to copy.
     */
    public static void copy(int[] src, int srcOffset, Buffer dst, int numElements){
        IntBuffer buffer = null;
        if(dst instanceof ByteBuffer)
            buffer = ((ByteBuffer)dst).asIntBuffer();
        else if(dst instanceof IntBuffer) buffer = (IntBuffer)dst;
        if(buffer == null) throw new ArcRuntimeException("dst must be a ByteBuffer or IntBuffer");

        int oldPosition = buffer.position();
        buffer.limit(oldPosition + numElements);
        buffer.put(src, srcOffset, numElements);
        buffer.position(oldPosition);
    }

    /**
     * Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer} instance's
     * {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same, the limit
     * will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error checking is
     * performed</b>.
     * @param src the source array.
     * @param srcOffset the offset into the source array.
     * @param dst the destination Buffer, its position is used as an offset.
     * @param numElements the number of elements to copy.
     */
    public static void copy(long[] src, int srcOffset, Buffer dst, int numElements){
        LongBuffer buffer = null;
        if(dst instanceof ByteBuffer)
            buffer = ((ByteBuffer)dst).asLongBuffer();
        else if(dst instanceof LongBuffer) buffer = (LongBuffer)dst;
        if(buffer == null) throw new ArcRuntimeException("dst must be a ByteBuffer or LongBuffer");

        int oldPosition = buffer.position();
        buffer.limit(oldPosition + numElements);
        buffer.put(src, srcOffset, numElements);
        buffer.position(oldPosition);
    }

    /**
     * Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer} instance's
     * {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same, the limit
     * will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error checking is
     * performed</b>.
     * @param src the source array.
     * @param srcOffset the offset into the source array.
     * @param dst the destination Buffer, its position is used as an offset.
     * @param numElements the number of elements to copy.
     */
    public static void copy(float[] src, int srcOffset, Buffer dst, int numElements){
        FloatBuffer buffer = asFloatBuffer(dst);

        int oldPosition = buffer.position();
        buffer.limit(oldPosition + numElements);
        buffer.put(src, srcOffset, numElements);
        buffer.position(oldPosition);
    }

    /**
     * Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer} instance's
     * {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same, the limit
     * will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error checking is
     * performed</b>.
     * @param src the source array.
     * @param srcOffset the offset into the source array.
     * @param dst the destination Buffer, its position is used as an offset.
     * @param numElements the number of elements to copy.
     */
    public static void copy(double[] src, int srcOffset, Buffer dst, int numElements){
        DoubleBuffer buffer = null;
        if(dst instanceof ByteBuffer)
            buffer = ((ByteBuffer)dst).asDoubleBuffer();
        else if(dst instanceof DoubleBuffer) buffer = (DoubleBuffer)dst;
        if(buffer == null) throw new ArcRuntimeException("dst must be a ByteBuffer or DoubleBuffer");

        int oldPosition = buffer.position();
        buffer.limit(oldPosition + numElements);
        buffer.put(src, srcOffset, numElements);
        buffer.position(oldPosition);
    }

    /**
     * Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer} instance's
     * {@link Buffer#position()} is used to define the offset into the Buffer itself. The position and limit will stay the same.
     * <b>The Buffer must be a direct Buffer with native byte order. No error checking is performed</b>.
     * @param src the source array.
     * @param srcOffset the offset into the source array.
     * @param numElements the number of elements to copy.
     * @param dst the destination Buffer, its position is used as an offset.
     */
    public static void copy(char[] src, int srcOffset, int numElements, Buffer dst){
        CharBuffer buffer = null;
        if(dst instanceof ByteBuffer)
            buffer = ((ByteBuffer)dst).asCharBuffer();
        else if(dst instanceof CharBuffer) buffer = (CharBuffer)dst;
        if(buffer == null) throw new ArcRuntimeException("dst must be a ByteBuffer or CharBuffer");

        int oldPosition = buffer.position();
        buffer.put(src, srcOffset, numElements);
        buffer.position(oldPosition);
    }

    /**
     * Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer} instance's
     * {@link Buffer#position()} is used to define the offset into the Buffer itself. The position and limit will stay the same.
     * <b>The Buffer must be a direct Buffer with native byte order. No error checking is performed</b>.
     * @param src the source array.
     * @param srcOffset the offset into the source array.
     * @param numElements the number of elements to copy.
     * @param dst the destination Buffer, its position is used as an offset.
     */
    public static void copy(int[] src, int srcOffset, int numElements, Buffer dst){
        IntBuffer buffer = null;
        if(dst instanceof ByteBuffer)
            buffer = ((ByteBuffer)dst).asIntBuffer();
        else if(dst instanceof IntBuffer) buffer = (IntBuffer)dst;
        if(buffer == null) throw new ArcRuntimeException("dst must be a ByteBuffer or IntBuffer");

        int oldPosition = buffer.position();
        buffer.put(src, srcOffset, numElements);
        buffer.position(oldPosition);
    }

    /**
     * Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer} instance's
     * {@link Buffer#position()} is used to define the offset into the Buffer itself. The position and limit will stay the same.
     * <b>The Buffer must be a direct Buffer with native byte order. No error checking is performed</b>.
     * @param src the source array.
     * @param srcOffset the offset into the source array.
     * @param numElements the number of elements to copy.
     * @param dst the destination Buffer, its position is used as an offset.
     */
    public static void copy(long[] src, int srcOffset, int numElements, Buffer dst){
        LongBuffer buffer = null;
        if(dst instanceof ByteBuffer)
            buffer = ((ByteBuffer)dst).asLongBuffer();
        else if(dst instanceof LongBuffer) buffer = (LongBuffer)dst;
        if(buffer == null) throw new ArcRuntimeException("dst must be a ByteBuffer or LongBuffer");

        int oldPosition = buffer.position();
        buffer.put(src, srcOffset, numElements);
        buffer.position(oldPosition);
    }

    /**
     * Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer} instance's
     * {@link Buffer#position()} is used to define the offset into the Buffer itself. The position and limit will stay the same.
     * <b>The Buffer must be a direct Buffer with native byte order. No error checking is performed</b>.
     * @param src the source array.
     * @param srcOffset the offset into the source array.
     * @param numElements the number of elements to copy.
     * @param dst the destination Buffer, its position is used as an offset.
     */
    public static void copy(float[] src, int srcOffset, int numElements, Buffer dst){
        FloatBuffer buffer = asFloatBuffer(dst);
        int oldPosition = buffer.position();
        buffer.put(src, srcOffset, numElements);
        buffer.position(oldPosition);
    }

    /**
     * Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer} instance's
     * {@link Buffer#position()} is used to define the offset into the Buffer itself. The position and limit will stay the same.
     * <b>The Buffer must be a direct Buffer with native byte order. No error checking is performed</b>.
     * @param src the source array.
     * @param srcOffset the offset into the source array.
     * @param numElements the number of elements to copy.
     * @param dst the destination Buffer, its position is used as an offset.
     */
    public static void copy(double[] src, int srcOffset, int numElements, Buffer dst){
        DoubleBuffer buffer = null;
        if(dst instanceof ByteBuffer)
            buffer = ((ByteBuffer)dst).asDoubleBuffer();
        else if(dst instanceof DoubleBuffer) buffer = (DoubleBuffer)dst;
        if(buffer == null) throw new ArcRuntimeException("dst must be a ByteBuffer or DoubleBuffer");

        int oldPosition = buffer.position();
        buffer.put(src, srcOffset, numElements);
        buffer.position(oldPosition);
    }

    /**
     * Copies the contents of src to dst, starting from the current position of src, copying numElements elements (using the data
     * type of src, no matter the datatype of dst). The dst {@link Buffer#position()} is used as the writing offset. The position
     * of both Buffers will stay the same. The limit of the src Buffer will stay the same. The limit of the dst Buffer will be set
     * to dst.position() + numElements, where numElements are translated to the number of elements appropriate for the dst Buffer
     * data type. <b>The Buffers must be direct Buffers with native byte order. No error checking is performed</b>.
     * @param src the source Buffer.
     * @param dst the destination Buffer.
     * @param numElements the number of elements to copy.
     */
    public static void copy(Buffer src, Buffer dst, int numElements){
        int srcPos = src.position();
        int dstPos = dst.position();
        src.limit(srcPos + numElements);
        final boolean srcIsByte = src instanceof ByteBuffer;
        final boolean dstIsByte = dst instanceof ByteBuffer;
        dst.limit(dst.capacity());
        if(srcIsByte && dstIsByte)
            ((ByteBuffer)dst).put((ByteBuffer)src);
        else if((srcIsByte || src instanceof CharBuffer) && (dstIsByte || dst instanceof CharBuffer))
            (dstIsByte ? ((ByteBuffer)dst).asCharBuffer() : (CharBuffer)dst).put((srcIsByte ? ((ByteBuffer)src).asCharBuffer() : (CharBuffer)src));
        else if((srcIsByte || src instanceof ShortBuffer) && (dstIsByte || dst instanceof ShortBuffer))
            (dstIsByte ? ((ByteBuffer)dst).asShortBuffer() : (ShortBuffer)dst).put((srcIsByte ? ((ByteBuffer)src).asShortBuffer() : (ShortBuffer)src));
        else if((srcIsByte || src instanceof IntBuffer) && (dstIsByte || dst instanceof IntBuffer))
            (dstIsByte ? ((ByteBuffer)dst).asIntBuffer() : (IntBuffer)dst).put((srcIsByte ? ((ByteBuffer)src).asIntBuffer() : (IntBuffer)src));
        else if((srcIsByte || src instanceof LongBuffer) && (dstIsByte || dst instanceof LongBuffer))
            (dstIsByte ? ((ByteBuffer)dst).asLongBuffer() : (LongBuffer)dst).put((srcIsByte ? ((ByteBuffer)src).asLongBuffer() : (LongBuffer)src));
        else if((srcIsByte || src instanceof FloatBuffer) && (dstIsByte || dst instanceof FloatBuffer))
            (dstIsByte ? ((ByteBuffer)dst).asFloatBuffer() : (FloatBuffer)dst).put((srcIsByte ? ((ByteBuffer)src).asFloatBuffer() : (FloatBuffer)src));
        else if((srcIsByte || src instanceof DoubleBuffer) && (dstIsByte || dst instanceof DoubleBuffer))
            (dstIsByte ? ((ByteBuffer)dst).asDoubleBuffer() : (DoubleBuffer)dst).put((srcIsByte ? ((ByteBuffer)src).asDoubleBuffer() : (DoubleBuffer)src));
        else
            throw new ArcRuntimeException("Buffers must be of same type or ByteBuffer");
        src.position(srcPos);
        dst.flip();
        dst.position(dstPos);
    }

    private final static FloatBuffer asFloatBuffer(final Buffer data){
        FloatBuffer buffer = null;
        if(data instanceof ByteBuffer)
            buffer = ((ByteBuffer)data).asFloatBuffer();
        else if(data instanceof FloatBuffer) buffer = (FloatBuffer)data;
        if(buffer == null) throw new ArcRuntimeException("data must be a ByteBuffer or FloatBuffer");
        return buffer;
    }

    private final static float[] asFloatArray(final FloatBuffer buffer){
        final int pos = buffer.position();
        final float[] result = new float[buffer.remaining()];
        buffer.get(result);
        buffer.position(pos);
        return result;
    }

    /**
     * Multiply float vector components within the buffer with the specified matrix. The {@link Buffer#position()} is used as
     * the offset.
     * @param data The buffer to transform.
     * @param dimensions The number of components (x, y, z) of the vector (2 for xy or 3 for xyz)
     * @param strideInBytes The offset between the first and the second vector to transform
     * @param count The number of vectors to transform
     * @param matrix The matrix to multiply the vector with
     */
    public static void transform(Buffer data, int dimensions, int strideInBytes, int count, Matrix3 matrix){
        FloatBuffer buffer = asFloatBuffer(data);
        // FIXME untested code:
        final int pos = buffer.position();
        int idx = pos;
        float[] arr = asFloatArray(buffer);
        int stride = strideInBytes / 4;
        float[] m = matrix.val;
        for(int i = 0; i < count; i++){
            final float x = arr[idx];
            final float y = arr[idx + 1];
            final float z = dimensions >= 3 ? arr[idx + 2] : 1f;
            arr[idx] = x * m[0] + y * m[3] + z * m[6];
            arr[idx + 1] = x * m[1] + y * m[4] + z * m[7];
            if(dimensions >= 3)
                arr[idx + 2] = x * m[2] + y * m[5] + z * m[8];
            idx += stride;
        }
        buffer.put(arr);
        buffer.position(pos);
    }

    public static long findFloats(Buffer vertex, int strideInBytes, Buffer vertices, int numVertices){
        return findFloats(asFloatArray(asFloatBuffer(vertex)), strideInBytes, asFloatArray(asFloatBuffer(vertices)), numVertices);
    }

    public static long findFloats(float[] vertex, int strideInBytes, Buffer vertices, int numVertices){
        return findFloats(vertex, strideInBytes, asFloatArray(asFloatBuffer(vertices)), numVertices);
    }

    public static long findFloats(Buffer vertex, int strideInBytes, float[] vertices, int numVertices){
        return findFloats(asFloatArray(asFloatBuffer(vertex)), strideInBytes, vertices, numVertices);
    }

    public static long findFloats(float[] vertex, int strideInBytes, float[] vertices, int numVertices){
        final int size = strideInBytes / 4;
        for(int i = 0; i < numVertices; i++){
            final int offset = i * size;
            boolean found = true;
            for(int j = 0; !found && j < size; j++)
                if(vertices[offset + j] != vertex[j])
                    found = false;
            if(found)
                return (long)i;
        }
        return -1;
    }

    public static long findFloats(Buffer vertex, int strideInBytes, Buffer vertices, int numVertices, float epsilon){
        return findFloats(asFloatArray(asFloatBuffer(vertex)), strideInBytes, asFloatArray(asFloatBuffer(vertices)), numVertices, epsilon);
    }

    public static long findFloats(float[] vertex, int strideInBytes, Buffer vertices, int numVertices, float epsilon){
        return findFloats(vertex, strideInBytes, asFloatArray(asFloatBuffer(vertices)), numVertices, epsilon);
    }

    public static long findFloats(Buffer vertex, int strideInBytes, float[] vertices, int numVertices, float epsilon){
        return findFloats(asFloatArray(asFloatBuffer(vertex)), strideInBytes, vertices, numVertices, epsilon);
    }

    public static long findFloats(float[] vertex, int strideInBytes, float[] vertices, int numVertices, float epsilon){
        final int size = strideInBytes / 4;
        for(int i = 0; i < numVertices; i++){
            final int offset = i * size;
            boolean found = true;
            for(int j = 0; !found && j < size; j++)
                if((vertices[offset + j] > vertex[j] ? vertices[offset + j] - vertex[j] : vertex[j] - vertices[offset + j]) > epsilon)
                    found = false;
            if(found)
                return (long)i;
        }
        return -1;
    }

    public static FloatBuffer newFloatBuffer(int numFloats){
        if(GWT.isProdMode()){
            ByteBuffer buffer = ByteBuffer.allocateDirect(numFloats * 4);
            buffer.order(ByteOrder.nativeOrder());
            return buffer.asFloatBuffer();
        }else{
            return FloatBuffer.wrap(new float[numFloats]);
        }
    }

    public static DoubleBuffer newDoubleBuffer(int numDoubles){
        if(GWT.isProdMode()){
            ByteBuffer buffer = ByteBuffer.allocateDirect(numDoubles * 8);
            buffer.order(ByteOrder.nativeOrder());
            return buffer.asDoubleBuffer();
        }else{
            return DoubleBuffer.wrap(new double[numDoubles]);
        }
    }

    public static ByteBuffer newByteBuffer(int numBytes){
        if(GWT.isProdMode()){
            ByteBuffer buffer = ByteBuffer.allocateDirect(numBytes);
            buffer.order(ByteOrder.nativeOrder());
            return buffer;
        }else{
            return ByteBuffer.wrap(new byte[numBytes]);
        }
    }

    public static ShortBuffer newShortBuffer(int numShorts){
        if(GWT.isProdMode()){
            ByteBuffer buffer = ByteBuffer.allocateDirect(numShorts * 2);
            buffer.order(ByteOrder.nativeOrder());
            return buffer.asShortBuffer();
        }else{
            return ShortBuffer.wrap(new short[numShorts]);
        }
    }

    public static CharBuffer newCharBuffer(int numChars){
        if(GWT.isProdMode()){
            ByteBuffer buffer = ByteBuffer.allocateDirect(numChars * 2);
            buffer.order(ByteOrder.nativeOrder());
            return buffer.asCharBuffer();
        }else{
            return CharBuffer.wrap(new char[numChars]);
        }
    }

    public static IntBuffer newIntBuffer(int numInts){
        if(GWT.isProdMode()){
            ByteBuffer buffer = ByteBuffer.allocateDirect(numInts * 4);
            buffer.order(ByteOrder.nativeOrder());
            return buffer.asIntBuffer();
        }else{
            return IntBuffer.wrap(new int[numInts]);
        }
    }

    public static LongBuffer newLongBuffer(int numLongs){
        // FIXME ouch :p
        return LongBuffer.wrap(new long[numLongs]);
    }
}
