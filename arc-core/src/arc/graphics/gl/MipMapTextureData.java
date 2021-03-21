package arc.graphics.gl;

import arc.graphics.GLTexture;
import arc.graphics.Pixmap;
import arc.graphics.Pixmap.Format;
import arc.graphics.TextureData;
import arc.util.ArcRuntimeException;

/**
 * This class will load each contained TextureData to the chosen mipmap level.
 * All the mipmap levels must be defined and cannot be null.
 */
public class MipMapTextureData implements TextureData{
    TextureData[] mips;

    /** @param mipMapData must be != null and its length must be >= 1 */
    public MipMapTextureData(TextureData... mipMapData){
        mips = new TextureData[mipMapData.length];
        System.arraycopy(mipMapData, 0, mips, 0, mipMapData.length);
    }

    @Override
    public boolean isCustom(){
        return true;
    }

    @Override
    public boolean isPrepared(){
        return true;
    }

    @Override
    public void prepare(){
    }

    @Override
    public Pixmap consumePixmap(){
        throw new ArcRuntimeException("It's compressed, use the compressed method");
    }

    @Override
    public boolean disposePixmap(){
        return false;
    }

    @Override
    public void consumeCustomData(int target){
        for(int i = 0; i < mips.length; ++i){
            GLTexture.uploadImageData(target, mips[i], i);
        }
    }

    @Override
    public int getWidth(){
        return mips[0].getWidth();
    }

    @Override
    public int getHeight(){
        return mips[0].getHeight();
    }

    @Override
    public Format getFormat(){
        return mips[0].getFormat();
    }

    @Override
    public boolean useMipMaps(){
        return false;
    }

}