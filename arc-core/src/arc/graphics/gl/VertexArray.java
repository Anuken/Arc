package arc.graphics.gl;

import arc.graphics.GL20;
import arc.graphics.VertexAttribute;
import arc.util.Buffers;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

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
    final VertexAttributes attributes;
    final FloatBuffer buffer;
    final ByteBuffer byteBuffer;
    boolean isBound = false;

    /**
     * Constructs a new interleaved VertexArray
     * @param numVertices the maximum number of vertices
     * @param attributes the {@link VertexAttribute}s
     */
    public VertexArray(int numVertices, VertexAttribute... attributes){
        this(numVertices, new VertexAttributes(attributes));
    }

    /**
     * Constructs a new interleaved VertexArray
     * @param numVertices the maximum number of vertices
     * @param attributes the {@link VertexAttributes}
     */
    public VertexArray(int numVertices, VertexAttributes attributes){
        this.attributes = attributes;
        byteBuffer = Buffers.newUnsafeByteBuffer(this.attributes.vertexSize * numVertices);
        buffer = byteBuffer.asFloatBuffer();
        buffer.flip();
        byteBuffer.flip();

        byteBuffer.asFloatBuffer();
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
    public int getNumVertices(){
        return buffer.limit() * 4 / attributes.vertexSize;
    }

    @Override
    public int getNumMaxVertices(){
        return byteBuffer.capacity() / attributes.vertexSize;
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
        final int numAttributes = attributes.size();
        byteBuffer.limit(buffer.limit() * 4);
        for(int i = 0; i < numAttributes; i++){
            final VertexAttribute attribute = attributes.get(i);
            final int location = shader.getAttributeLocation(attribute.alias);
            if(location < 0) continue;
            shader.enableVertexAttribute(location);

            if(attribute.type == GL20.GL_FLOAT){
                buffer.position(attribute.offset / 4);
                shader.setVertexAttribute(location, attribute.components, attribute.type, attribute.normalized,
                attributes.vertexSize, buffer);
            }else{
                byteBuffer.position(attribute.offset);
                shader.setVertexAttribute(location, attribute.components, attribute.type, attribute.normalized,
                attributes.vertexSize, byteBuffer);
            }
        }

        isBound = true;
    }

    @Override
    public void unbind(Shader shader){
        final int numAttributes = attributes.size();
        for(int i = 0; i < numAttributes; i++){
            shader.disableVertexAttribute(attributes.get(i).alias);
        }

        isBound = false;
    }

    @Override
    public VertexAttributes getAttributes(){
        return attributes;
    }
}
