package arc.graphics.g2d;

import arc.*;
import arc.graphics.*;
import arc.graphics.Mesh.*;
import arc.graphics.Pixmap.*;
import arc.graphics.VertexAttributes.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.util.*;

/**
 * Draws batched quads using indices.
 * <p>
 * <b>Experimental: This Batch requires GLES3.0! Enable this by setting useGL30=true in your application configuration(s). GL30 is
 * supported by most Android devices (starting with Android 4.3 Jelly Bean, 2012) and PCs with appropriate OpenGL support.</b>
 * <p>
 * This is an optimized version of the {@link SpriteBatch} that maintains an texture-cache inside a GL_TEXTURE_2D_ARRAY to combine
 * draw calls with different textures effectively. This will avoid costly (especially on mobile) batch flushes that would usually
 * occur when your render with more then one texture.
 * <p>
 * Use this Batch if you frequently utilize more than a single texture between calling {@link#begin()} and {@link#end()}. An
 * example would be if your Atlas is spread over multiple Textures or if you draw with individual Textures.
 * <p>
 * Using this Batch to render to a Frame Buffer Object (FBO) is not allowed on WebGL because of current WebGL and LibCore.API
 * limitations. Other platforms may use this Batch to render to a FBO as the state is saved and restored recursively.
 * @author mzechner (Original SpriteBatch)
 * @author Nathan Sweet (Original SpriteBatch)
 * @author VaTTeRGeR (ArrayTextureSpriteBatch)
 * @see Batch
 * @see SpriteBatch
 */

public class ArrayTextureSpriteBatch extends Batch{
    private final float[] vertices;

    private final int spriteVertexSize = SpriteBatch.VERTEX_SIZE;
    private final int spriteFloatSize = SpriteBatch.SPRITE_SIZE;

    /** The maximum number of available texture slots for the fragment shader */
    private final int maxTextureSlots;

    /** Textures in use (index: Texture Slot, value: Texture) */
    private final Texture[] usedTextures;

    /** This slot gets replaced once texture cache space runs out. */
    private int usedTexturesNextSwapSlot;

    private final FrameBuffer copyFramebuffer;

    private int arrayTextureHandle;
    private int arrayTextureMagFilter;
    private int arrayTextureMinFilter;

    private int maxTextureWidth, maxTextureHeight;

    private float invMaxTextureWidth, invMaxTextureHeight;
    private float subImageScaleWidth, subImageScaleHeight;

    private boolean useMipMaps = true;
    private boolean mipMapsDirty = true;
    private ApplicationListener listener;

    /** Number of rendering calls, ever. Will not be reset unless set manually. **/
    public int totalRenderCalls = 0;

    /** The maximum number of sprites rendered in one batch so far. **/
    public int maxSpritesInBatch = 0;

    /** The current number of textures in the LFU cache. **/
    public int currentTextureLFUSize = 0;

    /** The current number of texture swaps in the LFU cache. Gets reset when calling {@link#begin()} **/
    public int currentTextureLFUSwaps = 0;

    public ArrayTextureSpriteBatch(){
        this(16);
    }

    public ArrayTextureSpriteBatch(int maxTextures){
        this(4096, 2048, 2048, maxTextures, Gl.linear, Gl.linear, null);
    }

    /**
     * Constructs a new ArrayTextureSpriteBatch with the default shader, texture cache size and texture filters.
     * @see ArrayTextureSpriteBatch#ArrayTextureSpriteBatch(int, int, int, int, int, int, Shader)
     */
    public ArrayTextureSpriteBatch(int maxSprites, int maxTextureWidth, int maxTextureHeight, int maxConcurrentTextures, int texFilterMag, int texFilterMin){
        this(maxSprites, maxTextureWidth, maxTextureHeight, maxConcurrentTextures, texFilterMag, texFilterMin, null);
    }

