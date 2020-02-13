package arc.fx.util;

import arc.graphics.*;
import arc.graphics.Mesh.*;
import arc.graphics.VertexAttributes.*;
import arc.graphics.gl.*;
import arc.util.*;

/**
 * Encapsulates a fullscreen quad mesh. Geometry is aligned to the screen corners.
 * @author bmanuel
 * @author metaphore
 */
public class ScreenQuad implements Disposable{
    private static final float[] verts = {-1f, -1f, 0f, 0f, 1f, -1f, 1f, 0f, 1f, 1f, 1f, 1f, -1f, 1f, 0f, 1f};
    public final Mesh mesh;

    public ScreenQuad(){
        mesh = new Mesh(VertexDataType.VertexArray, true, 4, 0,
            new VertexAttribute(Usage.position, 2, "a_position"),
            new VertexAttribute(Usage.textureCoordinates, 2, "a_texCoord0"));
        mesh.setVertices(verts);
    }

    public void render(Shader program){
        mesh.render(program, Gl.triangleFan, 0, 4);
    }

    @Override
    public void dispose(){
        mesh.dispose();
    }
}
