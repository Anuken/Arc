package arc.graphics.gl;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.util.*;

/** @author Tomski **/
public class FileTextureArrayData implements TextureArrayData{

    boolean useMipMaps;
    private TextureData[] textureDatas;
    private boolean prepared;
    private int depth;

    public FileTextureArrayData(boolean useMipMaps, Fi[] files){
        this.useMipMaps = useMipMaps;
        this.depth = files.length;
        textureDatas = new TextureData[files.length];
        for(int i = 0; i < files.length; i++){
            textureDatas[i] = TextureData.load(files[i], useMipMaps);
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
            if(textureDatas[i].isCustom()){
                textureDatas[i].consumeCustomData(GL30.GL_TEXTURE_2D_ARRAY);
            }else{
                TextureData texData = textureDatas[i];
                Pixmap pixmap = texData.consumePixmap();
                boolean disposePixmap = texData.disposePixmap();
                Core.gl30.glTexSubImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, 0, 0, i, pixmap.width, pixmap.height, 1, pixmap.getGLInternalFormat(), pixmap.getGLType(), pixmap.pixels);
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
        return Gl.unsignedByte;
    }

    @Override
    public int getGLType(){
        return Gl.rgba;
    }

}
