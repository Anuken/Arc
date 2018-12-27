package io.anuke.arc.graphics.g2d;

import io.anuke.arc.Core;
import io.anuke.arc.graphics.*;
import io.anuke.arc.graphics.Mesh.VertexDataType;
import io.anuke.arc.graphics.VertexAttributes.Usage;
import io.anuke.arc.graphics.glutils.Shader;
import io.anuke.arc.math.Affine2;
import io.anuke.arc.math.Mathf;
import io.anuke.arc.math.Matrix3;
import io.anuke.arc.util.Disposable;

/**
 * Draws batched quads using indices.
 * @author mzechner
 * @author Nathan Sweet
 * @see Batch
 */
public class SpriteBatch implements Disposable{
    static final int VERTEX_SIZE = 2 + 1 + 2;
    static final int SPRITE_SIZE = 4 * VERTEX_SIZE;

    private Mesh mesh;

    final float[] vertices;
    int idx = 0;
    Texture lastTexture = null;
    float invTexWidth = 0, invTexHeight = 0;

    boolean drawing = false;

    private final Matrix3 transformMatrix = new Matrix3();
    private final Matrix3 projectionMatrix = new Matrix3();
    private final Matrix3 combinedMatrix = new Matrix3();

    private Blending blending = Blending.normal;

    private final Shader shader;
    private Shader customShader = null;
    private boolean ownsShader;

    private final Color color = new Color(1, 1, 1, 1);
    float colorPacked = Color.WHITE_FLOAT_BITS;

    /** Number of render calls since the last {@link #begin()}. **/
    int renderCalls = 0;
    /** Number of rendering calls, ever. Will not be reset unless set manually. **/
    int totalRenderCalls = 0;
    /** The maximum number of sprites rendered in one batch so far. **/
    int maxSpritesInBatch = 0;

    /**
     * Constructs a new SpriteBatch with a size of 4096, one buffer, and the default shader.
     * @see SpriteBatch#SpriteBatch(int, Shader)
     */
    SpriteBatch(){
        this(4096, null);
    }

    /**
     * Constructs a SpriteBatch with one buffer and the default shader.
     * @see SpriteBatch#SpriteBatch(int, Shader)
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
     * the ones expect for shaders set with {@link #setShader(Shader)}. See {@link #createDefaultShader()}.
     * @param size The max number of sprites in a single batch. Max of 8191.
     * @param defaultShader The default shader to use. This is not owned by the SpriteBatch and must be disposed separately.
     */
    public SpriteBatch(int size, Shader defaultShader){
        // 32767 is max vertex index, so 32767 / 4 vertices per sprite = 8191 sprites max.
        if(size > 8191) throw new IllegalArgumentException("Can't have more than 8191 sprites per batch: " + size);

        VertexDataType vertexDataType = (Core.gl30 != null) ? VertexDataType.VertexBufferObjectWithVAO : VertexDataType.VertexArray;

        mesh = new Mesh(vertexDataType, false, size * 4, size * 6,
        new VertexAttribute(Usage.Position, 2, Shader.POSITION_ATTRIBUTE),
        new VertexAttribute(Usage.ColorPacked, 4, Shader.COLOR_ATTRIBUTE),
        new VertexAttribute(Usage.TextureCoordinates, 2, Shader.TEXCOORD_ATTRIBUTE + "0"));

        projectionMatrix.setOrtho(0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());

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
    }

    void begin(){
        if(drawing) throw new IllegalStateException("SpriteBatch.end must be called before begin.");
        renderCalls = 0;

        Core.gl.glDepthMask(false);
        getShader().begin();
        setupMatrices();

        drawing = true;
    }

