package arc.graphics.g2d;

import arc.*;
import arc.graphics.*;
import arc.graphics.Mesh.*;
import arc.graphics.VertexAttributes.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.util.*;

public class ZSpriteBatch extends Batch{
    //xyz + color + uv + mix_color
    protected static final int VERTEX_SIZE = 3 + 1 + 2 + 1;
    protected static final int SPRITE_SIZE = 4 * VERTEX_SIZE;

    protected final float[] vertices;

    //near and far planes
    public float minZ = 0, maxZ = 100;

    public ZSpriteBatch(){
        this(1000);
    }

    public ZSpriteBatch(int size){
        if(size > 8191) throw new IllegalArgumentException("Can't have more than 8191 sprites per batch: " + size);

        Shader defaultShader = new Shader(Strings.join("\n",
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
        ),
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
        //"  if(c.a < 0.01) discard;",
        "  gl_FragColor = v_color * mix(c, vec4(v_mix_color.rgb, c.a), v_mix_color.a);",
        "}"
        ));

        projectionMatrix.setOrtho(0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());

        VertexDataType vertexDataType = (Core.gl30 != null) ? VertexDataType.VertexBufferObjectWithVAO : VertexDataType.VertexArray;

        mesh = new Mesh(vertexDataType, false, size * 4, size * 6,
        new VertexAttribute(Usage.position, 3, Shader.positionAttribute),
        new VertexAttribute(Usage.colorPacked, 4, Shader.colorAttribute),
        new VertexAttribute(Usage.textureCoordinates, 2, Shader.texcoordAttribute + "0"),
        new VertexAttribute(Usage.colorPacked, 4, Shader.mixColorAttribute));

        vertices = new float[size * SPRITE_SIZE];

        int len = size * 6;
        short[] indices = new short[len];
        short j = 0;
        for(int i = 0; i < len; i += 6, j += 4){
            indices[i] = j;
            indices[i + 1] = (short)(j + 1);
            indices[i + 2] = (short)(j + 2);
            indices[i + 3] = (short)(j + 2);
            indices[i + 4] = (short)(j + 3);
            indices[i + 5] = j;
        }
        mesh.setIndices(indices);
        shader = defaultShader;
    }

    @Override
    protected void flush(){
        if(idx == 0) return;

        getShader().bind();
        setupMatrices();

        if(customShader != null && apply){
            customShader.apply();
        }

        int spritesInBatch = idx / SPRITE_SIZE;
        int count = spritesInBatch * 6;

        if(blending != Blending.disabled){
            Gl.enable(Gl.blend);
            Gl.blendFuncSeparate(blending.src, blending.dst, blending.src, blending.dst);
        }else{
            Gl.disable(Gl.blend);
        }

        Gl.enable(Gl.depthTest);

        lastTexture.bind();
        Mesh mesh = this.mesh;
        mesh.setVertices(vertices, 0, idx);
        mesh.getIndicesBuffer().position(0);
        mesh.getIndicesBuffer().limit(count);
        mesh.render(getShader(), Gl.triangles, 0, count);

        idx = 0;

        Gl.disable(Gl.depthTest);
    }

    @Override
    protected void setupMatrices(){
        combinedMatrix.set(projectionMatrix).mul(transformMatrix);
        getShader().setUniformMatrix4("u_projTrans", BatchShader.copyTransform(combinedMatrix, minZ, maxZ));
        getShader().setUniformi("u_texture", 0);
    }

    @Override
    protected void draw(Texture texture, float[] source, int offset, int count){
        if(texture != lastTexture){
            switchTexture(texture);
        }

        int max = vertices.length;
        final float z = this.z - maxZ;

        for(int i = offset; i < offset + count; i += SpriteBatch.SPRITE_SIZE){
            if(idx + SPRITE_SIZE >= max){
                flush();
            }

            for(int j = 0; j < 4; j++){
                int li = i + j * SpriteBatch.VERTEX_SIZE;
                vertices[idx] = source[li]; //x
                vertices[idx + 1] = source[li + 1]; //y
                vertices[idx + 2] = z; //z (inserted)
                for(int k = 0; k < 4; k++){
                    vertices[idx + 3 + k] = source[li + 2 + k];
                }

                idx += VERTEX_SIZE;
            }
        }
    }

    @Override
    protected void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float rotation){
        Texture texture = region.getTexture();
        if(texture != lastTexture){
            switchTexture(texture);
        }else if(idx == vertices.length){
            flush();
        }

        //bottom left and top right corner points relative to origin
        final float worldOriginX = x + originX;
        final float worldOriginY = y + originY;
        final float z = this.z - maxZ;
        float fx = -originX;
        float fy = -originY;
        float fx2 = width - originX;
        float fy2 = height - originY;

        float x1;
        float y1;
        float x2;
        float y2;
        float x3;
        float y3;
        float x4;
        float y4;

        // rotate
        final float cos = Mathf.cosDeg(rotation);
        final float sin = Mathf.sinDeg(rotation);

        x1 = cos * fx - sin * fy;
        y1 = sin * fx + cos * fy;

        x2 = cos * fx - sin * fy2;
        y2 = sin * fx + cos * fy2;

        x3 = cos * fx2 - sin * fy2;
        y3 = sin * fx2 + cos * fy2;

        x4 = x1 + (x3 - x2);
        y4 = y3 - (y2 - y1);

        x1 += worldOriginX;
        y1 += worldOriginY;
        x2 += worldOriginX;
        y2 += worldOriginY;
        x3 += worldOriginX;
        y3 += worldOriginY;
        x4 += worldOriginX;
        y4 += worldOriginY;

        final float u = region.getU();
        final float v = region.getV2();
        final float u2 = region.getU2();
        final float v2 = region.getV();

        final float color = this.colorPacked;
        final float mixColor = this.mixColorPacked;
        int idx = this.idx;
        vertices[idx] = x1;
        vertices[idx + 1] = y1;
        vertices[idx + 2] = z;
        vertices[idx + 3] = color;
        vertices[idx + 4] = u;
        vertices[idx + 5] = v;
        vertices[idx + 6] = mixColor;

        vertices[idx + 7] = x2;
        vertices[idx + 8] = y2;
        vertices[idx + 9] = z;
        vertices[idx + 10] = color;
        vertices[idx + 11] = u;
        vertices[idx + 12] = v2;
        vertices[idx + 13] = mixColor;

        vertices[idx + 14] = x3;
        vertices[idx + 15] = y3;
        vertices[idx + 16] = z;
        vertices[idx + 17] = color;
        vertices[idx + 18] = u2;
        vertices[idx + 19] = v2;
        vertices[idx + 20] = mixColor;

        vertices[idx + 21] = x4;
        vertices[idx + 22] = y4;
        vertices[idx + 23] = z;
        vertices[idx + 24] = color;
        vertices[idx + 25] = u2;
        vertices[idx + 26] = v;
        vertices[idx + 27] = mixColor;
        this.idx = idx + SPRITE_SIZE;
    }
}