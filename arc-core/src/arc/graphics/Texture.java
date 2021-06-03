package arc.graphics;

import arc.*;
import arc.files.*;
import arc.graphics.gl.*;

/**
 * A Texture wraps a standard OpenGL ES texture.
 * <p>
 * A Texture can be managed. If the OpenGL context is lost all managed textures get invalidated. This happens when a user switches
 * to another application or receives an incoming call. Managed textures get reloaded automatically.
 * <p>
 * A Texture has to be bound via the {@link Texture#bind()} method in order for it to be applied to geometry. The texture will be
 * bound to the currently active texture unit specified via {@link GL20#glActiveTexture(int)}.
 * <p>
 * You can draw {@link Pixmap}s to a texture at any time. The changes will be automatically uploaded to texture memory. This is of
 * course not extremely fast so use it with care. It also only works with unmanaged textures.
 * <p>
 * A Texture must be disposed when it is no longer used
 * @author badlogicgames@gmail.com
 */
public class Texture extends GLTexture{
    TextureData data;

    public Texture(String internalPath){
        this(Core.files.internal(internalPath));
    }

    public Texture(Fi file){
        this(file, false);
    }

    public Texture(Fi file, boolean useMipMaps){
        this(TextureData.load(file, useMipMaps));
    }

    public Texture(Pixmap pixmap){
        this(new PixmapTextureData(pixmap, false, false));
    }

    public Texture(Pixmap pixmap, boolean useMipMaps){
        this(new PixmapTextureData(pixmap, useMipMaps, false));
    }

    public Texture(int width, int height){
        this(new PixmapTextureData(new Pixmap(width, height), false, true));
    }

    public Texture(TextureData data){
        this(GL20.GL_TEXTURE_2D, Gl.genTexture(), data);
    }

    /** For use in mocking only! */
    private Texture(){
        super(0, 0);
    }

    protected Texture(int glTarget, int glHandle, TextureData data){
        super(glTarget, glHandle);
        load(data);
    }

    public static Texture createEmpty(TextureData data){
        Texture tex = new Texture();
        tex.data = data;
        return tex;
    }

    public void load(TextureData data){
        this.data = data;
        this.width = data.getWidth();
        this.height = data.getHeight();

        if(!data.isPrepared()) data.prepare();

        bind();
        uploadImageData(Gl.texture2d, data);

        unsafeSetFilter(minFilter, magFilter, true);
        unsafeSetWrap(uWrap, vWrap, true);
        Gl.bindTexture(glTarget, 0);
    }

    public void draw(Pixmap pixmap){
        draw(pixmap, 0, 0);
    }

    /**
     * Draws the given {@link Pixmap} to the texture at position x, y. No clipping is performed so you have to make sure that you
     * draw only inside the texture region. Note that this will only draw to mipmap level 0!
     * @param pixmap The Pixmap
     * @param x The x coordinate in pixels
     * @param y The y coordinate in pixels
     */
    public void draw(Pixmap pixmap, int x, int y){
        bind();
        Gl.texSubImage2D(glTarget, 0, x, y, pixmap.width, pixmap.height, pixmap.getGLFormat(), pixmap.getGLType(), pixmap.pixels);
    }

    @Override
    public int getDepth(){
        return 0;
    }

    public TextureData getTextureData(){
        return data;
    }

    @Override
    public boolean isDisposed(){
        return glHandle == 0;
    }

    public String toString(){
        if(data instanceof FileTextureData) return data.toString();
        return super.toString();
    }

    public enum TextureFilter{
        /** Fetch the nearest texel that best maps to the pixel on screen. */
        nearest(GL20.GL_NEAREST),

        /** Fetch four nearest texels that best maps to the pixel on screen. */
        linear(GL20.GL_LINEAR),

        /** @see TextureFilter#mipMapLinearLinear */
        mipMap(GL20.GL_LINEAR_MIPMAP_LINEAR),

        /**
         * Fetch the best fitting image from the mip map chain based on the pixel/texel ratio and then sample the texels with a
         * nearest filter.
         */
        mipMapNearestNearest(GL20.GL_NEAREST_MIPMAP_NEAREST),

        /**
         * Fetch the best fitting image from the mip map chain based on the pixel/texel ratio and then sample the texels with a
         * Linear filter.
         */
        mipMapLinearNearest(GL20.GL_LINEAR_MIPMAP_NEAREST),

        /**
         * Fetch the two best fitting images from the mip map chain and then sample the nearest texel from each of the two images,
         * combining them to the final output pixel.
         */
        mipMapNearestLinear(GL20.GL_NEAREST_MIPMAP_LINEAR),

        /**
         * Fetch the two best fitting images from the mip map chain and then sample the four nearest texels from each of the two
         * images, combining them to the final output pixel.
         */
        mipMapLinearLinear(GL20.GL_LINEAR_MIPMAP_LINEAR);

        public static final TextureFilter[] all = values();

        public final int glEnum;

        TextureFilter(int glEnum){
            this.glEnum = glEnum;
        }

        public boolean isMipMap(){
            return glEnum != GL20.GL_NEAREST && glEnum != GL20.GL_LINEAR;
        }
    }

    public enum TextureWrap{
        mirroredRepeat(GL20.GL_MIRRORED_REPEAT), clampToEdge(GL20.GL_CLAMP_TO_EDGE), repeat(GL20.GL_REPEAT);

        public static final TextureWrap[] all = values();

        final int glEnum;

        TextureWrap(int glEnum){
            this.glEnum = glEnum;
        }

        public int getGLEnum(){
            return glEnum;
        }
    }
}
