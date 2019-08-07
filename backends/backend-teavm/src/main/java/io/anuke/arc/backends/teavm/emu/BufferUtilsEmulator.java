/*
 *  Copyright 2015 Alexey Andreev.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.anuke.arc.backends.teavm.emu;

import com.badlogic.gdx.utils.BufferUtils;
import io.anuke.arc.backends.teavm.plugin.Annotations.*;
import io.anuke.arc.util.*;

import java.nio.*;

/**
 *
 * @author Alexey Andreev
 */
@Emulate(BufferUtils.class)
@SuppressWarnings("unused")
public class BufferUtilsEmulator {

    private static void freeMemory(ByteBuffer buffer) {
    }

    private static ByteBuffer newDisposableByteBuffer(int numBytes) {
        return ByteBuffer.wrap(new byte[numBytes]);
    }

    private static void copyJni(float[] src, Buffer dst, int numFloats, int offset) {
        dst.position(0);
        dst.limit(dst.capacity());
        FloatBuffer floatDst;
        if (dst instanceof FloatBuffer) {
            floatDst = (FloatBuffer)dst;
            floatDst = floatDst.duplicate();
        } else if (dst instanceof ByteBuffer) {
            ByteBuffer byteDst = (ByteBuffer)dst;
            floatDst = byteDst.asFloatBuffer();
        } else {
            throw new ArcRuntimeException("Target buffer of type " + dst.getClass().getName() + " is not supported");
        }
        floatDst.put(src, offset, numFloats);
    }

    private static void copyJni(float[] src, int srcOffset, Buffer dst, int numFloats, int offset) {
        dst.position(0);
        dst.limit(dst.capacity());
        FloatBuffer floatDst;
        if (dst instanceof FloatBuffer) {
            floatDst = (FloatBuffer)dst;
            floatDst = floatDst.duplicate();
        } else if (dst instanceof ByteBuffer) {
            ByteBuffer byteDst = (ByteBuffer)dst;
            floatDst = byteDst.asFloatBuffer();
        } else {
            throw new ArcRuntimeException("Target buffer of type " + dst.getClass().getName() + " is not supported");
        }

        floatDst.put(src, offset, numFloats);
    }
}
