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
 * @author mzechner, Dave Clayton <contact@redskyforge.com>
 */
public class VertexBufferObject implements VertexData{
    boolean isDirty = false;
    boolean isBound = false;
    private VertexAttributes attributes;
    private FloatBuffer buffer;
    private ByteBuffer byteBuffer;
    private boolean ownsBuffer;
    private int bufferHandle;
    private int usage;

    /**
     * Constructs a new interleaved VertexBufferObject.
     * @param isStatic whether the vertex data is static.
     * @param numVertices the maximum number of vertices
     * @param attributes the {@link VertexAttribute}s.
     */
    public VertexBufferObject(boolean isStatic, int numVertices, VertexAttribute... attributes){
        this(isStatic, numVertices, new VertexAttributes(attributes));
    }

    /**
     * Constructs a new interleaved VertexBufferObject.
     * @param isStatic whether the vertex data is static.
     * @param numVertices the maximum number of vertices
     * @param attributes the {@link VertexAttributes}.
     */
    public VertexBufferObject(boolean isStatic, int numVertices, VertexAttributes attributes){
        bufferHandle = Gl.genBuffer();

        ByteBuffer data = Buffers.newUnsafeByteBuffer(attributes.vertexSize * numVertices);
        data.limit(0);
        setBuffer(data, true, attributes);
        setUsage(isStatic ? GL20.GL_STATIC_DRAW : GL20.GL_STREAM_DRAW);
    }

    protected VertexBufferObject(int usage, ByteBuffer data, boolean ownsBuffer, VertexAttributes attributes){
        bufferHandle = Gl.genBuffer();

        setBuffer(data, ownsBuffer, attributes);
        setUsage(usage);
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
    public FloatBuffer buffer(){
        isDirty = true;
        return buffer;
    }

    /**
     * Low level method to reset the buffer and attributes to the specified values. Use with care!
     */
    protected void setBuffer(Buffer data, boolean ownsBuffer, VertexAttributes value){
        if(isBound) throw new ArcRuntimeException("Cannot change attributes while VBO is bound");
        if(this.ownsBuffer && byteBuffer != null)
            Buffers.disposeUnsafeByteBuffer(byteBuffer);
        attributes = value;
        if(data instanceof ByteBuffer)
            byteBuffer = (ByteBuffer)data;
        else
            throw new ArcRuntimeException("Only ByteBuffer is currently supported");
        this.ownsBuffer = ownsBuffer;

        final int l = byteBuffer.limit();
        byteBuffer.limit(byteBuffer.capacity());
        buffer = byteBuffer.asFloatBuffer();
        byteBuffer.limit(l);
        buffer.limit(l / 4);
    }

    private void bufferChanged(){
        if(isBound){
            Gl.bufferData(GL20.GL_ARRAY_BUFFER, byteBuffer.limit(), byteBuffer, usage);
            isDirty = false;
        }
    }

    @Override
    public void set(float[] vertices, int offset, int count){
        isDirty = true;
        Buffers.copy(vertices, byteBuffer, count, offset);
        buffer.position(0);
        buffer.limit(count);
        bufferChanged();
    }

    @Override
    public void update(int targetOffset, float[] vertices, int sourceOffset, int count){
        isDirty = true;
        final int pos = byteBuffer.position();
        byteBuffer.position(targetOffset * 4);
        Buffers.copy(vertices, sourceOffset, count, byteBuffer);
        byteBuffer.position(pos);
        buffer.position(0);
        bufferChanged();
    }

    /**
     * @return The GL enum used in the call to {@link GL20#glBufferData(int, int, java.nio.Buffer, int)}, e.g. GL_STATIC_DRAW or
     * GL_DYNAMIC_DRAW
     */
    protected int getUsage(){
        return usage;
    }

    /**
     * Set the GL enum used in the call to {@link GL20#glBufferData(int, int, java.nio.Buffer, int)}, can only be called when the
     * VBO is not bound.
     */
    protected void setUsage(int value){
        if(isBound) throw new ArcRuntimeException("Cannot change usage while VBO is bound");
        usage = value;
    }

    /**
     * Binds this VertexBufferObject for rendering via glDrawArrays or glDrawElements
     * @param shader the shader
     */
    @Override
    public void bind(Shader shader){
        Gl.bindBuffer(GL20.GL_ARRAY_BUFFER, bufferHandle);
        if(isDirty){
            byteBuffer.limit(buffer.limit() * 4);
            Gl.bufferData(GL20.GL_ARRAY_BUFFER, byteBuffer.limit(), byteBuffer, usage);
            isDirty = false;
        }

        for(VertexAttribute attribute : attributes.attributes){
            int location = shader.getAttributeLocation(attribute.alias);
            if(location < 0) continue;

            shader.enableVertexAttribute(location);
            shader.setVertexAttribute(location, attribute.components, attribute.type, attribute.normalized, attributes.vertexSize, attribute.offset);
        }

        isBound = true;
    }


    @Override
    public void unbind(Shader shader){
        final int numAttributes = attributes.size();
        for(int i = 0; i < numAttributes; i++){
            shader.disableVertexAttribute(attributes.get(i).alias);
        }
        Gl.bindBuffer(GL20.GL_ARRAY_BUFFER, 0);
        isBound = false;
    }

    /** Disposes of all resources this VertexBufferObject uses. */
    @Override
    public void dispose(){
        Gl.bindBuffer(GL20.GL_ARRAY_BUFFER, 0);
        Gl.deleteBuffer(bufferHandle);
        bufferHandle = 0;
        if(ownsBuffer) Buffers.disposeUnsafeByteBuffer(byteBuffer);
    }
}
