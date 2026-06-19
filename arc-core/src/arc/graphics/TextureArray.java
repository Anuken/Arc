package arc.graphics;

import arc.*;
import arc.files.*;

/**
 * OpenGL ES wrapper for TextureArray
 * @author Tomski
 */
public class TextureArray extends GLTexture{
    public int depth;

    public TextureArray(){
        super(GL30.GL_TEXTURE_2D_ARRAY, Gl.genTexture());
    }

    public TextureArray(Pixmap[] pixmaps){
        this();

        load(pixmaps, false);
    }

    public TextureArray(Fi[] files, boolean useMipMaps){
        this();

        load(files, useMipMaps);
    }

    public TextureArray(int width, int height, int depth){
        this();
        init(width, height, depth);
    }

    void init(int width, int height, int depth){
        this.width = width;
        this.height = height;
        this.depth = depth;

        bind();
        Core.gl30.glTexImage3D(glTarget, 0, GL30.GL_RGBA8, width, height, depth, 0,  GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, null);
        setFilter(minFilter, magFilter);
        setWrap(uWrap, vWrap);
    }

    public void load(Fi[] files, boolean useMipMaps){
        for(int i = 0; i < files.length; i++){
            Pixmap pix = new Pixmap(files[i]);
            if(width == 0) init(pix.width, pix.height, files.length);

            load(pix, i);
            pix.dispose();
        }

        if(useMipMaps) Gl.generateMipmap(glTarget);
    }

    public void load(Pixmap[] pixmaps, boolean useMipmaps){
        init(pixmaps[0].width, pixmaps[0].height, pixmaps.length);

        for(int i = 0; i < pixmaps.length; i++){
            Pixmap pixmap = pixmaps[i];
            Core.gl30.glTexSubImage3D(glTarget, 0, 0, 0, i, pixmap.width, pixmap.height, 1, pixmap.getGLInternalFormat(), pixmap.getGLType(), pixmap.pixels);
        }

        if(useMipmaps) Gl.generateMipmap(glTarget);
    }

    public void load(Pixmap pixmap, int depth){
        bind();
        Core.gl30.glTexSubImage3D(glTarget, 0, 0, 0, depth, pixmap.width, pixmap.height, 1, pixmap.getGLInternalFormat(), pixmap.getGLType(), pixmap.pixels);
    }

    @Override
    public int getDepth(){
        return depth;
    }

}