    /**
     * Constructs a new ArrayTextureSpriteBatch. Sets the projection matrix to an orthographic projection with y-axis point
     * upwards, x-axis point to the right and the origin being in the bottom left corner of the screen. The projection will be
     * pixel perfect with respect to the current screen resolution.
     * <p>
     * The defaultShader specifies the shader to use. Note that the names for uniforms for this default shader are different than
     * the ones expected for shaders set with {@link #setShader(Shader)}.
     * <p>
     * <b>Remember: VRAM usage will be roughly maxTextureWidth * maxTextureHeight * maxConcurrentTextures * 4 byte plus some
     * overhead!</b>
     * @param maxSprites The maximum number of sprites in a single batched draw call. Upper limit of 8191.
     * @param maxTextureWidth Set as wide as your widest texture.
     * @param maxTextureHeight Set as tall as your tallest texture.
     * @param maxConcurrentTextures Set to the maximum number of textures you want to use ideally, grossly oversized values waste
     * VRAM.
     * @param texFilterMag The OpenGL texture magnification filter. See {@link #setArrayTextureFilter(int, int)}.
     * @param texFilterMin The OpenGL texture minification filter. See {@link #setArrayTextureFilter(int, int)}.
     * @param defaultShader The default shader to use. This is not owned by the ArrayTextureSpriteBatch and must be disposed
     * separately. Remember to incorporate the fragment-/vertex-shader changes required for the use of an array texture
     * as demonstrated by the default shader.
     * @throws IllegalStateException Thrown if the device does not support GLES 3.0 and by extension: GL_TEXTURE_2D_ARRAY and
     * Framebuffer Objects. Make sure to implement a Fallback to {@link SpriteBatch} in case Texture Arrays are not
     * supported on a device.
     */
    public ArrayTextureSpriteBatch(int maxSprites, int maxTextureWidth, int maxTextureHeight, int maxConcurrentTextures, int texFilterMag, int texFilterMin, Shader defaultShader){
        if(Core.gl30 == null){
            throw new IllegalStateException("GL30 is not available. Remember to set \"useGL30 = true\" in your application config.");
        }

        // 32767 is max vertex index, so 32767 / 4 vertices per sprite = 8191 sprites max.
        if(maxSprites > 8191){
            throw new IllegalArgumentException("Can't have more than 8191 sprites per batch: " + maxSprites);
        }

        if(maxConcurrentTextures < 1 || maxConcurrentTextures > 256){
            throw new IllegalArgumentException("maxConcurrentTextures out of range [1,256]: " + maxConcurrentTextures);
        }

        if(maxTextureWidth < 1 || maxTextureHeight < 1){
            throw new IllegalArgumentException("Maximum Texture width / height must both be greater than zero: " + maxTextureWidth + " / " + maxTextureHeight);
        }

        maxTextureSlots = maxConcurrentTextures;

        this.maxTextureWidth = maxTextureWidth;
        this.maxTextureHeight = maxTextureHeight;

        invMaxTextureWidth = 1f / maxTextureWidth;
        invMaxTextureHeight = 1f / maxTextureHeight;

        if(defaultShader == null){
            shader = createDefaultShader();
            ownsShader = true;
        }else{
            shader = defaultShader;
            ownsShader = false;
        }

        usedTextures = new Texture[maxTextureSlots];

        arrayTextureMagFilter = texFilterMag;
        arrayTextureMinFilter = texFilterMin;

        initializeArrayTexture();

        copyFramebuffer = new FrameBuffer(Format.RGBA8888, maxTextureWidth, maxTextureHeight);

        // The vertex data is extended with one float for the texture index.
        mesh = new Mesh(VertexDataType.VertexBufferObjectWithVAO, false, maxSprites * 4, maxSprites * 6,
        new VertexAttribute(Usage.position, 2, Shader.positionAttribute),
        new VertexAttribute(Usage.colorPacked, 4, Shader.colorAttribute),
        new VertexAttribute(Usage.textureCoordinates, 2, Shader.texcoordAttribute + "0"),
        new VertexAttribute(Usage.colorPacked, 4, Shader.mixColorAttribute),
        new VertexAttribute(Usage.generic, 1, "texture_index"));

        projectionMatrix.setOrtho(0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());

        vertices = new float[maxSprites * (spriteFloatSize + 4)];

        int len = maxSprites * 6;
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

        if(Core.app.isAndroid()){
            Core.app.addListener(listener = new ApplicationListener(){
                @Override
                public void resume(){
                    initializeArrayTexture();
                }

                @Override
                public void pause(){
                    disposeArrayTexture();
                }
            });
        }

    }

