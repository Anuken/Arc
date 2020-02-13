package arc.fx.util;

import arc.*;
import arc.graphics.*;
import arc.graphics.gl.*;
import arc.util.*;

/**
 * Simple renderer that is capable of drawing
 * {@link FxBuffer}'s texture onto the screen or into another buffer.
 * <p>
 */
public class FxBufferRenderer implements Disposable{
    private final ScreenQuad mesh;
    private final Shader shader;

    public FxBufferRenderer(){
        mesh = new ScreenQuad();

        shader = new Shader(
        "#ifdef GL_ES\n" +
        "    #define PRECISION mediump\n" +
        "    precision PRECISION float;\n" +
        "#else\n" +
        "    #define PRECISION\n" +
        "#endif\n" +
        "attribute vec4 a_position;\n" +
        "attribute vec2 a_texCoord0;\n" +
        "varying vec2 v_texCoords;\n" +
        "void main() {\n" +
        "    v_texCoords = a_texCoord0;\n" +
        "    gl_Position = a_position;\n" +
        "}",
        "#ifdef GL_ES\n" +
        "    #define PRECISION mediump\n" +
        "    precision PRECISION float;\n" +
        "#else\n" +
        "    #define PRECISION\n" +
        "#endif\n" +
        "varying vec2 v_texCoords;\n" +
        "uniform sampler2D u_texture0;\n" +
        "void main() {\n" +
        "    gl_FragColor = texture2D(u_texture0, v_texCoords);\n" +
        "}"
        );

        rebind();
    }

    @Override
    public void dispose(){
        shader.dispose();
        mesh.dispose();
    }

    public void rebind(){
        shader.begin();
        shader.setUniformi("u_texture0", 0);
        shader.end();
    }

    public void renderToScreen(FxBuffer input){
        renderToScreen(input, 0, 0, Core.graphics.getBackBufferWidth(), Core.graphics.getBackBufferHeight());
    }

    public void renderToScreen(FxBuffer input, int x, int y, int width, int height){
        input.getFbo().getTexture().bind(0);
        Gl.viewport(x, y, width, height);

        shader.begin();
        mesh.render(shader);
        shader.end();
    }

    public void renderToFbo(FxBuffer input, FxBuffer output){
        input.getFbo().getTexture().bind(0);

        // Viewport will be set from VfxFrameBuffer#begin() method.

        output.begin();
        shader.begin();
        mesh.render(shader);
        shader.end();
        output.end();
    }

    public ScreenQuad getMesh(){
        return mesh;
    }
}
