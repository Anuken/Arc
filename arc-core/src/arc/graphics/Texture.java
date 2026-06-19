package arc.graphics;

import arc.*;
import arc.files.*;

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

    protected Texture(int target, int handle){
        super(target, handle);
    }

    public Texture(){
        super(Gl.texture2d, Gl.genTexture());
    }

    public Texture(String internalPath){
        this(Core.files.internal(internalPath));
    }

    public Texture(Fi file){
        this(file, false);
    }

    public Texture(Fi file, boolean useMipMaps){
        this();
        load(file, useMipMaps);
    }

    public Texture(Pixmap pixmap){
        this(pixmap, false);
    }

    public Texture(Pixmap pixmap, boolean useMipMaps){
        this();
        load(pixmap, useMipMaps, false);
    }

    public void load(Fi file, boolean mipmaps){
        load(new Pixmap(file), mipmaps, true);
    }

    public void load(Pixmap pixmap){
        load(pixmap, false, false);
    }

    public void load(Pixmap pixmap, boolean mipmaps, boolean dispose){
        this.width = pixmap.getWidth();
        this.height = pixmap.getHeight();

        bind();
        setFilter(minFilter, magFilter);
        setWrap(uWrap, vWrap);

        Gl.texImage2D(glTarget, 0, pixmap.getGLInternalFormat(), pixmap.width, pixmap.height, 0, pixmap.getGLFormat(), pixmap.getGLType(), pixmap.pixels);

        if(mipmaps) Gl.generateMipmap(glTarget);

        if(dispose) pixmap.dispose();
    }

    public void draw(Pixmap pixmap){
        draw(pixmap, 0, 0);
    }

    /**
     * Draws the given {@link Pixmap} to the texture at position x, y. No clipping is performed, so you have to make sure that you
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

    @Override
    public boolean isDisposed(){
        return glHandle == 0;
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
