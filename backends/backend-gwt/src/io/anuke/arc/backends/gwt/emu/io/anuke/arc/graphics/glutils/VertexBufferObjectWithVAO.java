package io.anuke.arc.graphics.glutils;

import io.anuke.arc.Core;
import io.anuke.arc.collection.IntArray;
import io.anuke.arc.graphics.GL20;
import io.anuke.arc.graphics.GL30;
import io.anuke.arc.graphics.VertexAttribute;
import io.anuke.arc.graphics.VertexAttributes;
import io.anuke.arc.util.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

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
    final static IntBuffer tmpHandle = BufferUtils.newIntBuffer(1);

    final VertexAttributes attributes;
    final FloatBuffer buffer;
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
     * @param attributes the {@link io.anuke.arc.graphics.VertexAttribute}s.
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

        buffer = BufferUtils.newFloatBuffer(this.attributes.vertexSize / 4 * numVertices);
        buffer.flip();
        bufferHandle = Core.gl20.glGenBuffer();
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
        return buffer.capacity() * 4 / attributes.vertexSize;
    }

    @Override
    public FloatBuffer getBuffer(){
        isDirty = true;
        return buffer;
    }

    private void bufferChanged(){
        if(isBound){
            Core.gl20.glBufferData(GL20.GL_ARRAY_BUFFER, buffer.limit(), buffer, usage);
            isDirty = false;
        }
    }

    @Override
    public void setVertices(float[] vertices, int offset, int count){
        isDirty = true;
        BufferUtils.copy(vertices, buffer, count, offset);
        buffer.position(0);
        buffer.limit(count);
        bufferChanged();
    }

    @Override
    public void updateVertices(int targetOffset, float[] vertices, int sourceOffset, int count){
        isDirty = true;
        final int pos = buffer.position();
        buffer.position(targetOffset);
        BufferUtils.copy(vertices, sourceOffset, count, buffer);
        buffer.position(pos);
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
            Core.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, bufferHandle);
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
        if(cachedLocations == null){
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
            buffer.limit(buffer.limit());
            gl.glBufferData(GL20.GL_ARRAY_BUFFER, buffer.limit(), buffer, usage);
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
        bufferHandle = Core.gl20.glGenBuffer();
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
