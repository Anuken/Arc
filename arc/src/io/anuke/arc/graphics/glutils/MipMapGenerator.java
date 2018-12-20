package io.anuke.arc.graphics.glutils;

import io.anuke.arc.Application.ApplicationType;
import io.anuke.arc.Core;
import io.anuke.arc.graphics.GL20;
import io.anuke.arc.graphics.Pixmap;
import io.anuke.arc.graphics.Pixmap.Blending;
import io.anuke.arc.graphics.Texture;
import io.anuke.arc.utils.ArcRuntimeException;

public class MipMapGenerator{

    private static boolean useHWMipMap = true;

    private MipMapGenerator(){
        // disallow, static methods only
    }

    static public void setUseHardwareMipMap(boolean useHWMipMap){
        MipMapGenerator.useHWMipMap = useHWMipMap;
    }

    /**
     * Sets the image data of the {@link Texture} based on the {@link Pixmap}. The texture must be bound for this to work. If
     * <code>disposePixmap</code> is true, the pixmap will be disposed at the end of the method.
     * @param pixmap the Pixmap
     */
    public static void generateMipMap(Pixmap pixmap, int textureWidth, int textureHeight){
        generateMipMap(GL20.GL_TEXTURE_2D, pixmap, textureWidth, textureHeight);
    }

    /**
     * Sets the image data of the {@link Texture} based on the {@link Pixmap}. The texture must be bound for this to work. If
     * <code>disposePixmap</code> is true, the pixmap will be disposed at the end of the method.
     */
    public static void generateMipMap(int target, Pixmap pixmap, int textureWidth, int textureHeight){
        if(!useHWMipMap){
            generateMipMapCPU(target, pixmap, textureWidth, textureHeight);
            return;
        }

        if(Core.app.getType() == ApplicationType.Android || Core.app.getType() == ApplicationType.WebGL
        || Core.app.getType() == ApplicationType.iOS){
            generateMipMapGLES20(target, pixmap);
        }else{
            generateMipMapDesktop(target, pixmap, textureWidth, textureHeight);
        }
    }

    private static void generateMipMapGLES20(int target, Pixmap pixmap){
        Core.gl.glTexImage2D(target, 0, pixmap.getGLInternalFormat(), pixmap.getWidth(), pixmap.getHeight(), 0,
        pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
        Core.gl20.glGenerateMipmap(target);
    }

    private static void generateMipMapDesktop(int target, Pixmap pixmap, int textureWidth, int textureHeight){
        if(Core.graphics.supportsExtension("GL_ARB_framebuffer_object")
        || Core.graphics.supportsExtension("GL_EXT_framebuffer_object") || Core.gl30 != null){
            Core.gl.glTexImage2D(target, 0, pixmap.getGLInternalFormat(), pixmap.getWidth(), pixmap.getHeight(), 0,
            pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
            Core.gl20.glGenerateMipmap(target);
        }else{
            generateMipMapCPU(target, pixmap, textureWidth, textureHeight);
        }
    }

    private static void generateMipMapCPU(int target, Pixmap pixmap, int textureWidth, int textureHeight){
        Core.gl.glTexImage2D(target, 0, pixmap.getGLInternalFormat(), pixmap.getWidth(), pixmap.getHeight(), 0,
        pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
        if((Core.gl20 == null) && textureWidth != textureHeight)
            throw new ArcRuntimeException("texture width and height must be square when using mipmapping.");
        int width = pixmap.getWidth() / 2;
        int height = pixmap.getHeight() / 2;
        int level = 1;
        while(width > 0 && height > 0){
            Pixmap tmp = new Pixmap(width, height, pixmap.getFormat());
            tmp.setBlending(Blending.None);
            tmp.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(), 0, 0, width, height);
            if(level > 1) pixmap.dispose();
            pixmap = tmp;

            Core.gl.glTexImage2D(target, level, pixmap.getGLInternalFormat(), pixmap.getWidth(), pixmap.getHeight(), 0,
            pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());

            width = pixmap.getWidth() / 2;
            height = pixmap.getHeight() / 2;
            level++;
        }
    }
}
