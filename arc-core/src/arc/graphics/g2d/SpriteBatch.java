package arc.graphics.g2d;

import arc.Core;
import arc.graphics.*;
import arc.graphics.Mesh.VertexDataType;
import arc.graphics.VertexAttributes.Usage;
import arc.graphics.gl.Shader;
import arc.math.Mathf;
import arc.math.Matrix3;
import arc.util.Disposable;

/**
 * Draws batched quads using indices.
 * @author mzechner
 * @author Nathan Sweet
 */
public class SpriteBatch implements Disposable{
    //xy + color + uv + mix_color
    static final int VERTEX_SIZE = 2 + 1 + 2 + 1;
    static final int SPRITE_SIZE = 4 * VERTEX_SIZE;

    protected Mesh mesh;

    protected final float[] vertices;
    protected int idx = 0;
    protected Texture lastTexture = null;
    protected float invTexWidth = 0, invTexHeight = 0;

    protected boolean apply;

    protected final Matrix3 transformMatrix = new Matrix3();
    protected final Matrix3 projectionMatrix = new Matrix3();
    protected final Matrix3 combinedMatrix = new Matrix3();

    protected Blending blending = Blending.normal;

    protected final Shader shader;
    protected Shader customShader = null;
    private boolean ownsShader;

    protected final Color color = new Color(1, 1, 1, 1);
    protected float colorPacked = Color.whiteFloatBits;

    protected final Color mixColor = Color.clear;
    protected float mixColorPacked = Color.clearFloatBits;

    /** Number of render calls. **/
    int renderCalls = 0;
    /** Number of rendering calls, ever. Will not be reset unless set manually. **/
    int totalRenderCalls = 0;
    /** The maximum number of sprites rendered in one batch so far. **/
    int maxSpritesInBatch = 0;

    /**
     * Constructs a new SpriteBatch with a size of 4096, one buffer, and the default shader.
     * @see SpriteBatch#SpriteBatch(int, Shader)
     */
    public SpriteBatch(){
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
     * the ones expect for shaders set with {@link #setShader(Shader)}.
     * @param size The max number of sprites in a single batch. Max of 8191.
     * @param defaultShader The default shader to use. This is not owned by the SpriteBatch and must be disposed separately.
     */
    public SpriteBatch(int size, Shader defaultShader){
        // 32767 is max vertex index, so 32767 / 4 vertices per sprite = 8191 sprites max.
        if(size > 8191) throw new IllegalArgumentException("Can't have more than 8191 sprites per batch: " + size);

        projectionMatrix.setOrtho(0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());

        if(size > 0){
            VertexDataType vertexDataType = (Core.gl30 != null) ? VertexDataType.VertexBufferObjectWithVAO : VertexDataType.VertexArray;

            mesh = new Mesh(vertexDataType, false, size * 4, size * 6,
            new VertexAttribute(Usage.Position, 2, Shader.POSITION_ATTRIBUTE),
            new VertexAttribute(Usage.ColorPacked, 4, Shader.COLOR_ATTRIBUTE),
            new VertexAttribute(Usage.TextureCoordinates, 2, Shader.TEXCOORD_ATTRIBUTE + "0"),
            new VertexAttribute(Usage.ColorPacked, 4, Shader.MIX_COLOR_ATTRIBUTE));

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

    protected SpriteBatch(Object empty){
        vertices = null;
        mesh = null;
        shader = null;
    }

    void setColor(Color tint){
        color.set(tint);
        colorPacked = tint.toFloatBits();
    }

    void setColor(float r, float g, float b, float a){
        color.set(r, g, b, a);
        colorPacked = color.toFloatBits();
    }

    protected Color getColor(){
        return color;
    }

    void setPackedColor(float packedColor){
        Color.abgr8888ToColor(color, packedColor);
        this.colorPacked = packedColor;
    }

    protected float getPackedColor(){
        return colorPacked;
    }

    void setMixColor(Color tint){
        mixColor.set(tint);
        mixColorPacked = tint.toFloatBits();
    }

    void setMixColor(float r, float g, float b, float a){
        mixColor.set(r, g, b, a);
        mixColorPacked = mixColor.toFloatBits();
    }

    protected Color getMixColor(){
        return mixColor;
    }

    void setPackedMixColor(float packedColor){
        Color.abgr8888ToColor(mixColor, packedColor);
        this.mixColorPacked = packedColor;
    }

    protected float getPackedMixColor(){
        return mixColorPacked;
    }

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

    protected void draw(TextureRegion region, float x, float y){
        draw(region, x, y, region.getWidth(), region.getHeight());
    }

    protected void draw(TextureRegion region, float x, float y, float width, float height){
        draw(region, x, y, 0, 0, width, height, 0);
    }

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
            int idx = this.idx;
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
            this.idx = idx + 24;

        }else{
            final float fx2 = x + width;
            final float fy2 = y + height;
            final float u = region.u;
            final float v = region.v2;
            final float u2 = region.u2;
            final float v2 = region.v;

            final float color = this.colorPacked;
            final float mixColor = this.mixColorPacked;
            int idx = this.idx;
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
            this.idx = idx + 24;
        }
    }

    protected void flush(){
        if(idx == 0) return;

        renderCalls = 0;

        getShader().begin();
        setupMatrices();

        if(customShader != null && apply){
            customShader.apply();
        }

        renderCalls++;
        totalRenderCalls++;
        int spritesInBatch = idx / 24;
        if(spritesInBatch > maxSpritesInBatch) maxSpritesInBatch = spritesInBatch;
        int count = spritesInBatch * 6;

        if(blending != Blending.disabled){
            Core.gl.glEnable(GL20.GL_BLEND);
            Core.gl.glBlendFuncSeparate(blending.src, blending.dst, blending.src, blending.dst);
        }else{
            Core.gl.glDisable(GL20.GL_BLEND);
        }

        lastTexture.bind();
        Mesh mesh = this.mesh;
        mesh.setVertices(vertices, 0, idx);
        mesh.getIndicesBuffer().position(0);
        mesh.getIndicesBuffer().limit(count);
        mesh.render(getShader(), GL20.GL_TRIANGLES, 0, count);

        idx = 0;

        getShader().end();
    }

    void setBlending(Blending blending){
        flush();
        this.blending = blending;
    }

    @Override
    public void dispose(){
        if(mesh != null){
            mesh.dispose();
        }
        if(ownsShader && shader != null) shader.dispose();
    }

    Matrix3 getProjection(){
        return projectionMatrix;
    }

    Matrix3 getTransform(){
        return transformMatrix;
    }

    void setProjection(Matrix3 projection){
        flush();
        projectionMatrix.set(projection);
    }

    void setTransform(Matrix3 transform){
        flush();
        transformMatrix.set(transform);
    }

    private void setupMatrices(){
        combinedMatrix.set(projectionMatrix).mul(transformMatrix);
        getShader().setUniformMatrix4("u_projTrans", BatchShader.copyTransform(combinedMatrix));
        getShader().setUniformi("u_texture", 0);
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
        flush();
        customShader = shader;
        this.apply = apply;
    }

    Shader getShader(){
        if(customShader == null){
            return shader;
        }
        return customShader;
    }
}