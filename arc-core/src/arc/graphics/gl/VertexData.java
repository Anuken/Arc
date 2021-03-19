package arc.graphics.gl;

import arc.graphics.*;
import arc.util.*;

import java.nio.*;

/**
 * A VertexData instance holds vertices for rendering with OpenGL. It is implemented as either a {@link VertexArray} or a
 * {@link VertexBufferObject}. Only the later supports OpenGL ES 2.0.
 * @author mzechner
 */
public interface VertexData extends Disposable{

    default void render(IndexData indices, int primitiveType, int offset, int count){

        if(indices.size() > 0){
            if(count + offset > indices.max()){
                throw new ArcRuntimeException("Mesh attempting to access memory outside of the index buffer (count: "
                + count + ", offset: " + offset + ", max: " + indices.max() + ")");
            }

            Gl.drawElements(primitiveType, count, GL20.GL_UNSIGNED_SHORT, offset * 2);
        }else{
            Gl.drawArrays(primitiveType, offset, count);
        }
    }

    /** @return the number of vertices this VertexData stores */
    int size();

    /** @return the number of vertices this VertexData can store */
    int max();

    /**
     * Sets the vertices of this VertexData, discarding the old vertex data. The count must equal the number of floats per vertex
     * times the number of vertices to be copied to this VertexData.
     * <p>
     * This can be called in between calls to bind and unbind. The vertex data will be updated instantly.
     * @param vertices the vertex data
     * @param offset the offset to start copying the data from
     * @param count the number of floats to copy
     */
    void set(float[] vertices, int offset, int count);

    /**
     * Update (a portion of) the vertices. Does not resize the backing buffer.
     * @param vertices the vertex data
     * @param sourceOffset the offset to start copying the data from
     * @param count the number of floats to copy
     */
    void update(int targetOffset, float[] vertices, int sourceOffset, int count);

    /**
     * Returns the underlying FloatBuffer and marks it as dirty, causing the buffer contents to be uploaded on the next call to
     * bind. If you need immediate uploading use {@link #set(float[], int, int)}; Any modifications made to the Buffer
     * *after* the call to bind will not automatically be uploaded.
     * @return the underlying FloatBuffer holding the vertex data.
     */
    FloatBuffer buffer();

    /** Binds this VertexData for rendering via glDrawArrays or glDrawElements. */
    void bind(Shader shader);

    /** Unbinds this VertexData. */
    void unbind(Shader shader);
}
