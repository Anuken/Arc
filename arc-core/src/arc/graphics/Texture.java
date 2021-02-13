package arc.graphics;

import arc.Application;
import arc.Core;
import arc.assets.AssetManager;
import arc.assets.loaders.AssetLoader;
import arc.assets.loaders.TextureLoader.TextureParameter;
import arc.graphics.TextureData.*;
import arc.struct.Seq;
import arc.files.Fi;
import arc.graphics.Pixmap.Format;
import arc.graphics.gl.FileTextureData;
import arc.graphics.gl.PixmapTextureData;
import arc.util.ArcRuntimeException;

import java.util.HashMap;
import java.util.Map;

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
    final static Map<Application, Seq<Texture>> managedTextures = new HashMap<>();
    private static AssetManager assetManager;

    TextureData data;

    public Texture(String internalPath){
        this(Core.files.internal(internalPath));
    }

    public Texture(Fi file){
        this(file, null, false);
    }

    public Texture(Fi file, boolean useMipMaps){
        this(file, null, useMipMaps);
    }

    public Texture(Fi file, Format format, boolean useMipMaps){
        this(TextureDataFactory.loadFromFile(file, format, useMipMaps));
    }

    public Texture(Pixmap pixmap){
        this(new PixmapTextureData(pixmap, null, false, false));
    }

    public Texture(Pixmap pixmap, boolean useMipMaps){
        this(new PixmapTextureData(pixmap, null, useMipMaps, false));
    }

    public Texture(Pixmap pixmap, Format format, boolean useMipMaps){
        this(new PixmapTextureData(pixmap, format, useMipMaps, false));
    }

    public Texture(int width, int height, Format format){
        this(new PixmapTextureData(new Pixmap(width, height, format), null, false, true));
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
        if(data.isManaged()) addManagedTexture(Core.app, this);
    }

    public static Texture createEmpty(TextureData data){
        Texture tex = new Texture();
        tex.data = data;
        return tex;
    }

    private static void addManagedTexture(Application app, Texture texture){
        Seq<Texture> managedTextureArray = managedTextures.get(app);
        if(managedTextureArray == null) managedTextureArray = new Seq<>();
        managedTextureArray.add(texture);
        managedTextures.put(app, managedTextureArray);
    }

    /** Clears all managed textures. This is an internal method. Do not use it! */
    public static void clearAllTextures(Application app){
        managedTextures.remove(app);
    }

    /** Invalidate all managed textures. This is an internal method. Do not use it! */
    public static void invalidateAllTextures(Application app){
        Seq<Texture> managedTextureArray = managedTextures.get(app);
        if(managedTextureArray == null) return;

        if(assetManager == null){
            for(int i = 0; i < managedTextureArray.size; i++){
                Texture texture = managedTextureArray.get(i);
                texture.reload();
            }
        }else{
            // first we have to make sure the AssetManager isn't loading anything anymore,
            // otherwise the ref counting trick below wouldn't work (when a texture is
            // currently on the task stack of the manager.)
            assetManager.finishLoading();

            // next we go through each texture and reload either directly or via the
            // asset manager.
            Seq<Texture> textures = new Seq<>(managedTextureArray);
            for(Texture texture : textures){
                String fileName = assetManager.getAssetFileName(texture);
                if(fileName == null){
                    texture.reload();
                }else{
                    // get the ref count of the texture, then set it to 0 so we
                    // can actually remove it from the assetmanager. Also set the
                    // handle to zero, otherwise we might accidentially dispose
                    // already reloaded textures.
                    final int refCount = assetManager.getReferenceCount(fileName);
                    assetManager.setReferenceCount(fileName, 0);
                    texture.glHandle = 0;

                    // create the parameters, passing the reference to the texture as
                    // well as a callback that sets the ref count.
                    TextureParameter params = new TextureParameter();
                    params.textureData = texture.getTextureData();
                    params.minFilter = texture.getMinFilter();
                    params.magFilter = texture.getMagFilter();
                    params.wrapU = texture.getUWrap();
                    params.wrapV = texture.getVWrap();
                    params.genMipMaps = texture.data.useMipMaps(); // not sure about this?
                    params.texture = texture; // special parameter which will ensure that the references stay the same.
                    params.loadedCallback = (assetManager, fileName1, type) -> assetManager.setReferenceCount(fileName1, refCount);

                    // unload the texture, create a new gl handle then reload it.
                    assetManager.unload(fileName);
                    texture.glHandle = Gl.genTexture();
                    assetManager.load(fileName, Texture.class, params);
                }
            }
            managedTextureArray.clear();
            managedTextureArray.addAll(textures);
        }
    }

    /**
     * Sets the {@link AssetManager}. When the context is lost, textures managed by the asset manager are reloaded by the manager
     * on a separate thread (provided that a suitable {@link AssetLoader} is registered with the manager). Textures not managed by
     * the AssetManager are reloaded via the usual means on the rendering thread.
     * @param manager the asset manager.
     */
    public static void setAssetManager(AssetManager manager){
        Texture.assetManager = manager;
    }

    public static String getManagedStatus(){
        StringBuilder builder = new StringBuilder();
        builder.append("Managed textures/app: { ");
        for(Application app : managedTextures.keySet()){
            builder.append(managedTextures.get(app).size);
            builder.append(" ");
        }
        builder.append("}");
        return builder.toString();
    }

    /** @return the number of managed textures currently loaded */
    public static int getNumManagedTextures(){
        return managedTextures.get(Core.app).size;
    }

    public void load(TextureData data){
        if(this.data != null && data.isManaged() != this.data.isManaged())
            throw new ArcRuntimeException("New data must have the same managed status as the old data");
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

    /**
     * Used internally to reload after context loss. Creates a new GL handle then calls {@link #load(TextureData)}. Use this only
     * if you know what you're doing!
     */
    @Override
    protected void reload(){
        if(!isManaged()) throw new ArcRuntimeException("Tried to reload unmanaged Texture");
        glHandle = Gl.genTexture();
        load(data);
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
        if(data.isManaged()) throw new ArcRuntimeException("can't draw to a managed texture");

        bind();
        Gl.texSubImage2D(glTarget, 0, x, y, pixmap.getWidth(), pixmap.getHeight(), pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
    }

    @Override
    public int getDepth(){
        return 0;
    }

    public TextureData getTextureData(){
        return data;
    }

    /** @return whether this texture is managed or not. */
    @Override
    public boolean isManaged(){
        return data.isManaged();
    }

    /** Disposes all resources associated with the texture */
    @Override
    public void dispose(){
        // this is a hack. reason: we have to set the glHandle to 0 for textures that are
        // reloaded through the asset manager as we first remove (and thus dispose) the texture
        // and then reload it. the glHandle is set to 0 in invalidateAllTextures prior to
        // removal from the asset manager.
        if(glHandle == 0) return;
        delete();
        if(data.isManaged())
            if(managedTextures.get(Core.app) != null) managedTextures.get(Core.app).remove(this, true);
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

        final int glEnum;

        TextureWrap(int glEnum){
            this.glEnum = glEnum;
        }

        public int getGLEnum(){
            return glEnum;
        }
    }
}
