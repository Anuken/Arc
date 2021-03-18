package arc.graphics;

/**
 * Instances of this class specify the vertex attributes of a mesh. VertexAttributes are used by {@link Mesh} instances to define
 * its vertex structure. Vertex attributes have an order. The order is specified by the order they are added to this class.
 * @author mzechner, Xoppa
 */
public final class VertexAttributes{
    /** the size of a single vertex in bytes **/
    public final int vertexSize;
    /** the attributes in the order they were specified **/
    public final VertexAttribute[] attributes;

    /** Constructor, sets the vertex attributes in a specific order */
    public VertexAttributes(VertexAttribute... attributes){
        if(attributes.length == 0) throw new IllegalArgumentException("attributes must be >= 1");

        this.attributes = attributes;
        int count = 0;
        for(int i = 0; i < this.attributes.length; i++){
            count += attributes[i].size;
        }

        vertexSize = count;
    }

    /** @return the number of attributes */
    public int size(){
        return attributes.length;
    }

    /**
     * @param index the index
     * @return the VertexAttribute at the given index
     */
    public VertexAttribute get(int index){
        return attributes[index];
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for(VertexAttribute attribute : attributes){
            builder.append("(");
            builder.append(attribute.alias);
            builder.append(", ");
            builder.append(attribute.components);
            builder.append(")");
            builder.append("\n");
        }
        builder.append("]");
        return builder.toString();
    }

}
