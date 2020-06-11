package arc.graphics.g2d;

import arc.graphics.*;
import arc.graphics.VertexAttributes.*;
import arc.graphics.gl.*;
import arc.util.*;

public class ScreenQuad implements Disposable{
    public final Mesh mesh;

    public ScreenQuad(){
        mesh = new Mesh(true, 4, 0,
            new VertexAttribute(Usage.position, 2, "a_position"),
            new VertexAttribute(Usage.textureCoordinates, 2, "a_texCoord0"));
        mesh.setVertices(new float[]{-1f, -1f, 0f, 0f, 1f, -1f, 1f, 0f, 1f, 1f, 1f, 1f, -1f, 1f, 0f, 1f});
    }

    public void render(Shader shader){
        mesh.render(shader, Gl.triangleFan, 0, 4);
    }

    @Override
    public void dispose(){
        mesh.dispose();
    }
}
