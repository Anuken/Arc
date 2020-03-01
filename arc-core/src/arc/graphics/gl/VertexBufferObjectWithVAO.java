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

    final VertexAttributes attributes;
    final FloatBuffer buffer;
    final ByteBuffer byteBuffer;
    final boolean isStatic;
    final int usage;
    int bufferHandle;
    boolean isDirty = false;
    boolean isBound = false;
    int vaoHandle = -1;
    IntArray cachedLocations = new IntArray();


    /**
     * Constructs a new interleaved VertexBufferObjectWithVAO.
     * @param isStatic whether the vertex data is static.
     * @param numVertices the maximum number of vertices
     * @param attributes the {@link arc.graphics.VertexAttribute}s.
     */
    public VertexBufferObjectWithVAO(boolean isStatic, int numVertices, VertexAttribute... attributes){
        this(isStatic, numVertices, new VertexAttributes(attributes));
    }

    /**
     * Constructs a new interleaved VertexBufferObjectWithVAO.
     * @param isStatic whether the vertex data is static.
     * @param numVertices the maximum number of vertices
     * @param attributes the {@link VertexAttributes}.
     */
    public VertexBufferObjectWithVAO(boolean isStatic, int numVertices, VertexAttributes attributes){
        this.isStatic = isStatic;
        this.attributes = attributes;

        byteBuffer = Buffers.newUnsafeByteBuffer(this.attributes.vertexSize * numVertices);
        buffer = byteBuffer.asFloatBuffer();
        buffer.flip();
        byteBuffer.flip();
        bufferHandle = Gl.genBuffer();
        usage = isStatic ? GL20.GL_STATIC_DRAW : GL20.GL_DYNAMIC_DRAW;
        createVAO();
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
            Gl.bufferData(GL20.GL_ARRAY_BUFFER, byteBuffer.limit(), byteBuffer, usage);
            isDirty = false;
        }
    }

    @Override
    public void setVertices(float[] vertices, int offset, int count){
        isDirty = true;
        Buffers.copy(vertices, byteBuffer, count, offset);
        buffer.position(0);
        buffer.limit(count);
        bufferChanged();
    }

    @Override
    public void updateVertices(int targetOffset, float[] vertices, int sourceOffset, int count){
        isDirty = true;
        final int pos = byteBuffer.position();
        byteBuffer.position(targetOffset * 4);
        Buffers.copy(vertices, sourceOffset, count, byteBuffer);
        byteBuffer.position(pos);
        buffer.position(0);
        bufferChanged();
    }

    /**
     * Binds this VertexBufferObject for rendering via glDrawArrays or glDrawElements
     * @param shader the shader
     */
    @Override
    public void bind(Shader shader){
        bind(shader, null);
    }

    @Override
    public void bind(Shader shader, int[] locations){
        GL30 gl = Core.gl30;

        gl.glBindVertexArray(vaoHandle);

        bindAttributes(shader, locations);

        //if our data has changed upload it:
        bindData(gl);

        isBound = true;
    }

    private void bindAttributes(Shader shader, int[] locations){
        boolean stillValid = this.cachedLocations.size != 0;
        final int numAttributes = attributes.size();

        if(stillValid){
            if(locations == null){
                for(int i = 0; stillValid && i < numAttributes; i++){
                    VertexAttribute attribute = attributes.get(i);
                    int location = shader.getAttributeLocation(attribute.alias);
                    stillValid = location == this.cachedLocations.get(i);
                }
            }else{
                stillValid = locations.length == this.cachedLocations.size;
                for(int i = 0; stillValid && i < numAttributes; i++){
                    stillValid = locations[i] == this.cachedLocations.get(i);
                }
            }
        }

        if(!stillValid){
            Gl.bindBuffer(GL20.GL_ARRAY_BUFFER, bufferHandle);
            unbindAttributes(shader);
            this.cachedLocations.clear();

            for(int i = 0; i < numAttributes; i++){
                VertexAttribute attribute = attributes.get(i);
                if(locations == null){
                    this.cachedLocations.add(shader.getAttributeLocation(attribute.alias));
                }else{
                    this.cachedLocations.add(locations[i]);
                }

                int location = this.cachedLocations.get(i);
                if(location < 0){
                    continue;
                }

                shader.enableVertexAttribute(location);
                shader.setVertexAttribute(location, attribute.numComponents, attribute.type, attribute.normalized, attributes.vertexSize, attribute.offset);
            }
        }
    }

    private void unbindAttributes(Shader shader){
        if(cachedLocations.size == 0){
            return;
        }
        int numAttributes = attributes.size();
        for(int i = 0; i < numAttributes; i++){
            int location = cachedLocations.get(i);
            if(location < 0){
                continue;
            }
            shader.disableVertexAttribute(location);
        }
    }

    private void bindData(GL20 gl){
        if(isDirty){
            gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, bufferHandle);
            byteBuffer.limit(buffer.limit() * 4);
            gl.glBufferData(GL20.GL_ARRAY_BUFFER, byteBuffer.limit(), byteBuffer, usage);
            isDirty = false;
        }
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
        GL30 gl = Core.gl30;
        gl.glBindVertexArray(0);
        isBound = false;
    }

    /**
     * Invalidates the VertexBufferObject so a new OpenGL buffer handle is created. Use this in case of a context loss.
     */
    @Override
    public void invalidate(){
        bufferHandle = Core.gl30.glGenBuffer();
        createVAO();
        isDirty = true;
    }

    /**
     * Disposes of all resources this VertexBufferObject uses.
     */
    @Override
    public void dispose(){
        GL30 gl = Core.gl30;

        gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
        gl.glDeleteBuffer(bufferHandle);
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