    void end(){
        if(!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before end.");
        if(idx > 0) flush();
        lastTexture = null;
        drawing = false;

        Core.gl.glDepthMask(true);

        getShader().end();
    }

    void setColor(Color tint){
        color.set(tint);
        colorPacked = tint.toFloatBits();
    }

    void setColor(float r, float g, float b, float a){
        color.set(r, g, b, a);
        colorPacked = color.toFloatBits();
    }

    Color getColor(){
        return color;
    }

    void setPackedColor(float packedColor){
        Color.abgr8888ToColor(color, packedColor);
        this.colorPacked = packedColor;
    }

    float getPackedColor(){
        return colorPacked;
    }

    void draw(Texture texture, float[] spriteVertices, int offset, int count){
        if(!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

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

    void draw(TextureRegion region, float x, float y){
        draw(region, x, y, region.getWidth(), region.getHeight());
    }

    void draw(TextureRegion region, float x, float y, float width, float height){
        draw(region, x, y, 0, 0, width, height, 0);
    }

    void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float rotation){
        if(!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

        float[] vertices = this.vertices;

        Texture texture = region.texture;
        if(texture != lastTexture){
            switchTexture(texture);
        }else if(idx == vertices.length) //
            flush();

        if(!Mathf.isZero(rotation)){
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

            float color = this.colorPacked;
            int idx = this.idx;
            vertices[idx] = x1;
            vertices[idx + 1] = y1;
            vertices[idx + 2] = color;
            vertices[idx + 3] = u;
            vertices[idx + 4] = v;

            vertices[idx + 5] = x2;
            vertices[idx + 6] = y2;
            vertices[idx + 7] = color;
            vertices[idx + 8] = u;
            vertices[idx + 9] = v2;

            vertices[idx + 10] = x3;
            vertices[idx + 11] = y3;
            vertices[idx + 12] = color;
            vertices[idx + 13] = u2;
            vertices[idx + 14] = v2;

            vertices[idx + 15] = x4;
            vertices[idx + 16] = y4;
            vertices[idx + 17] = color;
            vertices[idx + 18] = u2;
            vertices[idx + 19] = v;
            this.idx = idx + 20;

        }else{
            final float fx2 = x + width;
            final float fy2 = y + height;
            final float u = region.u;
            final float v = region.v2;
            final float u2 = region.u2;
            final float v2 = region.v;

            float color = this.colorPacked;
            int idx = this.idx;
            vertices[idx] = x;
            vertices[idx + 1] = y;
            vertices[idx + 2] = color;
            vertices[idx + 3] = u;
            vertices[idx + 4] = v;

            vertices[idx + 5] = x;
            vertices[idx + 6] = fy2;
            vertices[idx + 7] = color;
            vertices[idx + 8] = u;
            vertices[idx + 9] = v2;

            vertices[idx + 10] = fx2;
            vertices[idx + 11] = fy2;
            vertices[idx + 12] = color;
            vertices[idx + 13] = u2;
            vertices[idx + 14] = v2;

            vertices[idx + 15] = fx2;
            vertices[idx + 16] = y;
            vertices[idx + 17] = color;
            vertices[idx + 18] = u2;
            vertices[idx + 19] = v;
            this.idx = idx + 20;
        }
    }

    void draw(TextureRegion region, float width, float height, Affine2 transform){
        if(!drawing) throw new IllegalStateException("SpriteBatch.begin must be called before draw.");

        float[] vertices = this.vertices;

        Texture texture = region.texture;
        if(texture != lastTexture){
            switchTexture(texture);
        }else if(idx == vertices.length){
            flush();
        }

        // construct corner points
        float x1 = transform.m02;
        float y1 = transform.m12;
        float x2 = transform.m01 * height + transform.m02;
        float y2 = transform.m11 * height + transform.m12;
        float x3 = transform.m00 * width + transform.m01 * height + transform.m02;
        float y3 = transform.m10 * width + transform.m11 * height + transform.m12;
        float x4 = transform.m00 * width + transform.m02;
        float y4 = transform.m10 * width + transform.m12;

        float u = region.u;
        float v = region.v2;
        float u2 = region.u2;
        float v2 = region.v;

        float color = this.colorPacked;
        int idx = this.idx;
        vertices[idx] = x1;
        vertices[idx + 1] = y1;
        vertices[idx + 2] = color;
        vertices[idx + 3] = u;
        vertices[idx + 4] = v;

        vertices[idx + 5] = x2;
        vertices[idx + 6] = y2;
        vertices[idx + 7] = color;
        vertices[idx + 8] = u;
        vertices[idx + 9] = v2;

        vertices[idx + 10] = x3;
        vertices[idx + 11] = y3;
        vertices[idx + 12] = color;
        vertices[idx + 13] = u2;
        vertices[idx + 14] = v2;

        vertices[idx + 15] = x4;
        vertices[idx + 16] = y4;
        vertices[idx + 17] = color;
        vertices[idx + 18] = u2;
        vertices[idx + 19] = v;
        this.idx = idx + 20;
    }

    void flush(){
        if(idx == 0) return;

        renderCalls++;
        totalRenderCalls++;
        int spritesInBatch = idx / 20;
        if(spritesInBatch > maxSpritesInBatch) maxSpritesInBatch = spritesInBatch;
        int count = spritesInBatch * 6;

        lastTexture.bind();
        Mesh mesh = this.mesh;
        mesh.setVertices(vertices, 0, idx);
        mesh.getIndicesBuffer().position(0);
        mesh.getIndicesBuffer().limit(count);

        Core.gl.glEnable(GL20.GL_BLEND);
        if(blending != Blending.normal)
            Core.gl.glBlendFuncSeparate(blending.src, blending.dst, blending.src, blending.dst);

        mesh.render(getShader(), GL20.GL_TRIANGLES, 0, count);

        idx = 0;
    }

    void setBlending(Blending blending){
        flush();
        this.blending = blending;
    }

    @Override
    public void dispose(){
        mesh.dispose();
        if(ownsShader && shader != null) shader.dispose();
    }

    Matrix3 getProjection(){
        return projectionMatrix;
    }

    Matrix3 getTransform(){
        return transformMatrix;
    }

    void setProjection(Matrix3 projection){
        if(drawing) flush();
        projectionMatrix.set(projection);
        if(drawing) setupMatrices();
    }

    void setTransform(Matrix3 transform){
        if(drawing) flush();
        transformMatrix.set(transform);
        if(drawing) setupMatrices();
    }

    private void setupMatrices(){
        combinedMatrix.set(projectionMatrix).mul(transformMatrix);
        if(customShader != null){
            customShader.setUniformMatrix("u_projTrans", combinedMatrix);
            customShader.setUniformi("u_texture", 0);
        }else{
            shader.setUniformMatrix("u_projTrans", combinedMatrix);
            shader.setUniformi("u_texture", 0);
        }
    }

    protected void switchTexture(Texture texture){
        flush();
        lastTexture = texture;
        invTexWidth = 1.0f / texture.getWidth();
        invTexHeight = 1.0f / texture.getHeight();
    }

    void setShader(Shader shader){
        setShader(shader, true);
    }

    void setShader(Shader shader, boolean apply){
        if(drawing){
            flush();
            if(customShader != null)
                customShader.end();
            else
                this.shader.end();
        }
        customShader = shader;
        if(drawing){
            if(customShader != null)
                customShader.begin();
            else
                this.shader.begin();
            setupMatrices();

            if(shader != null && apply){
                shader.apply();
            }
        }
    }

    Shader getShader(){
        if(customShader == null){
            return shader;
        }
        return customShader;
    }

    boolean isDrawing(){
        return drawing;
    }
}