package arc.graphics;

import arc.graphics.Texture.*;
import arc.graphics.gl.*;
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

    protected static void uploadImageData(int target, TextureData data){
        uploadImageData(target, data, 0);
    }

    public static void uploadImageData(int target, TextureData data, int miplevel){
        if(data == null){
            // FIXME: remove texture on target?
            return;
        }

        if(!data.isPrepared()) data.prepare();

        if(data.isCustom()){
            data.consumeCustomData(target);
            return;
        }

        Pixmap pixmap = data.consumePixmap();
        boolean disposePixmap = data.disposePixmap();

        Gl.pixelStorei(GL20.GL_UNPACK_ALIGNMENT, 1);
        if(data.useMipMaps()){
            MipMapGenerator.generateMipMap(target, pixmap, pixmap.width, pixmap.height);
        }else{
            Gl.texImage2D(target, miplevel, pixmap.getGLInternalFormat(), pixmap.width, pixmap.height, 0,
            pixmap.getGLFormat(), pixmap.getGLType(), pixmap.pixels);
        }
        if(disposePixmap) pixmap.dispose();
    }

    /** @return the depth of the texture in pixels */
    public abstract int getDepth();

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
        Gl.activeTexture(GL20.GL_TEXTURE0 + unit);
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

    /**
     * Sets the {@link TextureWrap} for this texture on the u and v axis. Assumes the texture is bound and active!
     * @param u the u wrap
     * @param v the v wrap
     */
    public void unsafeSetWrap(TextureWrap u, TextureWrap v){
        unsafeSetWrap(u, v, false);
    }

    /**
     * Sets the {@link TextureWrap} for this texture on the u and v axis. Assumes the texture is bound and active!
     * @param u the u wrap
     * @param v the v wrap
     * @param force True to always set the values, even if they are the same as the current values.
     */
    public void unsafeSetWrap(TextureWrap u, TextureWrap v, boolean force){
        if(u != null && (force || uWrap != u)){
            Gl.texParameteri(glTarget, GL20.GL_TEXTURE_WRAP_S, u.getGLEnum());
            uWrap = u;
        }
        if(v != null && (force || vWrap != v)){
            Gl.texParameteri(glTarget, GL20.GL_TEXTURE_WRAP_T, v.getGLEnum());
            vWrap = v;
        }
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

    /**
     * Sets the {@link TextureFilter} for this texture for minification and magnification. Assumes the texture is bound and active!
     * @param minFilter the minification filter
     * @param magFilter the magnification filter
     */
    public void unsafeSetFilter(TextureFilter minFilter, TextureFilter magFilter){
        unsafeSetFilter(minFilter, magFilter, false);
    }

    /**
     * Sets the {@link TextureFilter} for this texture for minification and magnification. Assumes the texture is bound and active!
     * @param minFilter the minification filter
     * @param magFilter the magnification filter
     * @param force True to always set the values, even if they are the same as the current values.
     */
    public void unsafeSetFilter(TextureFilter minFilter, TextureFilter magFilter, boolean force){
        if(minFilter != null && (force || this.minFilter != minFilter)){
            Gl.texParameteri(glTarget, GL20.GL_TEXTURE_MIN_FILTER, minFilter.glEnum);
            this.minFilter = minFilter;
        }
        if(magFilter != null && (force || this.magFilter != magFilter)){
            Gl.texParameteri(glTarget, GL20.GL_TEXTURE_MAG_FILTER, magFilter.glEnum);
            this.magFilter = magFilter;
        }
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
