package io.anuke.arc.graphics.glutils;

import io.anuke.arc.graphics.VertexAttribute;
import io.anuke.arc.graphics.VertexAttributes;

/**
 * <p>
 * A {@link VertexData} implementation based on OpenGL vertex buffer objects.
 * </p>
 *
 * <p>
 * If the OpenGL ES context was lost you can call {@link #invalidate()} to recreate a new OpenGL vertex buffer object. This class
 * can be used seamlessly with OpenGL ES 1.x and 2.0.
 * </p>
 *
 * <p>
 * In case OpenGL ES 2.0 is used in the application the data is bound via glVertexAttribPointer() according to the attribute
 * aliases specified via {@link VertexAttributes} in the constructor.
 * </p>
 *
 * <p>
 * Uses indirect Buffers on Android 1.5/1.6 to fix GC invocation due to leaking PlatformAddress instances.
 * </p>
 *
 * <p>
 * VertexBufferObjects must be disposed via the {@link #dispose()} method when no longer needed
 * </p>
 * @author mzechner, Dave Clayton <contact@redskyforge.com>
 */
public class VertexArray extends VertexBufferObject{
    public VertexArray(int numVertices, VertexAttribute... attributes){
        this(numVertices, new VertexAttributes(attributes));
    }

    /**
     * Constructs a new interleaved VertexArray
     * @param numVertices the maximum number of vertices
     * @param attributes the {@link VertexAttributes}
     */
    public VertexArray(int numVertices, VertexAttributes attributes){
        super(false, numVertices, attributes);
    }
}
