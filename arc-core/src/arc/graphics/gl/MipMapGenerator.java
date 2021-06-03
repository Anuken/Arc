package arc.graphics.gl;

import arc.*;
import arc.graphics.*;
import arc.util.*;

public class MipMapGenerator{

    private static boolean useHWMipMap = true;

    private MipMapGenerator(){
        // disallow, static methods only
    }

    public static void setUseHardwareMipMap(boolean useHWMipMap){
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

        if(Core.app.isAndroid() || Core.app.isWeb() || Core.app.isIOS()){
            generateMipMapGLES20(target, pixmap);
        }else{
            generateMipMapDesktop(target, pixmap, textureWidth, textureHeight);
        }
    }

    private static void generateMipMapGLES20(int target, Pixmap pixmap){
        Gl.texImage2D(target, 0, pixmap.getGLInternalFormat(), pixmap.width, pixmap.height, 0,
        pixmap.getGLFormat(), pixmap.getGLType(), pixmap.pixels);
        Gl.generateMipmap(target);
    }

    private static void generateMipMapDesktop(int target, Pixmap pixmap, int textureWidth, int textureHeight){
        if(Core.graphics.supportsExtension("GL_ARB_framebuffer_object")
        || Core.graphics.supportsExtension("GL_EXT_framebuffer_object") || Core.gl30 != null){
            Gl.texImage2D(target, 0, pixmap.getGLInternalFormat(), pixmap.width, pixmap.height, 0,
            pixmap.getGLFormat(), pixmap.getGLType(), pixmap.pixels);
            Gl.generateMipmap(target);
        }else{
            generateMipMapCPU(target, pixmap, textureWidth, textureHeight);
        }
    }

    private static void generateMipMapCPU(int target, Pixmap pixmap, int textureWidth, int textureHeight){
        Gl.texImage2D(target, 0, pixmap.getGLInternalFormat(), pixmap.width, pixmap.height, 0,
        pixmap.getGLFormat(), pixmap.getGLType(), pixmap.pixels);
        if((Core.gl20 == null) && textureWidth != textureHeight)
            throw new ArcRuntimeException("texture width and height must be square when using mipmapping.");
        int width = pixmap.width / 2;
        int height = pixmap.height / 2;
        int level = 1;
        while(width > 0 && height > 0){
            Pixmap tmp = new Pixmap(width, height);
            tmp.draw(pixmap, 0, 0, pixmap.width, pixmap.height, 0, 0, width, height);
            if(level > 1) pixmap.dispose();
            pixmap = tmp;

            Gl.texImage2D(target, level, pixmap.getGLInternalFormat(), pixmap.width, pixmap.height, 0,
            pixmap.getGLFormat(), pixmap.getGLType(), pixmap.pixels);

            width = pixmap.width / 2;
            height = pixmap.height / 2;
            level++;
        }
    }
}
