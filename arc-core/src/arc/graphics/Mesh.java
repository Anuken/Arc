package arc.graphics;

import arc.*;
import arc.graphics.gl.*;
import arc.util.*;

import java.nio.*;

/**
 * <p>
 * A Mesh holds vertices composed of attributes specified by an array of {@link VertexAttribute} instances. The vertices are held either in
 * VRAM in form of vertex buffer objects or in RAM in form of vertex arrays. The former variant is more performant and is
 * preferred over vertex arrays if hardware supports it.
 * </p>
 *
 * <p>
 * Meshes are automatically managed. If the OpenGL context is lost all vertex buffer objects get invalidated and must be reloaded
 * when the context is recreated. This only happens on Android when a user switches to another application or receives an incoming
 * call. A managed Mesh will be reloaded automagically so you don't have to do this manually.
 * </p>
 *
 * <p>
 * A Mesh consists of vertices and optionally indices which specify which vertices define a triangle. Each vertex is composed of
 * attributes such as position, normal, color or texture coordinate. Note that not all of this attributes must be given, except
 * for position which is non-optional. Each attribute has an alias which is used when rendering a Mesh in OpenGL ES 2.0. The alias
 * is used to bind a specific vertex attribute to a shader attribute. The shader source and the alias of the attribute must match
 * exactly for this to work.
 * </p>
 * @author mzechner, Dave Clayton <contact@redskyforge.com>, Xoppa
 */
public class Mesh implements Disposable{
    /** If true, VAOs will be used on OpenGL 3.0+. This may improve performance. */
    public static boolean useVAO = true;

    /** The size of one vertex, in bytes. */
    public final int vertexSize;
    /** Do not modify. */
    public final VertexAttribute[] attributes;

    public final VertexData vertices;
    public final IndexData indices;

    boolean autoBind = true;

    /**
     * Creates a new Mesh with the given attributes.
     * @param isStatic whether this mesh is static or not. Allows for internal optimizations.
     * @param maxVertices the maximum number of vertices this mesh can hold
     * @param maxIndices the maximum number of indices this mesh can hold
     * @param attributes the {@link VertexAttribute}s. Each vertex attribute defines one property of a vertex such as position,
     * normal or texture coordinate
     */
    public Mesh(boolean isStatic, int maxVertices, int maxIndices, VertexAttribute... attributes){
        this(false, isStatic, maxVertices, maxIndices, attributes);
    }

    /**
     * Creates a new Mesh with the given attributes. This is an expert method with no error checking. Use at your own risk.
     * @param useVertexArray whether to use VBOs or VAOs. Note that the latter is not supported with OpenGL 3.0.
     * @param isStatic whether this mesh is static or not. Allows for internal optimizations.
     * @param maxVertices the maximum number of vertices this mesh can hold
     * @param maxIndices the maximum number of indices this mesh can hold
     */
    public Mesh(boolean useVertexArray, boolean isStatic, int maxVertices, int maxIndices, VertexAttribute... attributes){
        int count = 0;
        for(VertexAttribute attribute : attributes){
            count += attribute.size;
        }

        this.vertexSize = count;
        this.attributes = attributes;

        if(useVertexArray && Core.gl30 == null){
            vertices = new VertexArray(maxVertices, this);
            indices = new IndexArray(maxIndices);
        }else if(Core.gl30 != null && useVAO){
            vertices = new VertexBufferObjectWithVAO(isStatic, maxVertices, this);
            indices = new IndexBufferObjectSubData(isStatic, maxIndices);
        }else{
            vertices = new VertexBufferObject(isStatic, maxVertices, this);
            indices = new IndexBufferObject(isStatic, maxIndices);
        }
    }

    /**
     * Sets the vertices of this Mesh. The attributes are assumed to be given in float format.
     * @param vertices the vertices.
     * @return the mesh for invocation chaining.
     */
    public Mesh setVertices(float[] vertices){
        this.vertices.set(vertices, 0, vertices.length);

        return this;
    }

