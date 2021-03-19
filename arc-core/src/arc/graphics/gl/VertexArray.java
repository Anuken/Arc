package arc.graphics.gl;

import arc.graphics.*;
import arc.util.*;

import java.nio.*;

/**
 * <p>
 * Convenience class for working with OpenGL vertex arrays. It interleaves all data in the order you specified in the constructor
 * via {@link VertexAttribute}.
 * </p>
 *
 * <p>
 * This class is not compatible with OpenGL 3+ core profiles. For this {@link VertexBufferObject}s are needed.
 * </p>
 * @author mzechner, Dave Clayton <contact@redskyforge.com>
 */
public class VertexArray implements VertexData{
    final Mesh mesh;
    final FloatBuffer buffer;
    final ByteBuffer byteBuffer;
    boolean isBound = false;

    /**
     * Constructs a new interleaved VertexArray
     * @param numVertices the maximum number of vertices
     */
    public VertexArray(int numVertices, Mesh mesh){
        this.mesh = mesh;
        byteBuffer = Buffers.newUnsafeByteBuffer(this.mesh.vertexSize * numVertices);
        buffer = byteBuffer.asFloatBuffer();
        buffer.flip();
        byteBuffer.flip();

        byteBuffer.asFloatBuffer();
    }

    @Override
    public void render(IndexData indices, int primitiveType, int offset, int count){
        if(indices.size() > 0){
            ShortBuffer buffer = indices.buffer();
            int oldPosition = buffer.position();
            int oldLimit = buffer.limit();
            buffer.position(offset);
            buffer.limit(offset + count);
            Gl.drawElements(primitiveType, count, GL20.GL_UNSIGNED_SHORT, buffer);
            buffer.position(oldPosition);
            buffer.limit(oldLimit);
        }else{
            Gl.drawArrays(primitiveType, offset, count);
        }
    }

    @Override
    public void dispose(){
        Buffers.disposeUnsafeByteBuffer(byteBuffer);
    }

    @Override
    public FloatBuffer buffer(){
        return buffer;
    }

    @Override
    public int size(){
        return buffer.limit() * 4 / mesh.vertexSize;
    }

    @Override
    public int max(){
        return byteBuffer.capacity() / mesh.vertexSize;
    }

    @Override
    public void set(float[] vertices, int offset, int count){
        Buffers.copy(vertices, byteBuffer, count, offset);
        buffer.position(0);
        buffer.limit(count);
    }

    @Override
    public void update(int targetOffset, float[] vertices, int sourceOffset, int count){
        final int pos = byteBuffer.position();
        byteBuffer.position(targetOffset * 4);
        Buffers.copy(vertices, sourceOffset, count, byteBuffer);
        byteBuffer.position(pos);
    }

    @Override
    public void bind(Shader shader){
        byteBuffer.limit(buffer.limit() * 4);

        int offset = 0;
        for(VertexAttribute attribute : mesh.attributes){
            int location = shader.getAttributeLocation(attribute.alias);
            int aoffset = offset;
            offset += attribute.size;
            if(location < 0) continue;
            shader.enableVertexAttribute(location);

            if(attribute.type == GL20.GL_FLOAT){
                buffer.position(aoffset / 4);
                shader.setVertexAttribute(location, attribute.components, attribute.type, attribute.normalized, mesh.vertexSize, buffer);
            }else{
                byteBuffer.position(aoffset);
                shader.setVertexAttribute(location, attribute.components, attribute.type, attribute.normalized, mesh.vertexSize, byteBuffer);
            }
        }

        isBound = true;
    }

    @Override
    public void unbind(Shader shader){
        for(VertexAttribute attribute : mesh.attributes){
            shader.disableVertexAttribute(attribute.alias);
        }

        isBound = false;
    }
}
