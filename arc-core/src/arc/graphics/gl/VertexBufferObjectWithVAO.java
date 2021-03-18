package arc.graphics.gl;

import arc.*;
import arc.graphics.*;
import arc.struct.*;
import arc.util.*;

import java.nio.*;

/**
 * <p>
 * A {@link VertexData} implementation that uses vertex buffer objects and vertex array objects.
 * (This is required for OpenGL 3.0+ core profiles. In particular, the default VAO has been
 * deprecated, as has the use of client memory for passing vertex attributes.) Use of VAOs should
 * give a slight performance benefit since you don't have to bind the attributes on every draw
 * anymore.
 * </p>
 *
 * <p>
 * If the OpenGL ES context was lost you can call {@link #invalidate()} to recreate a new OpenGL vertex buffer object.
 * </p>
 *
 * <p>
 * VertexBufferObjectWithVAO objects must be disposed via the {@link #dispose()} method when no longer needed
 * </p>
 * <p>
 * Code adapted from {@link VertexBufferObject}.
 * @author mzechner, Dave Clayton <contact@redskyforge.com>, Nate Austin <nate.austin gmail>
 */
public class VertexBufferObjectWithVAO implements VertexData{
    final static IntBuffer tmpHandle = Buffers.newIntBuffer(1);

    final Mesh mesh;
    final FloatBuffer buffer;
    final ByteBuffer byteBuffer;
    final boolean isStatic;
    final int usage;
    int bufferHandle;
    boolean isDirty = false;
    boolean isBound = false;
    int vaoHandle = -1;
    IntSeq cachedLocations = new IntSeq();

    /**
     * Constructs a new interleaved VertexBufferObjectWithVAO.
     * @param isStatic whether the vertex data is static.
     * @param numVertices the maximum number of vertices
     */
    public VertexBufferObjectWithVAO(boolean isStatic, int numVertices, Mesh mesh){
        this.isStatic = isStatic;
        this.mesh = mesh;

        byteBuffer = Buffers.newUnsafeByteBuffer(this.mesh.vertexSize * numVertices);
        buffer = byteBuffer.asFloatBuffer();
        buffer.flip();
        byteBuffer.flip();
        bufferHandle = Gl.genBuffer();
        usage = isStatic ? GL20.GL_STATIC_DRAW : GL20.GL_STREAM_DRAW;
        createVAO();
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
        isDirty = true;
        return buffer;
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

    @Override
    public void bind(Shader shader){
        Core.gl30.glBindVertexArray(vaoHandle);

        bindAttributes(shader);

        //if our data has changed upload it
        bindData();

        isBound = true;
    }

    private void bindAttributes(Shader shader){
        boolean stillValid = this.cachedLocations.size != 0;

        if(stillValid){
            for(int i = 0; stillValid && i < mesh.attributes.length; i++){
                VertexAttribute attribute = mesh.attributes[i];
                int location = shader.getAttributeLocation(attribute.alias);
                stillValid = location == this.cachedLocations.get(i);
            }
        }

        if(!stillValid){
            Gl.bindBuffer(GL20.GL_ARRAY_BUFFER, bufferHandle);
            unbindAttributes(shader);
            this.cachedLocations.clear();

            int offset = 0;
            for(int i = 0; i < mesh.attributes.length; i++){
                VertexAttribute attribute = mesh.attributes[i];
                this.cachedLocations.add(shader.getAttributeLocation(attribute.alias));
                int aoffset = offset;
                offset += attribute.size;

                int location = this.cachedLocations.get(i);
                if(location < 0){
                    continue;
                }

                shader.enableVertexAttribute(location);
                shader.setVertexAttribute(location, attribute.components, attribute.type, attribute.normalized, mesh.vertexSize, aoffset);
            }
        }
    }

    private void unbindAttributes(Shader shader){
        if(cachedLocations.size == 0){
            return;
        }

        for(int i = 0; i < mesh.attributes.length; i++){
            int location = cachedLocations.get(i);
            if(location < 0){
                continue;
            }
            shader.disableVertexAttribute(location);
        }
    }

    private void bindData(){
        if(isDirty){
            Core.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, bufferHandle);
            byteBuffer.limit(buffer.limit() * 4);
            Core.gl.glBufferData(GL20.GL_ARRAY_BUFFER, byteBuffer.limit(), byteBuffer, usage);
            isDirty = false;
        }
    }

    @Override
    public void unbind(Shader shader){
        Core.gl30.glBindVertexArray(0);
        isBound = false;
    }

    /**
     * Disposes of all resources this VertexBufferObject uses.
     */
    @Override
    public void dispose(){
        Core.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
        Core.gl.glDeleteBuffer(bufferHandle);
        bufferHandle = 0;
        Buffers.disposeUnsafeByteBuffer(byteBuffer);
        deleteVAO();
    }

    private void createVAO(){
        tmpHandle.clear();
        Core.gl30.glGenVertexArrays(1, tmpHandle);
        vaoHandle = tmpHandle.get();
    }

    private void deleteVAO(){
        if(vaoHandle != -1){
            tmpHandle.clear();
            tmpHandle.put(vaoHandle);
            tmpHandle.flip();
            Core.gl30.glDeleteVertexArrays(1, tmpHandle);
            vaoHandle = -1;
        }
    }
}
