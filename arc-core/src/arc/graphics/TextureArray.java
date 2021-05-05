package arc.graphics;

import arc.*;
import arc.files.*;
import arc.graphics.gl.*;
import arc.util.*;

/**
 * OpenGL ES wrapper for TextureArray
 * @author Tomski
 */
public class TextureArray extends GLTexture{
    private TextureArrayData data;

    public TextureArray(String... internalPaths){
        this(getInternalHandles(internalPaths));
    }

    public TextureArray(Fi... files){
        this(false, files);
    }

    public TextureArray(boolean useMipMaps, Fi... files){
        this(new FileTextureArrayData(useMipMaps, files));
    }

    public TextureArray(TextureArrayData data){
        super(GL30.GL_TEXTURE_2D_ARRAY, Gl.genTexture());

        if(Core.gl30 == null){
            throw new ArcRuntimeException("TextureArray requires a device running with GLES 3.0 compatibilty");
        }

        load(data);
    }

    private static Fi[] getInternalHandles(String... internalPaths){
        Fi[] handles = new Fi[internalPaths.length];
        for(int i = 0; i < internalPaths.length; i++){
            handles[i] = Core.files.internal(internalPaths[i]);
        }
        return handles;
    }

    private void load(TextureArrayData data){
        this.data = data;
        this.width = data.getWidth();
        this.height = data.getHeight();

        bind();
        Core.gl30.glTexImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, data.getInternalFormat(), data.getWidth(), data.getHeight(), data.getDepth(), 0, data.getInternalFormat(), data.getGLType(), null);

        if(!data.isPrepared()) data.prepare();

        data.consumeTextureArrayData();

        setFilter(minFilter, magFilter);
        setWrap(uWrap, vWrap);
        Gl.bindTexture(glTarget, 0);
    }

    @Override
    public int getDepth(){
        return data.getDepth();
    }

}