    private void initializeArrayTexture(){
        // This forces a re-population of the Array Texture
        currentTextureLFUSize = 0;

        arrayTextureHandle = Core.gl30.glGenTexture();

        Core.gl30.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, arrayTextureHandle);

        Core.gl30.glTexImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, GL30.GL_RGBA, maxTextureWidth, maxTextureHeight, maxTextureSlots, 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, null);

        setArrayTextureFilter(arrayTextureMagFilter, arrayTextureMinFilter);

        Core.gl30.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL30.GL_TEXTURE_WRAP_S, GL30.GL_REPEAT);
        Core.gl30.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL30.GL_TEXTURE_WRAP_T, GL30.GL_REPEAT);
        Core.gl30.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, GL30.GL_NONE);
    }

    private void disposeArrayTexture(){
        Core.gl30.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, GL30.GL_NONE);
        Core.gl30.glDeleteTexture(arrayTextureHandle);
    }

    @Override
    public void dispose(){
        if(listener != null) Core.app.removeListener(listener);

        disposeArrayTexture();

        copyFramebuffer.dispose();

        mesh.dispose();

        if(ownsShader && shader != null){
            shader.dispose();
        }
    }

    /**
     * Sets the OpenGL texture filtering modes. MipMaps will be generated on the GPU, this takes additional time when first
     * loading or swapping textures. Dimension the {@link ArrayTextureSpriteBatch} accordingly to avoid stuttering.
     * <p>
     * <b>Default magnification: GL30.GL_NEAREST -> Pixel perfect when going to close<br>
     * Default minification: GL30.GL_LINEAR_MIPMAP_LINEAR -> Smooth when zooming out.</b>
     * @param glTextureMagFilter The filtering mode used when zooming into the texture.
     * @param glTextureMinFilter The filtering mode used when zooming away from the texture.
     * @see <a href="https://www.khronos.org/opengl/wiki/Sampler_Object#Filtering">OpenGL Wiki: Sampler Object - Filtering</a>
     */
    public void setArrayTextureFilter(int glTextureMagFilter, int glTextureMinFilter){

        arrayTextureMagFilter = glTextureMagFilter;
        arrayTextureMinFilter = glTextureMinFilter;

        Core.gl30.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, arrayTextureHandle);

        Core.gl30.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL30.GL_TEXTURE_MAG_FILTER, glTextureMagFilter);
        Core.gl30.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL30.GL_TEXTURE_MIN_FILTER, glTextureMinFilter);

        if(glTextureMagFilter >= GL30.GL_NEAREST_MIPMAP_NEAREST && glTextureMagFilter <= GL30.GL_LINEAR_MIPMAP_LINEAR){
            useMipMaps = true;
        }else{
            useMipMaps = glTextureMinFilter >= GL30.GL_NEAREST_MIPMAP_NEAREST && glTextureMinFilter <= GL30.GL_LINEAR_MIPMAP_LINEAR;
        }

        mipMapsDirty = useMipMaps;
    }

    @Override
    public void draw(Texture texture, float[] spriteVertices, int offset, int count){

        // Assigns a texture unit to this texture, flushing if none is available
        final float ti = (float)activateTexture(texture);

        // spriteVertexSize is the number of floats an unmodified input vertex consists of,
        // therefore this loop iterates over the vertices stored in parameter spriteVertices.
        for(int srcPos = offset; srcPos < count; srcPos += spriteVertexSize){

            System.arraycopy(spriteVertices, srcPos, vertices, idx, spriteVertexSize);

            // Advance idx by vertex float count
            idx += spriteVertexSize - 3;

            // Scale UV coordinates to fit array texture
            vertices[idx++] *= subImageScaleWidth; //u
            vertices[idx++] *= subImageScaleHeight; //v
            idx++; //mixcolor (ignored)

            // Inject texture unit index and advance idx
            vertices[idx++] = ti;

            flushIfFull();
        }
    }

    @Override
    public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float rotation){
        flushIfFull();

        final float ti = activateTexture(region.texture);

        // bottom left and top right corner points relative to origin
        final float worldOriginX = x + originX;
        final float worldOriginY = y + originY;
        float fx = -originX;
        float fy = -originY;
        float fx2 = width - originX;
        float fy2 = height - originY;

        // construct corner points, start from top left and go counter clockwise

        float x1;
        float y1;
        float x2;
        float y2;
        float x3;
        float y3;
        float x4;
        float y4;

        // rotate
        if(rotation != 0){
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
        }else{
            x1 = fx;
            y1 = fy;

            x2 = fx;
            y2 = fy2;

            x3 = fx2;
            y3 = fy2;

            x4 = fx2;
            y4 = fy;
        }

        x1 += worldOriginX;
        y1 += worldOriginY;
        x2 += worldOriginX;
        y2 += worldOriginY;
        x3 += worldOriginX;
        y3 += worldOriginY;
        x4 += worldOriginX;
        y4 += worldOriginY;

        final float u = region.u * subImageScaleWidth;
        final float v = region.v2 * subImageScaleHeight;
        final float u2 = region.u2 * subImageScaleWidth;
        final float v2 = region.v * subImageScaleHeight;

        vertices[idx++] = x1;
        vertices[idx++] = y1;
        vertices[idx++] = colorPacked;
        vertices[idx++] = u;
        vertices[idx++] = v;
        vertices[idx++] = mixColorPacked;
        vertices[idx++] = ti;

        vertices[idx++] = x2;
        vertices[idx++] = y2;
        vertices[idx++] = colorPacked;
        vertices[idx++] = u;
        vertices[idx++] = v2;
        vertices[idx++] = mixColorPacked;
        vertices[idx++] = ti;

        vertices[idx++] = x3;
        vertices[idx++] = y3;
        vertices[idx++] = colorPacked;
        vertices[idx++] = u2;
        vertices[idx++] = v2;
        vertices[idx++] = mixColorPacked;
        vertices[idx++] = ti;

        vertices[idx++] = x4;
        vertices[idx++] = y4;
        vertices[idx++] = colorPacked;
        vertices[idx++] = u2;
        vertices[idx++] = v;
        vertices[idx++] = mixColorPacked;
        vertices[idx++] = ti;
    }

    /**
     * Convenience method to flush if the Batches vertex-array cannot hold an additional sprite ((spriteVertexSize + 1) * 4
     * vertices) anymore.
     */
    private void flushIfFull(){
        // original Sprite attribute size plus one extra float per sprite vertex
        if(vertices.length - idx < spriteFloatSize + spriteFloatSize / spriteVertexSize){
            flush();
        }
    }

    @Override
    public void flush(){
        if(idx == 0) return;

        currentTextureLFUSwaps = 0;

        Core.gl30.glActiveTexture(GL30.GL_TEXTURE0);
        Core.gl30.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, arrayTextureHandle);

        getShader().bind();

        setupMatrices();

        totalRenderCalls++;

        int spritesInBatch = idx / (spriteFloatSize + 4);
        if(spritesInBatch > maxSpritesInBatch) maxSpritesInBatch = spritesInBatch;
        int count = spritesInBatch * 6;

        if(useMipMaps && mipMapsDirty){
            Core.gl30.glGenerateMipmap(GL30.GL_TEXTURE_2D_ARRAY);
            mipMapsDirty = false;
        }

        Mesh mesh = this.mesh;

        mesh.setVertices(vertices, 0, idx);

        mesh.getIndicesBuffer().position(0);
        mesh.getIndicesBuffer().limit(count);

        blending.apply();

        mesh.render(getShader(), GL30.GL_TRIANGLES, 0, count);

        idx = 0;
    }

    /**
     * Assigns space on the Array Texture, sets up Texture scaling and manages the LFU cache.
     * @param texture The texture that shall be loaded into the cache, if it is not already loaded.
     * @return The texture slot that has been allocated to the selected texture
     */
    private int activateTexture(Texture texture){

        subImageScaleWidth = texture.getWidth() * invMaxTextureWidth;
        subImageScaleHeight = texture.getHeight() * invMaxTextureHeight;

        if(subImageScaleWidth > 1f || subImageScaleHeight > 1f){
            throw new IllegalStateException("Texture " + texture.getTextureObjectHandle() + " is larger than the Array Texture: ["
            + texture.getWidth() + "," + texture.getHeight() + "] > [" + maxTextureWidth + "," + maxTextureHeight + "]");
        }

        invTexWidth = subImageScaleWidth / texture.getWidth();
        invTexHeight = subImageScaleHeight / texture.getHeight();

        final int textureSlot = findTextureCacheIndex(texture);

        if(textureSlot >= 0){

            //don't throw out a texture we just used
            if(textureSlot == usedTexturesNextSwapSlot){
                usedTexturesNextSwapSlot = (usedTexturesNextSwapSlot + 1) % currentTextureLFUSize;
            }

            //update texture when it gets modified
            if(texture.lastModifications != texture.modifications){
                Log.info("Updating modified texture");
                copyTextureIntoArrayTexture(texture, textureSlot);
            }

            return textureSlot;
        }

        // If a free texture unit is available we just use it
        // If not we have to flush and then throw out the least accessed one.
        if(currentTextureLFUSize < maxTextureSlots){
            Log.info("Copying texture @ into LFU, slot @", texture, currentTextureLFUSize);

            // Put the texture into the next free slot
            usedTextures[currentTextureLFUSize] = texture;

            copyTextureIntoArrayTexture(texture, currentTextureLFUSize);
            currentTextureLFUSwaps++;

            return currentTextureLFUSize++;
        }else{

            // We have to flush if there is something in the pipeline using this texture already,
            // otherwise the texture index of previously rendered sprites gets invalidated
            if(idx > 0){
                flush();
            }

            final int slot = usedTexturesNextSwapSlot;

            usedTexturesNextSwapSlot = (usedTexturesNextSwapSlot + 1) % currentTextureLFUSize;
            usedTextures[slot] = texture;

            copyTextureIntoArrayTexture(texture, slot);

            // For statistics
            currentTextureLFUSwaps++;

            return slot;
        }
    }

    /**
     * Finds and returns the cache slot the Texture resides in.
     * @param texture The {@link Texture} which index should be searched for.
     * @return The index of the cache slot or -1 if not found.
     */
    private int findTextureCacheIndex(Texture texture){

        // This is our identifier for the textures
        final int textureHandle = texture.getTextureObjectHandle();

        // First try to see if the texture is already cached
        for(int i = 0; i < currentTextureLFUSize; i++){

            // getTextureObjectHandle() just returns an int,
            // it's fine to call this method instead of caching the value.
            if(textureHandle == usedTextures[i].getTextureObjectHandle()){
                return i;
            }
        }

        return -1;
    }

    /**
     * Causes the provided {@link Texture} to be reloaded if it is cached or loaded if it is not cached yet. This method can be
     * used to explicitly pre-load {@link Texture}s to cache if needed.
     * @param texture The {@link Texture} that should be reloaded.
     */
    public void reloadTexture(Texture texture){

        final int textureSlot = findTextureCacheIndex(texture);

        if(textureSlot < 0){
            activateTexture(texture);
        }else{
            copyTextureIntoArrayTexture(texture, textureSlot);
        }
    }

    /**
     * Copies a Texture to the internally managed Array Texture.
     * @param texture The Texture to copy onto the Array Texture.
     * @param slot The slice of the Array Texture to copy the texture onto.
     */
    private void copyTextureIntoArrayTexture(Texture texture, int slot){
        // Bind CopyFrameBuffer
        copyFramebuffer.beginBind();

        Core.gl30.glFramebufferTexture2D(GL30.GL_READ_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, texture.getTextureObjectHandle(), 0);

        Core.gl30.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);

        Core.gl30.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, arrayTextureHandle);

        Core.gl30.glCopyTexSubImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, 0, 0, slot, 0, 0, copyFramebuffer.getWidth(), copyFramebuffer.getHeight());

        copyFramebuffer.endBind();

        if(useMipMaps){
            mipMapsDirty = true;
        }

        //save modifications so we know when it changes
        texture.lastModifications = texture.modifications;
    }

    /** Returns a new instance of the default shader used by ArrayTextureSpriteBatch when no shader is specified. */
    public static Shader createDefaultShader(){

        return new Shader(
          "in vec4 " + Shader.positionAttribute + ";\n"
        + "in vec4 " + Shader.colorAttribute + ";\n"
        + "in vec2 " + Shader.texcoordAttribute + "0;\n"
        + "in vec4 a_mix_color;\n"
        + "in float texture_index;\n"
        + "uniform mat4 u_projTrans;\n"
        + "out vec4 v_color;\n"
        + "out vec4 v_mix_color;\n"
        + "out vec2 v_texCoords;\n"
        + "out float v_texture_index;\n"
        + "\n"
        + "void main(){\n"
        + "   v_color = " + Shader.colorAttribute + ";\n"
        + "   v_color.a = v_color.a * (255.0/254.0);\n"
        + "   v_mix_color = a_mix_color;\n"
        + "   v_mix_color.a *= (255.0/254.0);\n"
        + "   v_texCoords = " + Shader.texcoordAttribute + "0;\n"
        + "   v_texture_index = texture_index;\n"
        + "   gl_Position =  u_projTrans * " + Shader.positionAttribute + ";\n"
        + "}\n",


          "in lowp vec4 v_color;\n"
        + "in lowp vec4 v_mix_color;\n"
        + "in vec2 v_texCoords;\n"
        + "in float v_texture_index;\n"
        + "uniform mediump sampler2DArray u_texturearray;\n"
        + "void main(){\n"
        + "  vec4 c = texture(u_texturearray, vec3(v_texCoords, v_texture_index));\n"
        + "  fragColor = v_color * mix(c, vec4(v_mix_color.rgb, c.a), v_mix_color.a);\n"
        + "}");
    }

    /**
     * Converts a 'standard' shader into an array texture shader. This MUST be called before the GL3 preprocessor is run in a shader.
     * Does not cover all cases. Performs simple string replacement only.
     * */
    public static String preprocessShader(String shader, boolean fragment){

        if(fragment){
            //fragment shaders require:
            // - a uniform sampler array (u_texturearray)
            // - an in v_texture_index parameter to specify the texture
            // - a bridge function that samples the texture array with the index
            // - replacement of all calls of texture2D(...) with the bridge function above

            //find last definition of relevant variable
            int endIndex = shader.lastIndexOf("\n", Math.max(shader.lastIndexOf("varying"), shader.lastIndexOf("uniform")));

            //the function to inject
            String function = "\nvec4 __SAMPLE_ARRAY(vec2 coords){ return texture(u_texturearray, vec3(coords, v_texture_index)); }\n";

            return "uniform mediump sampler2DArray u_texturearray;\nin float v_texture_index;\n" //prepend sampler info
                + shader.substring(0, endIndex)
                + function
                + shader.substring(endIndex)
                    .replace("texture2D(u_texture,", "__SAMPLE_ARRAY("); //replace samplings of u_texture with sample array
        }else{
            //vertex shaders require:
            // - a texture index parameter (in + out variants)
            // - an assignment of that index from in to out in the main() method

            int maini = shader.indexOf("void main(){");
            if(maini == -1) maini = shader.indexOf("void main() {");
            if(maini == -1) throw new IllegalArgumentException("Your shader is missing a `void main(){` function. Add it, or fix your formatting. Don't dare use newline curly braces.");
            int offset = "void main() {".length();
            maini += offset;
            shader = "\nin float texture_index;\nout float v_texture_index;\n" + shader.substring(0, maini) + "\nv_texture_index = texture_index;\n" + shader.substring(maini);
        }

        return shader;
    }

}