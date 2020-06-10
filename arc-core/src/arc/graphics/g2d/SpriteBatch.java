package arc.graphics.g2d;

import arc.*;
import arc.graphics.*;
import arc.graphics.Mesh.*;
import arc.graphics.VertexAttributes.*;
import arc.graphics.gl.*;
import arc.math.*;

public class SpriteBatch extends Batch{
    //xy + color + uv + mix_color
    public static final int VERTEX_SIZE = 2 + 1 + 2 + 1;
    public static final int SPRITE_SIZE = 4 * VERTEX_SIZE;

    protected final float[] vertices;

    /** Number of render calls. **/
    int renderCalls = 0;
    /** Number of rendering calls, ever. Will not be reset unless set manually. **/
    int totalRenderCalls = 0;
    /** The maximum number of sprites rendered in one batch so far. **/
    int maxSpritesInBatch = 0;

    /**
     * Constructs a new SpriteBatch with a size of 4096, one buffer, and the default shader.
     * @see #SpriteBatch(int, Shader)
     */
    public SpriteBatch(){
        this(4096, null);
    }

    /**
     * Constructs a SpriteBatch with one buffer and the default shader.
     * @see #SpriteBatch(int, Shader)
     */
    public SpriteBatch(int size){
        this(size, null);
    }

    /**
     * Constructs a new SpriteBatch. Sets the projection matrix to an orthographic projection with y-axis point upwards, x-axis
     * point to the right and the origin being in the bottom left corner of the screen. The projection will be pixel perfect with
     * respect to the current screen resolution.
     * <p>
     * The defaultShader specifies the shader to use. Note that the names for uniforms for this default shader are different than
     * the ones expect for shaders set with {@link #setShader(Shader)}.
     * @param size The max number of sprites in a single batch. Max of 8191.
     * @param defaultShader The default shader to use. This is not owned by the SpriteBatch and must be disposed separately.
     */
    public SpriteBatch(int size, Shader defaultShader){
        // 32767 is max vertex index, so 32767 / 4 vertices per sprite = 8191 sprites max.
        if(size > 8191) throw new IllegalArgumentException("Can't have more than 8191 sprites per batch: " + size);

        projectionMatrix.setOrtho(0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());

        if(size > 0){
            VertexDataType vertexDataType = Core.gl30 != null ? VertexDataType.VertexBufferObjectWithVAO : VertexDataType.VertexArray;

            mesh = new Mesh(vertexDataType, false, size * 4, size * 6,
            new VertexAttribute(Usage.position, 2, Shader.positionAttribute),
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

            if(defaultShader == null){
                shader = BatchShader.create();
                ownsShader = true;
            }else{
                shader = defaultShader;
            }
        }else{
            vertices = new float[0];
            shader = null;
        }
    }

    @Override
    protected void flush(){
        if(idx == 0) return;

        renderCalls = 0;

        getShader().bind();
        setupMatrices();

        if(customShader != null && apply){
            customShader.apply();
        }

        Gl.depthMask(false);
        renderCalls++;
        totalRenderCalls++;
        int spritesInBatch = idx / SPRITE_SIZE;
        if(spritesInBatch > maxSpritesInBatch) maxSpritesInBatch = spritesInBatch;
        int count = spritesInBatch * 6;

        if(blending != Blending.disabled){
            Gl.enable(Gl.blend);
            Gl.blendFuncSeparate(blending.src, blending.dst, blending.src, blending.dst);
        }else{
            Gl.disable(Gl.blend);
        }

        lastTexture.bind();
        Mesh mesh = this.mesh;
        mesh.setVertices(vertices, 0, idx);
        mesh.getIndicesBuffer().position(0);
        mesh.getIndicesBuffer().limit(count);
        mesh.render(getShader(), Gl.triangles, 0, count);

        idx = 0;
    }

    @Override
    protected void draw(Texture texture, float[] spriteVertices, int offset, int count){

        int verticesLength = vertices.length;
        int remainingVertices = verticesLength;
        if(texture != lastTexture){
            switchTexture(texture);
        }else{
            remainingVertices -= idx;
            if(remainingVertices == 0){
                flush();
                remainingVertices = verticesLength;
            }
        }
        int copyCount = Math.min(remainingVertices, count);

        System.arraycopy(spriteVertices, offset, vertices, idx, copyCount);
        idx += copyCount;
        count -= copyCount;
        while(count > 0){
            offset += copyCount;
            flush();
            copyCount = Math.min(verticesLength, count);
            System.arraycopy(spriteVertices, offset, vertices, 0, copyCount);
            idx += copyCount;
            count -= copyCount;
        }
    }

    @Override
    protected void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float rotation){

        Texture texture = region.texture;
        if(texture != lastTexture){
            switchTexture(texture);
        }else if(idx == vertices.length){
            flush();
        }

        if(!Mathf.zero(rotation)){
            //bottom left and top right corner points relative to origin
            final float worldOriginX = x + originX;
            final float worldOriginY = y + originY;
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

            final float u = region.u;
            final float v = region.v2;
            final float u2 = region.u2;
            final float v2 = region.v;

            final float color = this.colorPacked;
            final float mixColor = this.mixColorPacked;
            vertices[idx] = x1;
            vertices[idx + 1] = y1;
            vertices[idx + 2] = color;
            vertices[idx + 3] = u;
            vertices[idx + 4] = v;
            vertices[idx + 5] = mixColor;

            vertices[idx + 6] = x2;
            vertices[idx + 7] = y2;
            vertices[idx + 8] = color;
            vertices[idx + 9] = u;
            vertices[idx + 10] = v2;
            vertices[idx + 11] = mixColor;

            vertices[idx + 12] = x3;
            vertices[idx + 13] = y3;
            vertices[idx + 14] = color;
            vertices[idx + 15] = u2;
            vertices[idx + 16] = v2;
            vertices[idx + 17] = mixColor;

            vertices[idx + 18] = x4;
            vertices[idx + 19] = y4;
            vertices[idx + 20] = color;
            vertices[idx + 21] = u2;
            vertices[idx + 22] = v;
            vertices[idx + 23] = mixColor;

            idx += SPRITE_SIZE;
        }else{
            final float fx2 = x + width;
            final float fy2 = y + height;
            final float u = region.u;
            final float v = region.v2;
            final float u2 = region.u2;
            final float v2 = region.v;

            final float color = this.colorPacked;
            final float mixColor = this.mixColorPacked;
            vertices[idx] = x;
            vertices[idx + 1] = y;
            vertices[idx + 2] = color;
            vertices[idx + 3] = u;
            vertices[idx + 4] = v;
            vertices[idx + 5] = mixColor;

            vertices[idx + 6] = x;
            vertices[idx + 7] = fy2;
            vertices[idx + 8] = color;
            vertices[idx + 9] = u;
            vertices[idx + 10] = v2;
            vertices[idx + 11] = mixColor;

            vertices[idx + 12] = fx2;
            vertices[idx + 13] = fy2;
            vertices[idx + 14] = color;
            vertices[idx + 15] = u2;
            vertices[idx + 16] = v2;
            vertices[idx + 17] = mixColor;

            vertices[idx + 18] = fx2;
            vertices[idx + 19] = y;
            vertices[idx + 20] = color;
            vertices[idx + 21] = u2;
            vertices[idx + 22] = v;
            vertices[idx + 23] = mixColor;

            idx += SPRITE_SIZE;
        }
    }
}
