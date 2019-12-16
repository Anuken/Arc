package io.anuke.arc.backends.teavm.emu;

import io.anuke.arc.*;
import io.anuke.arc.backends.teavm.plugin.Annotations.*;
import io.anuke.arc.graphics.*;
import io.anuke.arc.graphics.VertexAttributes.*;
import io.anuke.arc.graphics.glutils.*;
import io.anuke.arc.util.*;

import java.nio.*;

@Replace(VertexArray.class)
public class VertexArrayEmulator implements VertexData{
    final VertexAttributes attributes;
    final FloatBuffer buffer;
    int bufferHandle;
    final boolean isStatic;
    final int usage;
    boolean isDirty = false;
    boolean isBound = false;

    public VertexArrayEmulator(int numVertices, VertexAttribute... attributes){
        this(numVertices, new VertexAttributes(attributes));
    }

    /**
     * Constructs a new interleaved VertexArray
     * @param numVertices the maximum number of vertices
     * @param attributes the {@link VertexAttributes}
     */
    public VertexArrayEmulator(int numVertices, VertexAttributes attributes){
        this(false, numVertices, attributes);
    }

    /**
     * Constructs a new interleaved VertexBufferObject.
     * @param isStatic whether the vertex data is static.
     * @param numVertices the maximum number of vertices
     * @param attributes the {@link VertexAttribute}s.
     */
    public VertexArrayEmulator(boolean isStatic, int numVertices, VertexAttribute... attributes){
        this(isStatic, numVertices, new VertexAttributes(attributes));
    }

    /**
     * Constructs a new interleaved VertexBufferObject.
     * @param isStatic whether the vertex data is static.
     * @param numVertices the maximum number of vertices
     * @param attributes the {@link VertexAttributes}.
     */
    public VertexArrayEmulator(boolean isStatic, int numVertices, VertexAttributes attributes){
        this.isStatic = isStatic;
        this.attributes = attributes;

        buffer = BufferUtils.newFloatBuffer(this.attributes.vertexSize / 4 * numVertices);
        buffer.flip();
        bufferHandle = Core.gl20.glGenBuffer();
        usage = isStatic ? GL20.GL_STATIC_DRAW : GL20.GL_DYNAMIC_DRAW;
    }

    @Override
    public VertexAttributes getAttributes(){
        return attributes;
    }

    @Override
    public int getNumVertices(){
        return buffer.limit() / (attributes.vertexSize / 4);
    }

    @Override
    public int getNumMaxVertices(){
        return buffer.capacity() / (attributes.vertexSize / 4);
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
     * Binds this VertexBufferObject for rendering via glDrawArrays or
     * glDrawElements
     *
     * @param shader
     *            the shader
     */
    /**
     * Binds this VertexBufferObject for rendering via glDrawArrays or
     * glDrawElements
     * @param shader the shader
     */
    @Override
    public void bind(Shader shader){
        bind(shader, null);
    }

    @Override
    public void bind(Shader shader, int[] locations){
        final GL20 gl = Core.gl20;

        gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, bufferHandle);
        if(isDirty){
            gl.glBufferData(GL20.GL_ARRAY_BUFFER, buffer.limit(), buffer, usage);
            isDirty = false;
        }

        final int numAttributes = attributes.size();
        if(locations == null){
            for(int i = 0; i < numAttributes; i++){
                final VertexAttribute attribute = attributes.get(i);
                final int location = shader.getAttributeLocation(attribute.alias);
                if(location < 0){
                    continue;
                }
                shader.enableVertexAttribute(location);

                if(attribute.usage == Usage.ColorPacked)
                    shader.setVertexAttribute(location, attribute.numComponents, GL20.GL_UNSIGNED_BYTE, true, attributes.vertexSize, attribute.offset);
                else
                    shader.setVertexAttribute(location, attribute.numComponents, GL20.GL_FLOAT, false, attributes.vertexSize, attribute.offset);
            }
        }else{
            for(int i = 0; i < numAttributes; i++){
                final VertexAttribute attribute = attributes.get(i);
                final int location = locations[i];
                if(location < 0){
                    continue;
                }
                shader.enableVertexAttribute(location);

                if(attribute.usage == Usage.ColorPacked)
                    shader.setVertexAttribute(location, attribute.numComponents, GL20.GL_UNSIGNED_BYTE, true,
                    attributes.vertexSize, attribute.offset);
                else
                    shader.setVertexAttribute(location, attribute.numComponents, GL20.GL_FLOAT, false,
                    attributes.vertexSize, attribute.offset);
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
        final GL20 gl = Core.gl20;
        final int numAttributes = attributes.size();
        if(locations == null){
            for(int i = 0; i < numAttributes; i++){
                shader.disableVertexAttribute(attributes.get(i).alias);
            }
        }else{
            for(int i = 0; i < numAttributes; i++){
                final int location = locations[i];
                if(location >= 0){
                    shader.disableVertexAttribute(location);
                }
            }
        }
        gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
        isBound = false;
    }

    /**
     * Invalidates the VertexBufferObject so a new OpenGL buffer handle is
     * created. Use this in case of a context loss.
     */
    @Override
    public void invalidate(){
        bufferHandle = Core.gl20.glGenBuffer();
        isDirty = true;
    }

    /** Disposes of all resources this VertexBufferObject uses. */
    @Override
    public void dispose(){
        GL20 gl = Core.gl20;
        gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
        gl.glDeleteBuffer(bufferHandle);
        bufferHandle = 0;
    }
}
