package arc.graphics.gl;

import arc.graphics.*;
import arc.util.*;

import java.nio.*;

/**
 * <p>
 * A {@link VertexData} implementation based on OpenGL vertex buffer objects.
 * <p>
 * If the OpenGL ES context was lost you can call {@link #invalidate()} to recreate a new OpenGL vertex buffer object.
 * <p>
 * The data is bound via glVertexAttribPointer() according to the attribute aliases specified via {@link VertexAttributes}
 * in the constructor.
 * <p>
 * VertexBufferObjects must be disposed via the {@link #dispose()} method when no longer needed
 * @author mzechner
 */
public class VertexBufferObjectSubData implements VertexData{
    final VertexAttributes attributes;
    final FloatBuffer buffer;
    final ByteBuffer byteBuffer;
    final boolean isDirect;
    final boolean isStatic;
    final int usage;
    int bufferHandle;
    boolean isDirty = false;
    boolean isBound = false;

    /**
     * Constructs a new interleaved VertexBufferObject.
     * @param isStatic whether the vertex data is static.
     * @param numVertices the maximum number of vertices
     * @param attributes the {@link VertexAttributes}.
     */
    public VertexBufferObjectSubData(boolean isStatic, int numVertices, VertexAttribute... attributes){
        this(isStatic, numVertices, new VertexAttributes(attributes));
    }

    /**
     * Constructs a new interleaved VertexBufferObject.
     * @param isStatic whether the vertex data is static.
     * @param numVertices the maximum number of vertices
     * @param attributes the {@link VertexAttribute}s.
     */
    public VertexBufferObjectSubData(boolean isStatic, int numVertices, VertexAttributes attributes){
        this.isStatic = isStatic;
        this.attributes = attributes;
        byteBuffer = Buffers.newByteBuffer(this.attributes.vertexSize * numVertices);
        isDirect = true;

        usage = isStatic ? GL20.GL_STATIC_DRAW : GL20.GL_DYNAMIC_DRAW;
        buffer = byteBuffer.asFloatBuffer();
        bufferHandle = createBufferObject();
        buffer.flip();
        byteBuffer.flip();
    }

    private int createBufferObject(){
        int result = Gl.genBuffer();
        Gl.bindBuffer(GL20.GL_ARRAY_BUFFER, result);
        Gl.bufferData(GL20.GL_ARRAY_BUFFER, byteBuffer.capacity(), null, usage);
        Gl.bindBuffer(GL20.GL_ARRAY_BUFFER, 0);
        return result;
    }

    @Override
    public VertexAttributes getAttributes(){
        return attributes;
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
    public FloatBuffer getBuffer(){
        isDirty = true;
        return buffer;
    }

    private void bufferChanged(){
        if(isBound){
            Gl.bufferSubData(GL20.GL_ARRAY_BUFFER, 0, byteBuffer.limit(), byteBuffer);
            isDirty = false;
        }
    }

    @Override
    public void setVertices(float[] vertices, int offset, int count){
        isDirty = true;
        if(isDirect){
            Buffers.copy(vertices, byteBuffer, count, offset);
            buffer.position(0);
            buffer.limit(count);
        }else{
            buffer.clear();
            buffer.put(vertices, offset, count);
            buffer.flip();
            byteBuffer.position(0);
            byteBuffer.limit(buffer.limit() << 2);
        }

        bufferChanged();
    }

    @Override
    public void updateVertices(int targetOffset, float[] vertices, int sourceOffset, int count){
        isDirty = true;
        if(isDirect){
            final int pos = byteBuffer.position();
            byteBuffer.position(targetOffset * 4);
            Buffers.copy(vertices, sourceOffset, count, byteBuffer);
            byteBuffer.position(pos);
        }else
            throw new ArcRuntimeException("Buffer must be allocated direct."); // Should never happen

        bufferChanged();
    }

    /**
     * Binds this VertexBufferObject for rendering via glDrawArrays or glDrawElements
     * @param shader the shader
     */
    @Override
    public void bind(final Shader shader){
        bind(shader, null);
    }

    @Override
    public void bind(final Shader shader, final int[] locations){
        Gl.bindBuffer(GL20.GL_ARRAY_BUFFER, bufferHandle);
        if(isDirty){
            byteBuffer.limit(buffer.limit() * 4);
            Gl.bufferData(GL20.GL_ARRAY_BUFFER, byteBuffer.limit(), byteBuffer, usage);
            isDirty = false;
        }

        final int numAttributes = attributes.size();
        if(locations == null){
            for(int i = 0; i < numAttributes; i++){
                final VertexAttribute attribute = attributes.get(i);
                final int location = shader.getAttributeLocation(attribute.alias);
                if(location < 0) continue;
                shader.enableVertexAttribute(location);

                shader.setVertexAttribute(location, attribute.numComponents, attribute.type, attribute.normalized, attributes.vertexSize,
                attribute.offset);
            }
        }else{
            for(int i = 0; i < numAttributes; i++){
                final VertexAttribute attribute = attributes.get(i);
                final int location = locations[i];
                if(location < 0) continue;
                shader.enableVertexAttribute(location);

                shader.setVertexAttribute(location, attribute.numComponents, attribute.type, attribute.normalized, attributes.vertexSize,
                attribute.offset);
            }
        }
        isBound = true;
    }

    /**
     * Unbinds this VertexBufferObject.
     * @param shader the shader
     */
    @Override
    public void unbind(final Shader shader){
        unbind(shader, null);
    }

    @Override
    public void unbind(final Shader shader, final int[] locations){
        final int numAttributes = attributes.size();
        if(locations == null){
            for(int i = 0; i < numAttributes; i++){
                shader.disableVertexAttribute(attributes.get(i).alias);
            }
        }else{
            for(int i = 0; i < numAttributes; i++){
                final int location = locations[i];
                if(location >= 0) shader.disableVertexAttribute(location);
            }
        }
        Gl.bindBuffer(GL20.GL_ARRAY_BUFFER, 0);
        isBound = false;
    }

    /** Invalidates the VertexBufferObject so a new OpenGL buffer handle is created. Use this in case of a context loss. */
    public void invalidate(){
        bufferHandle = createBufferObject();
        isDirty = true;
    }

    /** Disposes of all resources this VertexBufferObject uses. */
    @Override
    public void dispose(){
        Gl.bindBuffer(GL20.GL_ARRAY_BUFFER, 0);
        Gl.deleteBuffer(bufferHandle);
        bufferHandle = 0;
    }

    /**
     * Returns the VBO handle
     * @return the VBO handle
     */
    public int getBufferHandle(){
        return bufferHandle;
    }
}