    /**
     * Sets the vertices of this Mesh. The attributes are assumed to be given in float format.
     * @param vertices the vertices.
     * @param offset the offset into the vertices array
     * @param count the number of floats to use
     * @return the mesh for invocation chaining.
     */
    public Mesh setVertices(float[] vertices, int offset, int count){
        this.vertices.set(vertices, offset, count);

        return this;
    }

    /**
     * Update (a portion of) the vertices. Does not resize the backing buffer.
     * @param targetOffset the offset in number of floats of the mesh part.
     * @param source the vertex data to update the mesh part with
     */
    public Mesh updateVertices(int targetOffset, float[] source){
        return updateVertices(targetOffset, source, 0, source.length);
    }

    /**
     * Update (a portion of) the vertices. Does not resize the backing buffer.
     * @param targetOffset the offset in number of floats of the mesh part.
     * @param source the vertex data to update the mesh part with
     * @param sourceOffset the offset in number of floats within the source array
     * @param count the number of floats to update
     */
    public Mesh updateVertices(int targetOffset, float[] source, int sourceOffset, int count){
        this.vertices.update(targetOffset, source, sourceOffset, count);
        return this;
    }

    /**
     * Sets the indices of this Mesh
     * @param indices the indices
     * @return the mesh for invocation chaining.
     */
    public Mesh setIndices(short[] indices){
        this.indices.set(indices, 0, indices.length);

        return this;
    }

    /**
     * Sets the indices of this Mesh.
     * @param indices the indices
     * @param offset the offset into the indices array
     * @param count the number of indices to copy
     * @return the mesh for invocation chaining.
     */
    public Mesh setIndices(short[] indices, int offset, int count){
        this.indices.set(indices, offset, count);

        return this;
    }

    /**
     * Copies the indices from the Mesh to the short array. The short array must be large enough to hold all the Mesh's indices.
     * @param indices the array to copy the indices to
     */
    public void getIndices(short[] indices){
        getIndices(indices, 0);
    }

    /**
     * Copies the indices from the Mesh to the short array. The short array must be large enough to hold destOffset + all the
     * Mesh's indices.
     * @param indices the array to copy the indices to
     * @param destOffset the offset in the indices array to start copying
     */
    public void getIndices(short[] indices, int destOffset){
        getIndices(0, indices, destOffset);
    }

    /**
     * Copies the remaining indices from the Mesh to the short array. The short array must be large enough to hold destOffset + all
     * the remaining indices.
     * @param srcOffset the zero-based offset of the first index to fetch
     * @param indices the array to copy the indices to
     * @param destOffset the offset in the indices array to start copying
     */
    public void getIndices(int srcOffset, short[] indices, int destOffset){
        getIndices(srcOffset, -1, indices, destOffset);
    }

    /**
     * Copies the indices from the Mesh to the short array. The short array must be large enough to hold destOffset + count
     * indices.
     * @param srcOffset the zero-based offset of the first index to fetch
     * @param count the total amount of indices to copy
     * @param indices the array to copy the indices to
     * @param destOffset the offset in the indices array to start copying
     */
    public void getIndices(int srcOffset, int count, short[] indices, int destOffset){
        int max = getNumIndices();
        if(count < 0) count = max - srcOffset;
        if(srcOffset < 0 || srcOffset >= max || srcOffset + count > max)
            throw new IllegalArgumentException("Invalid range specified, offset: " + srcOffset + ", count: " + count + ", max: "
            + max);
        if((indices.length - destOffset) < count)
            throw new IllegalArgumentException("not enough room in indices array, has " + indices.length + " shorts, needs " + count);
        int pos = getIndicesBuffer().position();
        getIndicesBuffer().position(srcOffset);
        getIndicesBuffer().get(indices, destOffset, count);
        getIndicesBuffer().position(pos);
    }

    /** @return the number of defined indices */
    public int getNumIndices(){
        return indices.size();
    }

