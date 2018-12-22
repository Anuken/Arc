package io.anuke.arc.graphics.g2d;

import io.anuke.arc.Core;
import io.anuke.arc.collection.Array;
import io.anuke.arc.collection.Sort;
import io.anuke.arc.graphics.*;
import io.anuke.arc.graphics.Mesh.VertexDataType;
import io.anuke.arc.graphics.VertexAttributes.Usage;
import io.anuke.arc.graphics.glutils.ShaderProgram;
import io.anuke.arc.math.Mathf;
import io.anuke.arc.math.Matrix3;
import io.anuke.arc.util.Disposable;
import io.anuke.arc.util.pooling.Pool.Poolable;

/**
 * Draws batched quads using indices.
 * @author mzechner
 * @author Nathan Sweet
 */
public class SpriteBatch implements Disposable{
    static final int VERTEX_SIZE = 2 + 1 + 2;
    static final int SPRITE_SIZE = 4 * VERTEX_SIZE;

    private final Mesh mesh;
    private final float[] vertices;

    private final Matrix3 transformMatrix = new Matrix3();
    private final Matrix3 projectionMatrix = new Matrix3();
    private final Matrix3 combinedMatrix = new Matrix3();

    private final ShaderProgram shader;
    private ShaderProgram customShader = null;
    private boolean ownsShader;

    private Array<BatchRect> rects;
    private int rectAmount;
    private boolean sort;
    private Color color = new Color(1f, 1f, 1f);

    /**
     * Constructs a new SpriteBatch with a size of 1000, one buffer, and the default shader.
     * @see SpriteBatch#SpriteBatch(int, ShaderProgram)
     */
    public SpriteBatch(){
        this(1000, null);
    }

    /**
     * Constructs a SpriteBatch with one buffer and the default shader.
     * @see SpriteBatch#SpriteBatch(int, ShaderProgram)
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
     * the ones expect for shaders set with {@link #setShader(ShaderProgram)}. See {@link #createDefaultShader()}.
     * @param size The max number of sprites in a single batch. Max of 8191.
     * @param defaultShader The default shader to use. This is not owned by the SpriteBatch and must be disposed separately.
     */
    public SpriteBatch(int size, ShaderProgram defaultShader){
        // 32767 is max vertex index, so 32767 / 4 vertices per sprite = 8191 sprites max.
        if(size > 8191) throw new IllegalArgumentException("Can't have more than 8191 sprites per batch: " + size);

        VertexDataType vertexDataType = (Core.gl30 != null) ? VertexDataType.VertexBufferObjectWithVAO : VertexDataType.VertexArray;

        mesh = new Mesh(vertexDataType, false, size * 4, size * 6,
        new VertexAttribute(Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
        new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
        new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));

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
            shader = createDefaultShader();
            ownsShader = true;
        }else
            shader = defaultShader;

