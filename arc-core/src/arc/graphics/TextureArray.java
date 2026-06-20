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

    /**
     * Resizes the texture array to the new depth, preserving existing layers via FBO blit.
     */
    public void resizeDepth(int newDepth){
        if(newDepth == depth) return;

        //this is extremely slow and requires a 2-pass solution to keep the handle the same.
        int newTex = Gl.genTexture();
        Core.gl30.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, newTex);
        Core.gl30.glTexImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, GL30.GL_RGBA8, width, height, depth, 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, null);

        int readFbo = Core.gl30.glGenFramebuffer(), drawFbo = Core.gl30.glGenFramebuffer();
        int layersToCopy = Math.min(depth, newDepth);

        for(int layer = 0; layer < layersToCopy; layer++){
            Core.gl30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFbo);
            Core.gl30.glFramebufferTextureLayer(GL30.GL_READ_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, glHandle, 0, layer);

            Core.gl30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFbo);
            Core.gl30.glFramebufferTextureLayer(GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, newTex, 0, layer);

            Core.gl30.glBlitFramebuffer(
            0, 0, width, height,
            0, 0, width, height,
            GL20.GL_COLOR_BUFFER_BIT, GL20.GL_NEAREST);
        }

        Core.gl30.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, glHandle);
        Core.gl30.glTexImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, GL30.GL_RGBA8, width, height, newDepth, 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, null);

        for(int layer = 0; layer < layersToCopy; layer++){
            Core.gl30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFbo);
            Core.gl30.glFramebufferTextureLayer(GL30.GL_READ_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, newTex, 0, layer);

            Core.gl30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFbo);
            Core.gl30.glFramebufferTextureLayer(GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, glHandle, 0, layer);

            Core.gl30.glBlitFramebuffer(
            0, 0, width, height,
            0, 0, width, height,
            GL20.GL_COLOR_BUFFER_BIT, GL20.GL_NEAREST);
        }

        Core.gl30.glDeleteFramebuffer(readFbo);
        Core.gl30.glDeleteFramebuffer(drawFbo);
        Gl.deleteTexture(newTex);

        depth = newDepth;

        bind();
        setFilter(minFilter, magFilter);
        setWrap(uWrap, vWrap);
    }

    @Override
    public int getDepth(){
        return depth;
    }

}
