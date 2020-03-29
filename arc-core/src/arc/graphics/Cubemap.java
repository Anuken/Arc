package arc.graphics;

import arc.Application;
import arc.Core;
import arc.assets.AssetManager;
import arc.assets.loaders.AssetLoader;
import arc.assets.loaders.CubemapLoader.CubemapParameter;
import arc.struct.Array;
import arc.files.Fi;
import arc.graphics.Pixmap.Format;
import arc.graphics.Texture.TextureFilter;
import arc.graphics.Texture.TextureWrap;
import arc.graphics.gl.FacedCubemapData;
import arc.graphics.gl.PixmapTextureData;
import arc.math.geom.Vec3;
import arc.util.ArcRuntimeException;

import java.util.HashMap;
import java.util.Map;

/**
 * Wraps a standard OpenGL ES Cubemap. Must be disposed when it is no longer used.
 * @author Xoppa
 */
public class Cubemap extends GLTexture{
    final static Map<Application, Array<Cubemap>> managedCubemaps = new HashMap<>();
    private static AssetManager assetManager;
    protected CubemapData data;

    /** Construct a Cubemap based on the given CubemapData. */
    public Cubemap(CubemapData data){
        super(GL20.GL_TEXTURE_CUBE_MAP);
        this.data = data;
        load(data);
    }

    public Cubemap(String base){
        this(Core.files.internal(base + "right.png"),
            Core.files.internal(base + "left.png"),
            Core.files.internal(base + "top.png"),
            Core.files.internal(base + "bottom.png"),
            Core.files.internal(base + "front.png"),
            Core.files.internal(base + "back.png")

        );
    }

    /** Construct a Cubemap with the specified texture files for the sides, does not generate mipmaps. */
    public Cubemap(Fi positiveX, Fi negativeX, Fi positiveY, Fi negativeY, Fi positiveZ, Fi negativeZ){
        this(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ, false);
    }

    /** Construct a Cubemap with the specified texture files for the sides, optionally generating mipmaps. */
    public Cubemap(Fi positiveX, Fi negativeX, Fi positiveY, Fi negativeY, Fi positiveZ, Fi negativeZ, boolean useMipMaps){
        this(TextureData.Factory.loadFromFile(positiveX, useMipMaps), TextureData.Factory.loadFromFile(negativeX, useMipMaps),
        TextureData.Factory.loadFromFile(positiveY, useMipMaps), TextureData.Factory.loadFromFile(negativeY, useMipMaps),
        TextureData.Factory.loadFromFile(positiveZ, useMipMaps), TextureData.Factory.loadFromFile(negativeZ, useMipMaps));
    }

    /** Construct a Cubemap with the specified {@link Pixmap}s for the sides, does not generate mipmaps. */
    public Cubemap(Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, Pixmap negativeY, Pixmap positiveZ, Pixmap negativeZ){
        this(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ, false);
    }

    /** Construct a Cubemap with the specified {@link Pixmap}s for the sides, optionally generating mipmaps. */
    public Cubemap(Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, Pixmap negativeY, Pixmap positiveZ, Pixmap negativeZ, boolean useMipMaps){
        this(positiveX == null ? null : new PixmapTextureData(positiveX, null, useMipMaps, false), negativeX == null ? null
        : new PixmapTextureData(negativeX, null, useMipMaps, false), positiveY == null ? null : new PixmapTextureData(positiveY,
        null, useMipMaps, false), negativeY == null ? null : new PixmapTextureData(negativeY, null, useMipMaps, false),
        positiveZ == null ? null : new PixmapTextureData(positiveZ, null, useMipMaps, false), negativeZ == null ? null
        : new PixmapTextureData(negativeZ, null, useMipMaps, false));
    }

