package arc.graphics.g2d;

import arc.graphics.gl.Shader;
import arc.math.Mat;
import arc.util.*;

public class BatchShader{
    private static final float[] val = new float[16];

    private static final int M00 = 0, M01 = 4, M02 = 8, M03 = 12, M10 = 1, M11 = 5, M12 = 9, M13 = 13, M20 = 2,
        M21 = 6, M22 = 10, M23 = 14, M30 = 3, M31 = 7, M32 = 11, M33 = 15;
    
    private static final String vertexShader =
    Strings.join("\n",
        "attribute vec4 " + Shader.positionAttribute + ";",
        "attribute vec4 " + Shader.colorAttribute + ";",
        "attribute vec2 " + Shader.texcoordAttribute + "0;",
        "attribute vec4 " + Shader.mixColorAttribute + ";",
        "uniform mat4 u_projTrans;",
        "varying vec4 v_color;",
        "varying vec4 v_mix_color;",
        "varying vec2 v_texCoords;",
        "",
        "void main(){",
        "   v_color = " + Shader.colorAttribute + ";",
        "   v_color.a = v_color.a * (255.0/254.0);",
        "   v_mix_color = " + Shader.mixColorAttribute + ";",
        "   v_mix_color.a *= (255.0/254.0);",
        "   v_texCoords = " + Shader.texcoordAttribute + "0;",
        "   gl_Position = u_projTrans * " + Shader.positionAttribute + ";",
        "}"
    );
    private static final String fragmentShader =
    Strings.join("\n",
        "#ifdef GL_ES",
        "#define LOWP lowp",
        "precision mediump float;",
        "#else",
        "#define LOWP ",
        "#endif",
        "",
        "varying LOWP vec4 v_color;",
        "varying LOWP vec4 v_mix_color;",
        "varying vec2 v_texCoords;",
        "uniform sampler2D u_texture;",
        "",
        "void main(){",
        "  vec4 c = texture2D(u_texture, v_texCoords);",
        "  gl_FragColor = v_color * mix(c, vec4(v_mix_color.rgb, c.a), v_mix_color.a);",
        "}"
    );

    /** Returns a new instance of the default shader used by SpriteBatch for GL2 when no shader is specified. */
    public static Shader create(){
        return new Shader(vertexShader, fragmentShader);
    }

    //mistakes were made
    public static float[] copyTransform(Mat matrix){
        val[M01] = matrix.val[Mat.M01];
        val[M10] = matrix.val[Mat.M10];

        val[M00] = matrix.val[Mat.M00];
        val[M11] = matrix.val[Mat.M11];
        val[M22] = matrix.val[Mat.M22];
        val[M03] = matrix.val[Mat.M02];
        val[M13] = matrix.val[Mat.M12];
        val[M33] = 1;
        return val;
    }
}
