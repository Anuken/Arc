package io.anuke.arc.graphics.glutils;

import io.anuke.arc.Core;
import io.anuke.arc.graphics.GL20;
import io.anuke.arc.util.BufferUtils;
import io.anuke.arc.util.ArcRuntimeException;

import java.nio.ShortBuffer;

/**
 * <p>
 * In IndexBufferObject wraps OpenGL's index buffer functionality to be used in conjunction with VBOs. This class can be
 * seamlessly used with OpenGL ES 1.x and 2.0.
 * </p>
 *
 * <p>
 * Uses indirect Buffers on Android 1.5/1.6 to fix GC invocation due to leaking PlatformAddress instances.
 * </p>
 *
 * <p>
 * You can also use this to store indices for vertex arrays. Do not call {@link #bind()} or {@link #unbind()} in this case but
 * rather use {@link #getBuffer()} to use the buffer directly with glDrawElements. You must also create the IndexBufferObject with
 * the second constructor and specify isDirect as true as glDrawElements in conjunction with vertex arrays needs direct buffers.
 * </p>
 *
 * <p>
 * VertexBufferObjects must be disposed via the {@link #dispose()} method when no longer needed
 * </p>
 * @author mzechner
 */
public class IndexBufferObject implements IndexData{
    final boolean isDirect;
    final int usage;
    ShortBuffer buffer;
    int bufferHandle;
    boolean isDirty = true;
    boolean isBound = false;

    /**
     * Creates a new IndexBufferObject.
     * @param isStatic whether the index buffer is static
     * @param maxIndices the maximum number of indices this buffer can hold
     */
    public IndexBufferObject(boolean isStatic, int maxIndices){
        isDirect = true;
        buffer = BufferUtils.newShortBuffer(maxIndices);
        buffer.flip();
        bufferHandle = Core.gl20.glGenBuffer();
        usage = isStatic ? GL20.GL_STATIC_DRAW : GL20.GL_DYNAMIC_DRAW;
    }

    /**
     * Creates a new IndexBufferObject to be used with vertex arrays.
     * @param maxIndices the maximum number of indices this buffer can hold
     */
    public IndexBufferObject(int maxIndices){
        this.isDirect = true;
        buffer = BufferUtils.newShortBuffer(maxIndices);
        buffer.flip();
        bufferHandle = Core.gl20.glGenBuffer();
        usage = GL20.GL_STATIC_DRAW;
    }

    /** @return the number of indices currently stored in this buffer */
    public int getNumIndices(){
        return buffer.limit();
    }

    /** @return the maximum number of indices this IndexBufferObject can store. */
    public int getNumMaxIndices(){
        return buffer.capacity();
    }

    /**
     * <p>
     * Sets the indices of this IndexBufferObject, discarding the old indices. The count must equal the number of indices to be
     * copied to this IndexBufferObject.
     * </p>
     *
     * <p>
     * This can be called in between calls to {@link #bind()} and {@link #unbind()}. The index data will be updated instantly.
     * </p>
     * @param indices the vertex data
     * @param offset the offset to start copying the data from
     * @param count the number of shorts to copy
     */
    public void setIndices(short[] indices, int offset, int count){
        isDirty = true;
        buffer.clear();
        buffer.put(indices, offset, count);
        buffer.flip();

        if(isBound){
            Core.gl20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, buffer.limit(), buffer, usage);
            isDirty = false;
        }
    }

    public void setIndices(ShortBuffer indices){
        isDirty = true;
        buffer.clear();
        buffer.put(indices);
        buffer.flip();

        if(isBound){
            Core.gl20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, buffer.limit(), buffer, usage);
            isDirty = false;
        }
    }

    @Override
    public void updateIndices(int targetOffset, short[] indices, int offset, int count){
        isDirty = true;
        final int pos = buffer.position();
        buffer.position(targetOffset);
        BufferUtils.copy(indices, offset, buffer, count);
        buffer.position(pos);

        if(isBound){
            Core.gl20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, buffer.limit(), buffer, usage);
            isDirty = false;
        }
    }

    /**
     * <p>
     * Returns the underlying ShortBuffer. If you modify the buffer contents they wil be uploaded on the call to {@link #bind()}.
     * If you need immediate uploading use {@link #setIndices(short[], int, int)}.
     * </p>
     * @return the underlying short buffer.
     */
    public ShortBuffer getBuffer(){
        isDirty = true;
        return buffer;
    }

    /** Binds this IndexBufferObject for rendering with glDrawElements. */
    public void bind(){
        if(bufferHandle == 0) throw new ArcRuntimeException("No buffer allocated!");

        Core.gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, bufferHandle);
        if(isDirty){
            Core.gl20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, buffer.limit(), buffer, usage);
            isDirty = false;
        }
        isBound = true;
    }

    /** Unbinds this IndexBufferObject. */
    public void unbind(){
        Core.gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);
        isBound = false;
    }

    /** Invalidates the IndexBufferObject so a new OpenGL buffer handle is created. Use this in case of a context loss. */
    public void invalidate(){
        bufferHandle = Core.gl20.glGenBuffer();
        isDirty = true;
    }

    /** Disposes this IndexBufferObject and all its associated OpenGL resources. */
    public void dispose(){
        GL20 gl = Core.gl20;
        gl.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);
        gl.glDeleteBuffer(bufferHandle);
        bufferHandle = 0;
    }
}