    /** Construct a Cubemap with {@link Pixmap}s for each side of the specified size. */
    public Cubemap(int width, int height, int depth, Format format){
        this(new PixmapTextureData(new Pixmap(depth, height, format), null, false, true), new PixmapTextureData(new Pixmap(depth,
        height, format), null, false, true), new PixmapTextureData(new Pixmap(width, depth, format), null, false, true),
        new PixmapTextureData(new Pixmap(width, depth, format), null, false, true), new PixmapTextureData(new Pixmap(width,
        height, format), null, false, true), new PixmapTextureData(new Pixmap(width, height, format), null, false, true));
    }

    /** Construct a Cubemap with the specified {@link TextureData}'s for the sides */
    public Cubemap(TextureData positiveX, TextureData negativeX, TextureData positiveY, TextureData negativeY,
                   TextureData positiveZ, TextureData negativeZ){
        super(GL20.GL_TEXTURE_CUBE_MAP);
        minFilter = TextureFilter.Nearest;
        magFilter = TextureFilter.Nearest;
        uWrap = TextureWrap.ClampToEdge;
        vWrap = TextureWrap.ClampToEdge;
        data = new FacedCubemapData(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ);
        load(data);
    }

    private static void addManagedCubemap(Application app, Cubemap cubemap){
        Array<Cubemap> managedCubemapArray = managedCubemaps.get(app);
        if(managedCubemapArray == null) managedCubemapArray = new Array<>();
        managedCubemapArray.add(cubemap);
        managedCubemaps.put(app, managedCubemapArray);
    }

    /** Clears all managed cubemaps. This is an internal method. Do not use it! */
    public static void clearAllCubemaps(Application app){
        managedCubemaps.remove(app);
    }

    /** Invalidate all managed cubemaps. This is an internal method. Do not use it! */
    public static void invalidateAllCubemaps(Application app){
        Array<Cubemap> managedCubemapArray = managedCubemaps.get(app);
        if(managedCubemapArray == null) return;

        if(assetManager == null){
            for(int i = 0; i < managedCubemapArray.size; i++){
                Cubemap cubemap = managedCubemapArray.get(i);
                cubemap.reload();
            }
        }else{
            // first we have to make sure the AssetManager isn't loading anything anymore,
            // otherwise the ref counting trick below wouldn't work (when a cubemap is
            // currently on the task stack of the manager.)
            assetManager.finishLoading();

            // next we go through each cubemap and reload either directly or via the
            // asset manager.
            Array<Cubemap> cubemaps = new Array<>(managedCubemapArray);
            for(Cubemap cubemap : cubemaps){
                String fileName = assetManager.getAssetFileName(cubemap);
                if(fileName == null){
                    cubemap.reload();
                }else{
                    // get the ref count of the cubemap, then set it to 0 so we
                    // can actually remove it from the assetmanager. Also set the
                    // handle to zero, otherwise we might accidentially dispose
                    // already reloaded cubemaps.
                    final int refCount = assetManager.getReferenceCount(fileName);
                    assetManager.setReferenceCount(fileName, 0);
                    cubemap.glHandle = 0;

                    // create the parameters, passing the reference to the cubemap as
                    // well as a callback that sets the ref count.
                    CubemapParameter params = new CubemapParameter();
                    params.cubemapData = cubemap.getCubemapData();
                    params.minFilter = cubemap.getMinFilter();
                    params.magFilter = cubemap.getMagFilter();
                    params.wrapU = cubemap.getUWrap();
                    params.wrapV = cubemap.getVWrap();
                    params.cubemap = cubemap; // special parameter which will ensure that the references stay the same.
                    params.loadedCallback = (assetManager, fileName1, type) -> assetManager.setReferenceCount(fileName1, refCount);

                    // unload the c, create a new gl handle then reload it.
                    assetManager.unload(fileName);
                    cubemap.glHandle = Gl.genTexture();
                    assetManager.load(fileName, Cubemap.class, params);
                }
            }
            managedCubemapArray.clear();
            managedCubemapArray.addAll(cubemaps);
        }
    }

