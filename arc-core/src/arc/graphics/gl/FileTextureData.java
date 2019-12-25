package arc.graphics.gl;

import arc.files.Fi;
import arc.graphics.Pixmap;
import arc.graphics.Pixmap.Format;
import arc.graphics.PixmapIO;
import arc.graphics.TextureData;
import arc.util.ArcRuntimeException;

public class FileTextureData implements TextureData{

    final Fi file;
    int width = 0;
    int height = 0;
    Format format;
    Pixmap pixmap;
    boolean useMipMaps;
    boolean isPrepared = false;

    public FileTextureData(Fi file, Pixmap preloadedPixmap, Format format, boolean useMipMaps){
        this.file = file;
        this.pixmap = preloadedPixmap;
        this.format = format;
        this.useMipMaps = useMipMaps;
        if(pixmap != null){
            width = pixmap.getWidth();
            height = pixmap.getHeight();
            if(format == null) this.format = pixmap.getFormat();
        }
    }

    @Override
    public boolean isPrepared(){
        return isPrepared;
    }

    @Override
    public void prepare(){
        if(isPrepared) throw new ArcRuntimeException("Already prepared");
        if(pixmap == null){
            if(file.extension().equals("cim"))
                pixmap = PixmapIO.readCIM(file);
            else
                pixmap = new Pixmap(file);
            width = pixmap.getWidth();
            height = pixmap.getHeight();
            if(format == null) format = pixmap.getFormat();
        }
        isPrepared = true;
    }

    @Override
    public Pixmap consumePixmap(){
        if(!isPrepared) throw new ArcRuntimeException("Call prepare() before calling getPixmap()");
        isPrepared = false;
        Pixmap pixmap = this.pixmap;
        this.pixmap = null;
        return pixmap;
    }

    @Override
    public boolean disposePixmap(){
        return true;
    }

    @Override
    public int getWidth(){
        return width;
    }

    @Override
    public int getHeight(){
        return height;
    }

    @Override
    public Format getFormat(){
        return format;
    }

    @Override
    public boolean useMipMaps(){
        return useMipMaps;
    }

    @Override
    public boolean isManaged(){
        return true;
    }

    public Fi getFileHandle(){
        return file;
    }

    @Override
    public TextureDataType getType(){
        return TextureDataType.Pixmap;
    }

    @Override
    public void consumeCustomData(int target){
        throw new ArcRuntimeException("This TextureData implementation does not upload data itself");
    }

    public String toString(){
        return file.toString();
    }
}