        rects = new Array<>(size);
        for(int i = 0; i < rects.size; i++){
            rects.set(i, new BatchRect());
        }
    }

    /** Returns a new instance of the default shader used by SpriteBatch for GL2 when no shader is specified. */
    public static ShaderProgram createDefaultShader(){
        String vertexShader =
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
        String fragmentShader = "#ifdef GL_ES\n" //
        + "#define LOWP lowp\n" //
        + "precision mediump float;\n" //
        + "#else\n" //
        + "#define LOWP \n" //
        + "#endif\n" //
        + "varying LOWP vec4 v_color;\n" //
        + "varying vec2 v_texCoords;\n" //
        + "uniform sampler2D u_texture;\n" //
        + "void main()\n"//
        + "{\n" //
        + "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" //
        + "}";

        ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
        if(!shader.isCompiled()) throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
        return shader;
    }

    public BatchRect draw(){
        if(rectAmount >= rects.size) rects.add(new BatchRect());
        BatchRect rect = rects.get(rectAmount++);
        rect.reset();
        return rect;
    }

    /** Sets whether or not to sort draw calls by their Z. Default is false. */
    public void setSort(boolean sort){
        this.sort = sort;
    }

    public void flush(){
        if(rectAmount == 0) return;

        if(customShader != null)
            customShader.begin();
        else
            shader.begin();
        setupMatrices();

        Core.gl.glEnable(GL20.GL_BLEND);

        int idx = 0;

        Texture lastTexture = null;

        //Z-sort draw calls if necessary
        if(sort){
            Sort.instance().sort(rects.items, 0, rectAmount);
        }

        Blending blending = Blending.normal;
        Core.gl.glBlendFunc(blending.src, blending.dst);

        for(int i = 0; i < rectAmount; i++){
            BatchRect rect = rects.get(i);

            if(((rect.region.texture != lastTexture) || idx >= vertices.length || blending != rect.blending) && lastTexture != null){
                render(idx, blending, lastTexture);
                idx = 0;
            }

            if(rect.vertices == null){
                //bottom left and top right corner points relative to origin
                final float worldOriginX = rect.x + rect.originX;
                final float worldOriginY = rect.y + rect.originY;
                float fx = -rect.originX;
                float fy = -rect.originY;
                float fx2 = rect.width - rect.originX;
                float fy2 = rect.height - rect.originY;

                // scale
                if(rect.scaleX != 1 || rect.scaleY != 1){
                    fx *= rect.scaleX;
                    fy *= rect.scaleY;
                    fx2 *= rect.scaleX;
                    fy2 *= rect.scaleY;
                }

                // construct corner points, start from top left and go counter clockwise
                final float p1x = fx;
                final float p1y = fy;
                final float p2x = fx;
                final float p2y = fy2;
                final float p3x = fx2;
                final float p3y = fy2;
                final float p4x = fx2;
                final float p4y = fy;

                float x1;
                float y1;
                float x2;
                float y2;
                float x3;
                float y3;
                float x4;
                float y4;

                // rotate
                if(rect.rotation != 0){
                    final float cos = Mathf.cosDeg(rect.rotation);
                    final float sin = Mathf.sinDeg(rect.rotation);

                    x1 = cos * p1x - sin * p1y;
                    y1 = sin * p1x + cos * p1y;

                    x2 = cos * p2x - sin * p2y;
                    y2 = sin * p2x + cos * p2y;

                    x3 = cos * p3x - sin * p3y;
                    y3 = sin * p3x + cos * p3y;

                    x4 = x1 + (x3 - x2);
                    y4 = y3 - (y2 - y1);
                }else{
                    x1 = p1x;
                    y1 = p1y;

                    x2 = p2x;
                    y2 = p2y;

                    x3 = p3x;
                    y3 = p3y;

                    x4 = p4x;
                    y4 = p4y;
                }

                x1 += worldOriginX;
                y1 += worldOriginY;
                x2 += worldOriginX;
                y2 += worldOriginY;
                x3 += worldOriginX;
                y3 += worldOriginY;
                x4 += worldOriginX;
                y4 += worldOriginY;

                final float u1, v1, u2, v2, u3, v3, u4, v4;
                u1 = rect.region.u2;
                v1 = rect.region.v2;
                u2 = rect.region.u;
                v2 = rect.region.v2;
                u3 = rect.region.u;
                v3 = rect.region.v;
                u4 = rect.region.u2;
                v4 = rect.region.v;

                float color = rect.color;
                vertices[idx] = x1;
                vertices[idx + 1] = y1;
                vertices[idx + 2] = color;
                vertices[idx + 3] = u1;
                vertices[idx + 4] = v1;

                vertices[idx + 5] = x2;
                vertices[idx + 6] = y2;
                vertices[idx + 7] = color;
                vertices[idx + 8] = u2;
                vertices[idx + 9] = v2;

                vertices[idx + 10] = x3;
                vertices[idx + 11] = y3;
                vertices[idx + 12] = color;
                vertices[idx + 13] = u3;
                vertices[idx + 14] = v3;

                vertices[idx + 15] = x4;
                vertices[idx + 16] = y4;
                vertices[idx + 17] = color;
                vertices[idx + 18] = u4;
                vertices[idx + 19] = v4;
                idx += 20;
                //end

                lastTexture = rect.region.texture;
                blending = rect.blending;
            }else{
                int offset = rect.voffset, count = rect.vcount;

                int verticesLength = vertices.length;
                int remainingVertices = verticesLength;
                remainingVertices -= idx;
                if(remainingVertices == 0){
                    render(idx, blending, rect.region.texture);
                    idx = 0;
                    remainingVertices = verticesLength;
                }
                int copyCount = Math.min(remainingVertices, count);

                System.arraycopy(rect.vertices, offset, vertices, idx, copyCount);
                idx += copyCount;
                count -= copyCount;
                while(count > 0){
                    offset += copyCount;
                    render(idx, blending, rect.region.texture);
                    idx = 0;
                    copyCount = Math.min(verticesLength, count);
                    System.arraycopy(rect.vertices, offset, vertices, 0, copyCount);
                    idx += copyCount;
                    count -= copyCount;
                }
            }
        }

        if(customShader != null)
            customShader.end();
        else
            shader.end();
    }

    private void render(int idx, Blending blending, Texture texture){
        int spritesInBatch = idx / 20;
        int count = spritesInBatch * 6;

        Core.gl.glBlendFunc(blending.src, blending.dst);

        texture.bind();
        Mesh mesh = this.mesh;
        mesh.setVertices(vertices, 0, idx);
        mesh.getIndicesBuffer().position(0);
        mesh.getIndicesBuffer().limit(count);
        mesh.render(customShader != null ? customShader : shader, GL20.GL_TRIANGLES, 0, count);
    }

    public Color getColor(){
        return color;
    }

    public void setColor(Color color){
        this.color.set(color);
    }

    public void setColor(float r, float g, float b, float a){
        this.color.set(r, g, b, a);
    }

    public Matrix3 getProjection(){
        return projectionMatrix;
    }

    public void setProjection(Matrix3 projection){
        projectionMatrix.set(projection);
    }

    public Matrix3 getTransform(){
        return transformMatrix;
    }

    public void setTransform(Matrix3 transform){
        transformMatrix.set(transform);
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

    public ShaderProgram getShader(){
        if(customShader == null){
            return shader;
        }
        return customShader;
    }

    public void setShader(ShaderProgram shader){
        customShader = shader;
    }

    /** @return Whether there are still pending draw requests. */
    public boolean needsFlush(){
        return rectAmount > 0;
    }

    @Override
    public void dispose(){
        mesh.dispose();
        if(ownsShader && shader != null) shader.dispose();
    }

    public class BatchRect implements Poolable, Comparable<BatchRect>{
        final TextureRegion region = new TextureRegion();
        float x, y, z, originX, originY, scaleX = 1f, scaleY = 1f, rotation, width, height;
        float color = Color.WHITE_FLOAT_BITS;
        float[] vertices;
        int voffset, vcount;
        Blending blending = Blending.normal;

        public BatchRect pos(float x, float y){
            this.x = x;
            this.y = y;
            return this;
        }

        public BatchRect color(Color color){
            this.color = Color.toFloatBits(color.r * SpriteBatch.this.color.r, color.g * SpriteBatch.this.color.g,
            color.b * SpriteBatch.this.color.b, color.a * SpriteBatch.this.color.a);
            return this;
        }

        public BatchRect set(float x, float y, float w, float h){
            this.width = w;
            this.height = h;
            this.x = x;
            this.y = y;
            return this;
        }

        public BatchRect size(float w, float h){
            this.width = w;
            this.height = h;
            return this;
        }

        public BatchRect origin(float x, float y){
            this.originX = x;
            this.originY = y;
            return this;
        }

        public BatchRect scl(float x, float y){
            this.scaleX = x;
            this.scaleY = y;
            return this;
        }

        public BatchRect rot(float rot){
            this.rotation = rot;
            return this;
        }

        public BatchRect tex(String name){
            this.region.set(Core.atlas.find(name));
            return this;
        }

        public BatchRect tex(Texture tex){
            this.region.set(tex);
            return this;
        }

        public BatchRect tex(TextureRegion region){
            this.region.set(region);
            return this;
        }

        public BatchRect uv(float u, float v, float u2, float v2){
            region.set(u, v, u2, v2);
            return this;
        }

        public BatchRect z(float z){
            this.z = z;
            return this;
        }

        public BatchRect blend(Blending blending){
            this.blending = blending;
            return this;
        }

        public BatchRect vert(Texture texture, float[] vertices, int offset, int count){
            this.vertices = vertices;
            this.voffset = offset;
            this.vcount = count;
            return tex(texture);
        }

        @Override
        public int compareTo(BatchRect other){
            return Float.compare(z, other.z);
        }

        @Override
        public void reset(){
            region.texture = null;
            region.u = region.v = region.u2 = region.v2 = region.regionWidth = region.regionHeight = 0;
            x = y = z = originX = originY = width = height = rotation = 0f;
            scaleX = scaleY = 1f;
            color = Color.WHITE_FLOAT_BITS;
            vertices = null;
        }
    }
}
