package arc.graphics.gl;

import arc.graphics.Pixmap;
import arc.graphics.Pixmap.Format;
import arc.graphics.TextureData;
import arc.util.ArcRuntimeException;

public class PixmapTextureData implements TextureData{
    final Pixmap pixmap;
    final boolean useMipMaps;
    final boolean disposePixmap;

    public PixmapTextureData(Pixmap pixmap, boolean useMipMaps, boolean disposePixmap){
        this.pixmap = pixmap;
        this.useMipMaps = useMipMaps;
        this.disposePixmap = disposePixmap;
    }

    @Override
    public boolean disposePixmap(){
        return disposePixmap;
    }

    @Override
    public Pixmap consumePixmap(){
        return pixmap;
    }

    @Override
    public int getWidth(){
        return pixmap.width;
    }

    @Override
    public int getHeight(){
        return pixmap.height;
    }

    @Override
    public Format getFormat(){
        return Format.rgba8888;
    }

    @Override
    public boolean useMipMaps(){
        return useMipMaps;
    }

    @Override
    public boolean isCustom(){
        return false;
    }

    @Override
    public void consumeCustomData(int target){
        throw new ArcRuntimeException("This TextureData implementation does not upload data itself");
    }

    @Override
    public boolean isPrepared(){
        return true;
    }

    @Override
    public void prepare(){
        throw new ArcRuntimeException("prepare() must not be called on a PixmapTextureData instance as it is already prepared.");
    }
}
