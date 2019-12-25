package arc.graphics.gl;

import arc.util.Disposable;

import java.nio.ShortBuffer;

/**
 * An IndexData instance holds index data. Can be either a plain short buffer or an OpenGL buffer object.
 * @author mzechner
 */
public interface IndexData extends Disposable{
    /** @return the number of indices currently stored in this buffer */
    int getNumIndices();

    /** @return the maximum number of indices this IndexBufferObject can store. */
    int getNumMaxIndices();

    /**
     * <p>
     * Sets the indices of this IndexBufferObject, discarding the old indices. The count must equal the number of indices to be
     * copied to this IndexBufferObject.
     * </p>
     *
     * <p>
     * This can be called in between calls to {@link #bind()} and {@link #unbind()}. The index data will be updated instantly.
     * </p>
     * @param indices the index data
     * @param offset the offset to start copying the data from
     * @param count the number of shorts to copy
     */
    void setIndices(short[] indices, int offset, int count);

    /**
     * Copies the specified indices to the indices of this IndexBufferObject, discarding the old indices. Copying start at the
     * current {@link ShortBuffer#position()} of the specified buffer and copied the {@link ShortBuffer#remaining()} amount of
     * indices. This can be called in between calls to {@link #bind()} and {@link #unbind()}. The index data will be updated
     * instantly.
     * @param indices the index data to copy
     */
    void setIndices(ShortBuffer indices);

    /**
     * Update (a portion of) the indices.
     * @param targetOffset offset in indices buffer
     * @param indices the index data
     * @param offset the offset to start copying the data from
     * @param count the number of shorts to copy
     */
    void updateIndices(int targetOffset, short[] indices, int offset, int count);

    /**
     * <p>
     * Returns the underlying ShortBuffer. If you modify the buffer contents they wil be uploaded on the call to {@link #bind()}.
     * If you need immediate uploading use {@link #setIndices(short[], int, int)}.
     * </p>
     * @return the underlying short buffer.
     */
    ShortBuffer getBuffer();

    /** Binds this IndexBufferObject for rendering with glDrawElements. */
    void bind();

    /** Unbinds this IndexBufferObject. */
    void unbind();

    /** Invalidates the IndexBufferObject so a new OpenGL buffer handle is created. Use this in case of a context loss. */
    void invalidate();

    /** Disposes this IndexDatat and all its associated OpenGL resources. */
    void dispose();
}
