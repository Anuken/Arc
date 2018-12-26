package io.anuke.arc.graphics.g2d;

import io.anuke.arc.graphics.glutils.Shader;

public class BatchShader{
    private static final String vertexShader =
    String.join("\n",
        "attribute vec4 " + Shader.POSITION_ATTRIBUTE + ";",
        "attribute vec4 " + Shader.COLOR_ATTRIBUTE + ";",
        "attribute vec2 " + Shader.TEXCOORD_ATTRIBUTE + "0;",
        "uniform mat4 u_projTrans;",
        "varying vec4 v_color;",
        "varying vec2 v_texCoords;",
        "",
        "void main(){",
        "   v_color = " + Shader.COLOR_ATTRIBUTE + ";",
        "   v_color.a = v_color.a * (255.0/254.0);",
        "   v_texCoords = " + Shader.TEXCOORD_ATTRIBUTE + "0;",
        "   gl_Position =  u_projTrans * " + Shader.POSITION_ATTRIBUTE + ";",
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
    public static Shader create(){
        return new Shader(vertexShader, fragmentShader);
    }
}
