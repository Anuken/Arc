/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

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

    public native static void copyJni(char[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes); /*
		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	*/

    public native static void copyJni(short[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes); /*
		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	 */

    public native static void copyJni(int[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes); /*
		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	*/

    public native static void copyJni(long[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes); /*
		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	*/

    public native static void copyJni(float[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes); /*
		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	*/

    public native static void copyJni(double[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes); /*
		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	*/

    public native static void copyJni(Buffer src, int srcOffset, Buffer dst, int dstOffset, int numBytes); /*
		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	*/
	
	/*JNI
	template<size_t n1, size_t n2> void transform(float * const &src, float * const &m, float * const &dst) {}
	
	template<> inline void transform<4, 4>(float * const &src, float * const &m, float * const &dst) {
		const float x = src[0], y = src[1], z = src[2], w = src[3];
		dst[0] = x * m[ 0] + y * m[ 4] + z * m[ 8] + w * m[12]; 
		dst[1] = x * m[ 1] + y * m[ 5] + z * m[ 9] + w * m[13];
		dst[2] = x * m[ 2] + y * m[ 6] + z * m[10] + w * m[14];
		dst[3] = x * m[ 3] + y * m[ 7] + z * m[11] + w * m[15]; 
	}
	
	template<> inline void transform<3, 4>(float * const &src, float * const &m, float * const &dst) {
		const float x = src[0], y = src[1], z = src[2];
		dst[0] = x * m[ 0] + y * m[ 4] + z * m[ 8] + m[12]; 
		dst[1] = x * m[ 1] + y * m[ 5] + z * m[ 9] + m[13];
		dst[2] = x * m[ 2] + y * m[ 6] + z * m[10] + m[14]; 
	}
	
	template<> inline void transform<2, 4>(float * const &src, float * const &m, float * const &dst) {
		const float x = src[0], y = src[1];
		dst[0] = x * m[ 0] + y * m[ 4] + m[12]; 
		dst[1] = x * m[ 1] + y * m[ 5] + m[13]; 
	}
	
	template<> inline void transform<3, 3>(float * const &src, float * const &m, float * const &dst) {
		const float x = src[0], y = src[1], z = src[2];
		dst[0] = x * m[0] + y * m[3] + z * m[6]; 
		dst[1] = x * m[1] + y * m[4] + z * m[7];
		dst[2] = x * m[2] + y * m[5] + z * m[8]; 
	}
	
	template<> inline void transform<2, 3>(float * const &src, float * const &m, float * const &dst) {
		const float x = src[0], y = src[1];
		dst[0] = x * m[0] + y * m[3] + m[6]; 
		dst[1] = x * m[1] + y * m[4] + m[7]; 
	}
	
	template<size_t n1, size_t n2> void transform(float * const &v, int const &stride, int const &count, float * const &m, int offset) {
		for (int i = 0; i < count; i++) {
			transform<n1, n2>(&v[offset], m, &v[offset]);
			offset += stride;
		}
	}
	
	template<size_t n1, size_t n2> void transform(float * const &v, int const &stride, unsigned short * const &indices, int const &count, float * const &m, int offset) {
		for (int i = 0; i < count; i++) {
			transform<n1, n2>(&v[offset], m, &v[offset]);
			offset += stride;
		}
	}
	
	inline bool compare(float * const &lhs, float * const & rhs, const unsigned int &size, const float &epsilon) {
   	for (unsigned int i = 0; i < size; i++)
   		if ((*(unsigned int*)&lhs[i] != *(unsigned int*)&rhs[i]) && ((lhs[i] > rhs[i] ? lhs[i] - rhs[i] : rhs[i] - lhs[i]) > epsilon))
         	return false;
		return true;
	}
	
	long find(float * const &vertex, const unsigned int &size, float * const &vertices, const unsigned int &count, const float &epsilon) {
		for (unsigned int i = 0; i < count; i++)
			if (compare(&vertices[i*size], vertex, size, epsilon))
				return (long)i;
		return -1;
	}

	inline bool compare(float * const &lhs, float * const & rhs, const unsigned int &size) {
   	for (unsigned int i = 0; i < size; i++)
      	if ((*(unsigned int*)&lhs[i] != *(unsigned int*)&rhs[i]) && lhs[i] != rhs[i])
         	return false;
		return true;
	}
	
	long find(float * const &vertex, const unsigned int &size, float * const &vertices, const unsigned int &count) {
		for (unsigned int i = 0; i < count; i++)
			if (compare(&vertices[i*size], vertex, size))
				return (long)i;
		return -1;
	}

	inline unsigned int calcHash(float * const &vertex, const unsigned int &size) {
		unsigned int result = 0;
		for (unsigned int i = 0; i < size; ++i)
			result += ((*((unsigned int *)&vertex[i])) & 0xffffff80) >> (i & 0x7);
		return result & 0x7fffffff;
	}
	
	long find(float * const &vertex, const unsigned int &size, float * const &vertices, unsigned int * const &hashes, const unsigned int &count) {
		const unsigned int hash = calcHash(vertex, size);
		for (unsigned int i = 0; i < count; i++)
			if (hashes[i] == hash && compare(&vertices[i*size], vertex, size))
				return (long)i;
		return -1;
	}
	*/

    public native static void transformV3M3Jni(Buffer data, int strideInBytes, int count, float[] matrix, int offsetInBytes); /*
		transform<3, 3>((float*)data, strideInBytes / 4, count, (float*)matrix, offsetInBytes / 4);
	*/

    public native static void transformV3M3Jni(float[] data, int strideInBytes, int count, float[] matrix, int offsetInBytes); /*
		transform<3, 3>((float*)data, strideInBytes / 4, count, (float*)matrix, offsetInBytes / 4);
	*/

    public native static void transformV2M3Jni(Buffer data, int strideInBytes, int count, float[] matrix, int offsetInBytes); /*
		transform<2, 3>((float*)data, strideInBytes / 4, count, (float*)matrix, offsetInBytes / 4);
	*/

    public native static void transformV2M3Jni(float[] data, int strideInBytes, int count, float[] matrix, int offsetInBytes); /*
		transform<2, 3>((float*)data, strideInBytes / 4, count, (float*)matrix, offsetInBytes / 4);
	*/

    public native static long find(Buffer vertex, int vertexOffsetInBytes, int strideInBytes, Buffer vertices, int verticesOffsetInBytes, int numVertices); /*
		return find((float *)&vertex[vertexOffsetInBytes / 4], (unsigned int)(strideInBytes / 4), (float*)&vertices[verticesOffsetInBytes / 4], (unsigned int)numVertices);
	*/

    public native static long find(float[] vertex, int vertexOffsetInBytes, int strideInBytes, Buffer vertices, int verticesOffsetInBytes, int numVertices); /*
		return find((float *)&vertex[vertexOffsetInBytes / 4], (unsigned int)(strideInBytes / 4), (float*)&vertices[verticesOffsetInBytes / 4], (unsigned int)numVertices);
	*/

    public native static long find(Buffer vertex, int vertexOffsetInBytes, int strideInBytes, float[] vertices, int verticesOffsetInBytes, int numVertices); /*
		return find((float *)&vertex[vertexOffsetInBytes / 4], (unsigned int)(strideInBytes / 4), (float*)&vertices[verticesOffsetInBytes / 4], (unsigned int)numVertices);
	*/

    public native static long find(float[] vertex, int vertexOffsetInBytes, int strideInBytes, float[] vertices, int verticesOffsetInBytes, int numVertices); /*
		return find((float *)&vertex[vertexOffsetInBytes / 4], (unsigned int)(strideInBytes / 4), (float*)&vertices[verticesOffsetInBytes / 4], (unsigned int)numVertices);
	*/

    public native static long find(Buffer vertex, int vertexOffsetInBytes, int strideInBytes, Buffer vertices, int verticesOffsetInBytes, int numVertices, float epsilon); /*
		return find((float *)&vertex[vertexOffsetInBytes / 4], (unsigned int)(strideInBytes / 4), (float*)&vertices[verticesOffsetInBytes / 4], (unsigned int)numVertices, epsilon);
	*/

    public native static long find(float[] vertex, int vertexOffsetInBytes, int strideInBytes, Buffer vertices, int verticesOffsetInBytes, int numVertices, float epsilon); /*
		return find((float *)&vertex[vertexOffsetInBytes / 4], (unsigned int)(strideInBytes / 4), (float*)&vertices[verticesOffsetInBytes / 4], (unsigned int)numVertices, epsilon);
	*/

    public native static long find(Buffer vertex, int vertexOffsetInBytes, int strideInBytes, float[] vertices, int verticesOffsetInBytes, int numVertices, float epsilon); /*
		return find((float *)&vertex[vertexOffsetInBytes / 4], (unsigned int)(strideInBytes / 4), (float*)&vertices[verticesOffsetInBytes / 4], (unsigned int)numVertices, epsilon);
	*/

    public native static long find(float[] vertex, int vertexOffsetInBytes, int strideInBytes, float[] vertices, int verticesOffsetInBytes, int numVertices, float epsilon); /*
		return find((float *)&vertex[vertexOffsetInBytes / 4], (unsigned int)(strideInBytes / 4), (float*)&vertices[verticesOffsetInBytes / 4], (unsigned int)numVertices, epsilon);
	*/
}
