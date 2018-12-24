package io.anuke.arc.graphics.g2d;

import io.anuke.arc.graphics.glutils.ShaderProgram;

public class BatchShader{
    private static final String vertexShader =
    String.join("\n",
        "attribute vec3 " + ShaderProgram.POSITION_ATTRIBUTE + ";",
        "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";",
        "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;",
        "uniform mat3 u_projTrans;",
        "varying vec4 v_color;",
        "varying vec2 v_texCoords;",
        "",
        "void main(){",
        "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";",
        "   v_color.a = v_color.a * (255.0/254.0);",
        "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;",
        "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";",
        "}"
    );
    private static final String fragmentShader =
    String.join("\n",
        "#ifdef GL_ES",
        "#define LOWP lowp",
        "precision mediump float;",
        "#else",
        "#define LOWP ",
        "#endif",
        "",
        "varying LOWP vec4 v_color;",
        "varying vec2 v_texCoords;",
        "uniform sampler2D u_texture;",
        "",
        "void main(){",
        "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);",
        "}"
    );

    /** Returns a new instance of the default shader used by SpriteBatch for GL2 when no shader is specified. */
    public static ShaderProgram create(){
        ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
        if(!shader.isCompiled()) throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
        return shader;
    }
}