    /** @return the number of defined vertices */
    public int getNumVertices(){
        return vertices.size();
    }

    /** @return the maximum number of vertices this mesh can hold */
    public int getMaxVertices(){
        return vertices.max();
    }

    /** @return the maximum number of indices this mesh can hold */
    public int getMaxIndices(){
        return indices.max();
    }

    /** @return the size of a single vertex in bytes */
    public int getVertexSize(){
        return vertexSize;
    }

    /**
     * Sets whether to bind the underlying {@link VertexArray} or {@link VertexBufferObject} automatically on a call to one of the
     * render methods. Usually you want to use autobind. Manual binding is an expert functionality. There is a driver bug on the
     * MSM720xa chips that will fuck up memory if you manipulate the vertices and indices of a Mesh multiple times while it is
     * bound. Keep this in mind.
     * @param autoBind whether to autobind meshes.
     */
    public void setAutoBind(boolean autoBind){
        this.autoBind = autoBind;
    }

    /**
     * Binds the underlying {@link VertexBufferObject} and {@link IndexBufferObject} if indices where given. Use this with OpenGL
     * ES 2.0 and when auto-bind is disabled.
     * @param shader the shader (does not bind the shader)
     */
    public void bind(final Shader shader){
        vertices.bind(shader);
        if(indices.size() > 0) indices.bind();
    }

    /**
     * Unbinds the underlying {@link VertexBufferObject} and {@link IndexBufferObject} is indices were given. Use this with OpenGL
     * ES 1.x and when auto-bind is disabled.
     * @param shader the shader (does not unbind the shader)
     */
    public void unbind(final Shader shader){
        vertices.unbind(shader);
        if(indices.size() > 0) indices.unbind();
    }

    /** @see #render(Shader, int, int, int, boolean) */
    public void render(Shader shader, int primitiveType){
        render(shader, primitiveType, 0, indices.max() > 0 ? getNumIndices() : getNumVertices(), autoBind);
    }

    /** @see #render(Shader, int, int, int, boolean) */
    public void render(Shader shader, int primitiveType, int offset, int count){
        render(shader, primitiveType, offset, count, autoBind);
    }

    /**
     * <p>
     * Renders the mesh using the given primitive type. offset specifies the offset into either the vertex buffer or the index
     * buffer depending on whether indices are defined. count specifies the number of vertices or indices to use thus count /
     * #vertices per primitive primitives are rendered.
     * </p>
     *
     * <p>
     * This method will automatically bind each vertex attribute as specified at construction time to
     * the respective shader attributes. The binding is based on the alias defined for each VertexAttribute.
     * </p>
     *
     * <p>
     * This method must only be called after the {@link Shader#bind()} method has been called!
     * </p>
     *
     * <p>
     * This method is intended for use with OpenGL ES 2.0 and will throw an IllegalStateException when OpenGL ES 1.x is used.
     * </p>
     * @param shader the shader to be used
     * @param primitiveType the primitive type
     * @param offset the offset into the vertex or index buffer
     * @param count number of vertices or indices to use
     * @param autoBind overrides the autoBind member of this Mesh
     */
    public void render(Shader shader, int primitiveType, int offset, int count, boolean autoBind){
        if(count == 0) return;

        if(autoBind) bind(shader);

        vertices.render(indices, primitiveType, offset, count);

        if(autoBind) unbind(shader);
    }

    /** Frees all resources associated with this Mesh */
    @Override
    public void dispose(){
        vertices.dispose();
        indices.dispose();
    }

    /** @return the backing FloatBuffer holding the vertices. Does not have to be a direct buffer on Android! */
    public FloatBuffer getVerticesBuffer(){
        return vertices.buffer();
    }

    /** @return the backing shortbuffer holding the indices. Does not have to be a direct buffer on Android! */
    public ShortBuffer getIndicesBuffer(){
        return indices.buffer();
    }
}