    /**
     * Sets the {@link AssetManager}. When the context is lost, cubemaps managed by the asset manager are reloaded by the manager
     * on a separate thread (provided that a suitable {@link AssetLoader} is registered with the manager). Cubemaps not managed by
     * the AssetManager are reloaded via the usual means on the rendering thread.
     * @param manager the asset manager.
     */
    public static void setAssetManager(AssetManager manager){
        Cubemap.assetManager = manager;
    }

    public static String getManagedStatus(){
        StringBuilder builder = new StringBuilder();
        builder.append("Managed cubemap/app: { ");
        for(Application app : managedCubemaps.keySet()){
            builder.append(managedCubemaps.get(app).size);
            builder.append(" ");
        }
        builder.append("}");
        return builder.toString();
    }

    /** @return the number of managed cubemaps currently loaded */
    public static int getNumManagedCubemaps(){
        return managedCubemaps.get(Core.app).size;
    }

    /** Sets the sides of this cubemap to the specified {@link CubemapData}. */
    public void load(CubemapData data){
        if(!data.isPrepared()) data.prepare();
        bind();
        unsafeSetFilter(minFilter, magFilter, true);
        unsafeSetWrap(uWrap, vWrap, true);
        data.consumeCubemapData();
        Gl.bindTexture(glTarget, 0);
    }

    public CubemapData getCubemapData(){
        return data;
    }

    @Override
    public boolean isManaged(){
        return data.isManaged();
    }

    @Override
    protected void reload(){
        if(!isManaged()) throw new ArcRuntimeException("Tried to reload an unmanaged Cubemap");
        glHandle = Gl.genTexture();
        load(data);
    }

    @Override
    public int getWidth(){
        return data.getWidth();
    }

    @Override
    public int getHeight(){
        return data.getHeight();
    }

    @Override
    public int getDepth(){
        return 0;
    }

    /** Disposes all resources associated with the cubemap */
    @Override
    public void dispose(){
        // this is a hack. reason: we have to set the glHandle to 0 for textures that are
        // reloaded through the asset manager as we first remove (and thus dispose) the texture
        // and then reload it. the glHandle is set to 0 in invalidateAllTextures prior to
        // removal from the asset manager.
        if(glHandle == 0) return;
        delete();
        if(data.isManaged())
            if(managedCubemaps.get(Core.app) != null) managedCubemaps.get(Core.app).remove(this, true);
    }

    /** Enum to identify each side of a Cubemap */
    public enum CubemapSide{
        /** The positive X and first side of the cubemap */
        PositiveX(0, GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, -1, 0, 1, 0, 0),
        /** The negative X and second side of the cubemap */
        NegativeX(1, GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, -1, 0, -1, 0, 0),
        /** The positive Y and third side of the cubemap */
        PositiveY(2, GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, 0, 1, 0, 1, 0),
        /** The negative Y and fourth side of the cubemap */
        NegativeY(3, GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, 0, -1, 0, -1, 0),
        /** The positive Z and fifth side of the cubemap */
        PositiveZ(4, GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, -1, 0, 0, 0, 1),
        /** The negative Z and sixth side of the cubemap */
        NegativeZ(5, GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, -1, 0, 0, 0, -1);

        /** The zero based index of the side in the cubemap */
        public final int index;
        /** The OpenGL target (used for glTexImage2D) of the side. */
        public final int glEnum;
        /** The up vector to target the side. */
        public final Vec3 up;
        /** The direction vector to target the side. */
        public final Vec3 direction;

        CubemapSide(int index, int glEnum, float upX, float upY, float upZ, float directionX, float directionY, float directionZ){
            this.index = index;
            this.glEnum = glEnum;
            this.up = new Vec3(upX, upY, upZ);
            this.direction = new Vec3(directionX, directionY, directionZ);
        }

        /** @return The OpenGL target (used for glTexImage2D) of the side. */
        public int getGLEnum(){
            return glEnum;
        }

        /** @return The up vector of the side. */
        public Vec3 getUp(Vec3 out){
            return out.set(up);
        }

        /** @return The direction vector of the side. */
        public Vec3 getDirection(Vec3 out){
            return out.set(direction);
        }
    }

}
