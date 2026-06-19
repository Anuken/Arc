package arc.graphics;

import arc.graphics.Texture.*;
import arc.util.*;

/**
 * Class representing an OpenGL texture by its target and handle. Keeps track of its state like the TextureFilter and TextureWrap.
 * Also provides some (protected) static methods to create TextureData and upload image data.
 * @author badlogic, Xoppa
 */
public abstract class GLTexture implements Disposable{
    /** The target of this texture, used when binding the texture, e.g. GL_TEXTURE_2D */
    public final int glTarget;
    /** Do not change. This is read-only and only set after texture data is loaded. */
    public int width, height;

    protected int glHandle;
    protected TextureFilter minFilter = TextureFilter.nearest;
    protected TextureFilter magFilter = TextureFilter.nearest;
    protected TextureWrap uWrap = TextureWrap.clampToEdge;
    protected TextureWrap vWrap = TextureWrap.clampToEdge;

    /** Generates a new OpenGL texture with the specified target. */
    public GLTexture(int glTarget){
        this(glTarget, Gl.genTexture());
    }

    public GLTexture(int glTarget, int glHandle){
        this.glTarget = glTarget;
        this.glHandle = glHandle;
    }

    /** @return the depth of the texture in pixels */
    public int getDepth(){
        return 0;
    }

    /**
     * Binds this texture. The texture will be bound to the currently active texture unit specified via
     * {@link GL20#glActiveTexture(int)}.
     */
    public void bind(){
        Gl.bindTexture(glTarget, glHandle);
    }

    /**
     * Binds the texture to the given texture unit. Sets the currently active texture unit via {@link GL20#glActiveTexture(int)}.
     * @param unit the unit (0 to MAX_TEXTURE_UNITS).
     */
    public void bind(int unit){
        Gl.activeTexture(Gl.texture0 + unit);
        Gl.bindTexture(glTarget, glHandle);
    }

    /** @return The {@link Texture.TextureFilter} used for minification. */
    public TextureFilter getMinFilter(){
        return minFilter;
    }

    /** @return The {@link Texture.TextureFilter} used for magnification. */
    public TextureFilter getMagFilter(){
        return magFilter;
    }

    /** @return The {@link Texture.TextureWrap} used for horizontal (U) texture coordinates. */
    public TextureWrap getUWrap(){
        return uWrap;
    }

    /** @return The {@link Texture.TextureWrap} used for vertical (V) texture coordinates. */
    public TextureWrap getVWrap(){
        return vWrap;
    }

    /** @return The OpenGL handle for this texture. */
    public int getTextureObjectHandle(){
        return glHandle;
    }

    public void setWrap(TextureWrap wrap){
        setWrap(wrap, wrap);
    }

    /**
     * Sets the {@link TextureWrap} for this texture on the u and v axis. This will bind this texture!
     * @param u the u wrap
     * @param v the v wrap
     */
    public void setWrap(TextureWrap u, TextureWrap v){
        this.uWrap = u;
        this.vWrap = v;
        bind();
        Gl.texParameteri(glTarget, GL20.GL_TEXTURE_WRAP_S, u.getGLEnum());
        Gl.texParameteri(glTarget, GL20.GL_TEXTURE_WRAP_T, v.getGLEnum());
    }

    public void setFilter(TextureFilter filter){
        setFilter(filter, filter);
    }

    /**
     * Sets the {@link TextureFilter} for this texture for minification and magnification. This will bind this texture!
     * @param minFilter the minification filter
     * @param magFilter the magnification filter
     */
    public void setFilter(TextureFilter minFilter, TextureFilter magFilter){
        this.minFilter = minFilter;
        this.magFilter = magFilter;
        bind();
        Gl.texParameteri(glTarget, GL20.GL_TEXTURE_MIN_FILTER, minFilter.glEnum);
        Gl.texParameteri(glTarget, GL20.GL_TEXTURE_MAG_FILTER, magFilter.glEnum);
    }

    @Override
    public void dispose(){
        if(glHandle != 0){
            Gl.deleteTexture(glHandle);
            glHandle = 0;
        }
    }
}
