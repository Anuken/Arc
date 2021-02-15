package arc.graphics.gl;

import arc.Core;
import arc.files.Fi;
import arc.graphics.GL30;
import arc.graphics.Pixmap;
import arc.graphics.Pixmap.*;
import arc.graphics.TextureArrayData;
import arc.graphics.TextureData;
import arc.graphics.TextureData.*;
import arc.util.ArcRuntimeException;

/** @author Tomski **/
public class FileTextureArrayData implements TextureArrayData{

    boolean useMipMaps;
    private TextureData[] textureDatas;
    private boolean prepared;
    private Pixmap.Format format;
    private int depth;

    public FileTextureArrayData(Pixmap.Format format, boolean useMipMaps, Fi[] files){
        this.format = format;
        this.useMipMaps = useMipMaps;
        this.depth = files.length;
        textureDatas = new TextureData[files.length];
        for(int i = 0; i < files.length; i++){
            textureDatas[i] = TextureDataFactory.loadFromFile(files[i], format, useMipMaps);
        }
    }

    @Override
    public boolean isPrepared(){
        return prepared;
    }

    @Override
    public void prepare(){
        int width = -1;
        int height = -1;
        for(TextureData data : textureDatas){
            data.prepare();
            if(width == -1){
                width = data.getWidth();
                height = data.getHeight();
                continue;
            }
            if(width != data.getWidth() || height != data.getHeight()){
                throw new ArcRuntimeException("Error whilst preparing TextureArray: TextureArray Textures must have equal dimensions.");
            }
        }
        prepared = true;
    }

    @Override
    public void consumeTextureArrayData(){
        for(int i = 0; i < textureDatas.length; i++){
            if(textureDatas[i].getType() == TextureData.TextureDataType.custom){
                textureDatas[i].consumeCustomData(GL30.GL_TEXTURE_2D_ARRAY);
            }else{
                TextureData texData = textureDatas[i];
                Pixmap pixmap = texData.consumePixmap();
                boolean disposePixmap = texData.disposePixmap();
                if(texData.getFormat() != pixmap.getFormat()){
                    Pixmap temp = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), texData.getFormat());
                    temp.setBlending(Blending.none);
                    temp.drawPixmap(pixmap, 0, 0, 0, 0, pixmap.getWidth(), pixmap.getHeight());
                    if(texData.disposePixmap()){
                        pixmap.dispose();
                    }
                    pixmap = temp;
                    disposePixmap = true;
                }
                Core.gl30.glTexSubImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, 0, 0, i, pixmap.getWidth(), pixmap.getHeight(), 1, pixmap.getGLInternalFormat(), pixmap.getGLType(), pixmap.getPixels());
                if(disposePixmap) pixmap.dispose();
            }
        }
    }

    @Override
    public int getWidth(){
        return textureDatas[0].getWidth();
    }

    @Override
    public int getHeight(){
        return textureDatas[0].getHeight();
    }

    @Override
    public int getDepth(){
        return depth;
    }

    @Override
    public int getInternalFormat(){
        return format.toGlFormat();
    }

    @Override
    public int getGLType(){
        return format.toGlType();
    }

    @Override
    public boolean isManaged(){
        for(TextureData data : textureDatas){
            if(!data.isManaged()){
                return false;
            }
        }
        return true;
    }
}
