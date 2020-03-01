package com.badlogic.gdx.utils;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Class with static helper methods to increase the speed of array/direct buffer and direct buffer/direct buffer transfers
 * @author mzechner, xoppa
 */
public final class BufferUtils{

    // @off
	/*JNI 
	#include <stdio.h>
	#include <stdlib.h>
	#include <string.h>
	*/

    /**
     * Frees the memory allocated for the ByteBuffer, which MUST have been allocated via {@link #newUnsafeByteBuffer(ByteBuffer)}
     * or in native code.
     */
    public static native void freeMemory(ByteBuffer buffer); /*
		free(buffer);
	 */

    public static native ByteBuffer newDisposableByteBuffer(int numBytes); /*
		return env->NewDirectByteBuffer((char*)malloc(numBytes), numBytes);
	*/

    public static native long getBufferAddress(Buffer buffer); /*
	    return (jlong) buffer;
	*/

    /** Writes the specified number of zeros to the buffer. This is generally faster than reallocating a new buffer. */
    public static native void clear(ByteBuffer buffer, int numBytes); /*
		memset(buffer, 0, numBytes);
	*/

    public native static void copyJni(float[] src, Buffer dst, int numFloats, int offset); /*
		memcpy(dst, src + offset, numFloats << 2 );
	*/

    public native static void copyJni(byte[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes); /*
		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	*/

    public native static void copyJni(short[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes); /*
		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	 */

    public native static void copyJni(int[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes); /*
		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	*/

    public native static void copyJni(float[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes); /*
		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	*/

    public native static void copyJni(Buffer src, int srcOffset, Buffer dst, int dstOffset, int numBytes); /*
		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	*/
}
