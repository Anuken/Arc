package arc.graphics.gl;

import arc.graphics.*;
import arc.util.*;

import java.nio.*;

/**
 * <p>
 * A {@link VertexData} implementation based on OpenGL vertex buffer objects.
 * <p>
 * <p>
 * The data is bound via glVertexAttribPointer() according to the attribute aliases specified in the constructor.
 * <p>
 * VertexBufferObjects must be disposed via the {@link #dispose()} method when no longer needed
 * @author mzechner, Dave Clayton <contact@redskyforge.com>
 */
public class VertexBufferObject implements VertexData{
    boolean dirty = false;
    boolean bound = false;
    private Mesh mesh;
    private FloatBuffer buffer;
    private ByteBuffer byteBuffer;
    private boolean ownsBuffer;
    private int bufferHandle;
    private int usage;

    /**
     * Constructs a new interleaved VertexBufferObject.
     * @param isStatic whether the vertex data is static.
     * @param numVertices the maximum number of vertices
     */
    public VertexBufferObject(boolean isStatic, int numVertices, Mesh mesh){
        this.mesh = mesh;
        //to create with subdata support: Gl.bufferData(GL20.GL_ARRAY_BUFFER, byteBuffer.capacity(), null, usage);
        bufferHandle = Gl.genBuffer();
        usage = isStatic ? Gl.staticDraw : Gl.streamDraw;

        ByteBuffer data = Buffers.newUnsafeByteBuffer(mesh.vertexSize * numVertices);
        data.limit(0);
        setBuffer(data, true);
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
    public FloatBuffer buffer(){
        dirty = true;
        return buffer;
    }

    /**
     * Low level method to reset the buffer and attributes to the specified values. Use with care!
     */
    protected void setBuffer(Buffer data, boolean ownsBuffer){
        if(bound) throw new ArcRuntimeException("Cannot change attributes while VBO is bound");
        if(this.ownsBuffer && byteBuffer != null)
            Buffers.disposeUnsafeByteBuffer(byteBuffer);
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
        if(bound){
            //possible alternative: Gl.bufferSubData(GL20.GL_ARRAY_BUFFER, 0, byteBuffer.limit(), byteBuffer);
            Gl.bufferData(Gl.arrayBuffer, byteBuffer.limit(), byteBuffer, usage);
            dirty = false;
        }
    }

    @Override
    public void set(float[] vertices, int offset, int count){
        dirty = true;
        Buffers.copy(vertices, byteBuffer, count, offset);
        buffer.position(0);
        buffer.limit(count);
        bufferChanged();
    }

    @Override
    public void update(int targetOffset, float[] vertices, int sourceOffset, int count){
        dirty = true;
        final int pos = byteBuffer.position();
        byteBuffer.position(targetOffset * 4);
        Buffers.copy(vertices, sourceOffset, count, byteBuffer);
        byteBuffer.position(pos);
        buffer.position(0);
        bufferChanged();
    }

    /**
     * Binds the buffer and updates data if necessary. Does not activate/deactivate attributes.
     * Advanced use only.
     * */
    public void bind(){
        Gl.bindBuffer(Gl.arrayBuffer, bufferHandle);
        if(dirty){
            byteBuffer.limit(buffer.limit() * 4);
            Gl.bufferData(Gl.arrayBuffer, byteBuffer.limit(), byteBuffer, usage);
            dirty = false;
        }

        bound = true;
    }

    /**
     * Binds this VertexBufferObject for rendering via glDrawArrays or glDrawElements
     * @param shader the shader
     */
    @Override
    public void bind(Shader shader){
        bind();

        int offset = 0;
        for(VertexAttribute attribute : mesh.attributes){
            int location = shader.getAttributeLocation(attribute.alias);
            int aoffset = offset;
            offset += attribute.size;
            if(location < 0) continue;

            shader.enableVertexAttribute(location);
            shader.setVertexAttribute(location, attribute.components, attribute.type, attribute.normalized, mesh.vertexSize, aoffset);
        }
    }

    @Override
    public void unbind(Shader shader){
        for(VertexAttribute attribute : mesh.attributes){
            shader.disableVertexAttribute(attribute.alias);
        }
        Gl.bindBuffer(Gl.arrayBuffer, 0);
        bound = false;
    }

    /** Disposes of all resources this VertexBufferObject uses. */
    @Override
    public void dispose(){
        Gl.bindBuffer(Gl.arrayBuffer, 0);
        Gl.deleteBuffer(bufferHandle);
        bufferHandle = 0;
        if(ownsBuffer) Buffers.disposeUnsafeByteBuffer(byteBuffer);
    }
}
