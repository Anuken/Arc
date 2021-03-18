package arc.graphics;

import arc.graphics.gl.Shader;

/**
 * A single vertex attribute defined by its number of components and its shader alias. The number of components
 * defines how many components the attribute has. The alias defines to which shader attribute this attribute should bind. The alias
 * is used by a {@link Mesh} when drawing with a {@link Shader}.
 * @author mzechner
 */
public final class VertexAttribute{

    public static final VertexAttribute

    position = new VertexAttribute(2, Shader.positionAttribute),
    position3 = new VertexAttribute(3, Shader.positionAttribute),
    texCoords = new VertexAttribute(2, Shader.texcoordAttribute + "0"),
    normal = new VertexAttribute(3, Shader.normalAttribute),
    color = new VertexAttribute(4, GL20.GL_UNSIGNED_BYTE, true, Shader.colorAttribute),
    mixColor = new VertexAttribute(4, GL20.GL_UNSIGNED_BYTE, true, Shader.mixColorAttribute);

    /** the number of components this attribute has **/
    public final int components;
    /** For fixed types, whether the values are normalized to either -1f and +1f (signed) or 0f and +1f (unsigned) */
    public final boolean normalized;
    /** the OpenGL type of each component, e.g. {@link GL20#GL_FLOAT} or {@link GL20#GL_UNSIGNED_BYTE} */
    public final int type;
    /** the alias for the attribute used in a {@link Shader} **/
    public final String alias;
    /** the size (in bytes) of this attribute */
    public final int size;

    /**
     * Constructs a new VertexAttribute. The GL data type is automatically selected based on the usage.
     * @param components the number of components of this attribute, must be between 1 and 4.
     * @param alias the alias used in a shader for this attribute. Can be changed after construction.
     */
    public VertexAttribute(int components, String alias){
        this(components, GL20.GL_FLOAT, false, alias);
    }

    /**
     * Constructs a new VertexAttribute.
     * @param components the number of components of this attribute, must be between 1 and 4.
     * @param type the OpenGL type of each component, e.g. {@link GL20#GL_FLOAT} or {@link GL20#GL_UNSIGNED_BYTE}. Since {@link Mesh}
     * stores vertex data in 32bit floats, the total size of this attribute (type size times number of components) must be a
     * multiple of four bytes.
     * @param normalized For fixed types, whether the values are normalized to either -1f and +1f (signed) or 0f and +1f (unsigned)
     * @param alias The alias used in a shader for this attribute. Can be changed after construction.
     */
    public VertexAttribute(int components, int type, boolean normalized, String alias){
        this.components = components;
        this.type = type;
        this.normalized = normalized;
        this.alias = alias;

        //calculate final size based on components & type
        int realSize = 0;
        switch(type){
            case GL20.GL_FLOAT:
            case GL20.GL_FIXED:
                realSize = 4 * components;
                break;
            case GL20.GL_UNSIGNED_BYTE:
            case GL20.GL_BYTE:
                realSize = components;
                break;
            case GL20.GL_UNSIGNED_SHORT:
            case GL20.GL_SHORT:
                realSize = 2 * components;
                break;
        }
        size = realSize;
    }
}
