package arc.graphics;

import arc.*;
import arc.files.*;
import arc.graphics.Pixmap.*;
import arc.graphics.Texture.*;
import arc.graphics.gl.*;
import arc.math.geom.*;

/**
 * Wraps a standard OpenGL ES Cubemap. Must be disposed when it is no longer used.
 * @author Xoppa
 */
public class Cubemap extends GLTexture{
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
        this(TextureData.load(positiveX, useMipMaps), TextureData.load(negativeX, useMipMaps),
        TextureData.load(positiveY, useMipMaps), TextureData.load(negativeY, useMipMaps),
        TextureData.load(positiveZ, useMipMaps), TextureData.load(negativeZ, useMipMaps));
    }

    /** Construct a Cubemap with the specified {@link Pixmap}s for the sides, does not generate mipmaps. */
    public Cubemap(Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, Pixmap negativeY, Pixmap positiveZ, Pixmap negativeZ){
        this(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ, false);
    }

    /** Construct a Cubemap with the specified {@link Pixmap}s for the sides, optionally generating mipmaps. */
    public Cubemap(Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, Pixmap negativeY, Pixmap positiveZ, Pixmap negativeZ, boolean useMipMaps){
        this(
        positiveX == null ? null : new PixmapTextureData(positiveX, useMipMaps, false),
        negativeX == null ? null : new PixmapTextureData(negativeX, useMipMaps, false),
        positiveY == null ? null : new PixmapTextureData(positiveY, useMipMaps, false),
        negativeY == null ? null : new PixmapTextureData(negativeY, useMipMaps, false),
        positiveZ == null ? null : new PixmapTextureData(positiveZ, useMipMaps, false),
        negativeZ == null ? null : new PixmapTextureData(negativeZ, useMipMaps, false)
        );
    }

    /** Construct a Cubemap with {@link Pixmap}s for each side of the specified size. */
    public Cubemap(int width, int height, int depth, Format format){
        this(
        new PixmapTextureData(new Pixmap(depth, height), false, true),
        new PixmapTextureData(new Pixmap(depth, height), false, true),
        new PixmapTextureData(new Pixmap(width, depth), false, true),
        new PixmapTextureData(new Pixmap(width, depth), false, true),
        new PixmapTextureData(new Pixmap(width, height), false, true),
        new PixmapTextureData(new Pixmap(width, height), false, true)
        );
    }

    /** Construct a Cubemap with the specified {@link TextureData}'s for the sides */
    public Cubemap(TextureData positiveX, TextureData negativeX, TextureData positiveY, TextureData negativeY,
                   TextureData positiveZ, TextureData negativeZ){
        super(GL20.GL_TEXTURE_CUBE_MAP);
        minFilter = TextureFilter.nearest;
        magFilter = TextureFilter.nearest;
        uWrap = TextureWrap.clampToEdge;
        vWrap = TextureWrap.clampToEdge;
        data = new FacedCubemapData(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ);
        load(data);
    }

    /** Sets the sides of this cubemap to the specified {@link CubemapData}. */
    public void load(CubemapData data){
        if(!data.isPrepared()) data.prepare();
        this.width = data.getWidth();
        this.height = data.getHeight();
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
    public int getDepth(){
        return 0;
    }

    /** Enum to identify each side of a Cubemap */
    public enum CubemapSide{
        /** The positive X and first side of the cubemap */
        positiveX(0, GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, -1, 0, 1, 0, 0),
        /** The negative X and second side of the cubemap */
        negativeX(1, GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, -1, 0, -1, 0, 0),
        /** The positive Y and third side of the cubemap */
        positiveY(2, GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, 0, 1, 0, 1, 0),
        /** The negative Y and fourth side of the cubemap */
        negativeY(3, GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, 0, -1, 0, -1, 0),
        /** The positive Z and fifth side of the cubemap */
        positiveZ(4, GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, -1, 0, 0, 0, 1),
        /** The negative Z and sixth side of the cubemap */
        negativeZ(5, GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, -1, 0, 0, 0, -1);

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
