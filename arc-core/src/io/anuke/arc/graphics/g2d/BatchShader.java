package io.anuke.arc.graphics.g2d;

import io.anuke.arc.graphics.glutils.Shader;
import io.anuke.arc.math.Matrix3;

public class BatchShader{
    private static final float[] val = new float[16];

    private static final int M00 = 0, M01 = 4, M02 = 8, M03 = 12, M10 = 1, M11 = 5, M12 = 9, M13 = 13, M20 = 2,
        M21 = 6, M22 = 10, M23 = 14, M30 = 3, M31 = 7, M32 = 11, M33 = 15;
    
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
        "   gl_Position = u_projTrans * " + Shader.POSITION_ATTRIBUTE + ";",
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

    //mistakes were made
    public static float[] copyTransform(Matrix3 matrix){
        val[M00] = matrix.val[Matrix3.M00];
        val[M11] = matrix.val[Matrix3.M11];
        val[M22] = matrix.val[Matrix3.M22];
        val[M03] = matrix.val[Matrix3.M02];
        val[M13] = matrix.val[Matrix3.M12];
        val[M33] = 1;
        return val;
    }
}
