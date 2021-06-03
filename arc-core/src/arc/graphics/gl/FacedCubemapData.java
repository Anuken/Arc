package arc.graphics.gl;

import arc.files.*;
import arc.graphics.*;
import arc.graphics.Cubemap.*;
import arc.graphics.Pixmap.*;
import arc.util.*;

/**
 * A FacedCubemapData holds a cubemap data definition based on a {@link TextureData} per face.
 * @author Vincent Nousquet
 */
public class FacedCubemapData implements CubemapData{
    protected final TextureData[] data = new TextureData[6];

    /**
     * Construct an empty Cubemap. Use the load(...) methods to set the texture of each side. Every side of the cubemap must be set
     * before it can be used.
     */
    public FacedCubemapData(){
        this((Pixmap)null, null, null, null, null, null);
    }

    /** Construct a Cubemap with the specified texture files for the sides, optionally generating mipmaps. */
    public FacedCubemapData(Fi positiveX, Fi negativeX, Fi positiveY, Fi negativeY, Fi positiveZ, Fi negativeZ){
        this(
        TextureData.load(positiveX, false), TextureData.load(negativeX, false), TextureData.load(positiveY, false),
        TextureData.load(negativeY, false), TextureData.load(positiveZ, false), TextureData.load(negativeZ, false)
        );
    }

    /** Construct a Cubemap with the specified texture files for the sides, optionally generating mipmaps. */
    public FacedCubemapData(Fi positiveX, Fi negativeX, Fi positiveY, Fi negativeY, Fi positiveZ, Fi negativeZ, boolean useMipMaps){
        this(
        TextureData.load(positiveX, useMipMaps), TextureData.load(negativeX, useMipMaps), TextureData.load(positiveY, useMipMaps),
        TextureData.load(negativeY, useMipMaps), TextureData.load(positiveZ, useMipMaps), TextureData.load(negativeZ, useMipMaps)
        );
    }

    /** Construct a Cubemap with the specified {@link Pixmap}s for the sides, does not generate mipmaps. */
    public FacedCubemapData(Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, Pixmap negativeY, Pixmap positiveZ, Pixmap negativeZ){
        this(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ, false);
    }

    /** Construct a Cubemap with the specified {@link Pixmap}s for the sides, optionally generating mipmaps. */
    public FacedCubemapData(Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, Pixmap negativeY, Pixmap positiveZ, Pixmap negativeZ, boolean useMipMaps){
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
    public FacedCubemapData(int width, int height, int depth, Format format){
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
    public FacedCubemapData(TextureData positiveX, TextureData negativeX, TextureData positiveY, TextureData negativeY, TextureData positiveZ, TextureData negativeZ){
        data[0] = positiveX;
        data[1] = negativeX;
        data[2] = positiveY;
        data[3] = negativeY;
        data[4] = positiveZ;
        data[5] = negativeZ;
    }

    /**
     * Loads the texture specified using the {@link Fi} and sets it to specified side, overwriting any previous data set to
     * that side. Note that you need to reload through {@link Cubemap#load(CubemapData)} any cubemap using this data for the change
     * to be taken in account.
     * @param side The {@link CubemapSide}
     * @param file The texture {@link Fi}
     */
    public void load(CubemapSide side, Fi file){
        data[side.index] = TextureData.load(file, false);
    }

    /**
     * Sets the specified side of this cubemap to the specified {@link Pixmap}, overwriting any previous data set to that side.
     * Note that you need to reload through {@link Cubemap#load(CubemapData)} any cubemap using this data for the change to be
     * taken in account.
     * @param side The {@link CubemapSide}
     * @param pixmap The {@link Pixmap}
     */
    public void load(CubemapSide side, Pixmap pixmap){
        data[side.index] = pixmap == null ? null : new PixmapTextureData(pixmap, false, false);
    }

    /** @return True if all sides of this cubemap are set, false otherwise. */
    public boolean isComplete(){
        for(int i = 0; i < data.length; i++)
            if(data[i] == null) return false;
        return true;
    }

    /** @return The {@link TextureData} for the specified side, can be null if the cubemap is incomplete. */
    public TextureData getTextureData(CubemapSide side){
        return data[side.index];
    }

    @Override
    public int getWidth(){
        int tmp, width = 0;
        if(data[CubemapSide.positiveZ.index] != null && (tmp = data[CubemapSide.positiveZ.index].getWidth()) > width)
            width = tmp;
        if(data[CubemapSide.negativeZ.index] != null && (tmp = data[CubemapSide.negativeZ.index].getWidth()) > width)
            width = tmp;
        if(data[CubemapSide.positiveY.index] != null && (tmp = data[CubemapSide.positiveY.index].getWidth()) > width)
            width = tmp;
        if(data[CubemapSide.negativeY.index] != null && (tmp = data[CubemapSide.negativeY.index].getWidth()) > width)
            width = tmp;
        return width;
    }

    @Override
    public int getHeight(){
        int tmp, height = 0;
        if(data[CubemapSide.positiveZ.index] != null && (tmp = data[CubemapSide.positiveZ.index].getHeight()) > height)
            height = tmp;
        if(data[CubemapSide.negativeZ.index] != null && (tmp = data[CubemapSide.negativeZ.index].getHeight()) > height)
            height = tmp;
        if(data[CubemapSide.positiveX.index] != null && (tmp = data[CubemapSide.positiveX.index].getHeight()) > height)
            height = tmp;
        if(data[CubemapSide.negativeX.index] != null && (tmp = data[CubemapSide.negativeX.index].getHeight()) > height)
            height = tmp;
        return height;
    }

    @Override
    public boolean isPrepared(){
        return false;
    }

    @Override
    public void prepare(){
        if(!isComplete()) throw new ArcRuntimeException("You need to complete your cubemap data before using it");
        for(TextureData datum : data) if(!datum.isPrepared()) datum.prepare();
    }

    @Override
    public void consumeCubemapData(){
        for(int i = 0; i < data.length; i++){
            if(data[i].isCustom()){
                data[i].consumeCustomData(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i);
            }else{
                Pixmap pixmap = data[i].consumePixmap();
                boolean disposePixmap = data[i].disposePixmap();
                Gl.pixelStorei(GL20.GL_UNPACK_ALIGNMENT, 1);
                Gl.texImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, pixmap.getGLInternalFormat(), pixmap.width,
                pixmap.height, 0, pixmap.getGLFormat(), pixmap.getGLType(), pixmap.pixels);
                if(disposePixmap) pixmap.dispose();
            }
        }
    }

}
