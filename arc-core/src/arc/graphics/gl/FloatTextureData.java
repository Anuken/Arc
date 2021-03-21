package arc.graphics.gl;

import arc.*;
import arc.graphics.*;
import arc.graphics.Pixmap.*;
import arc.graphics.gl.GLVersion.*;
import arc.util.*;

import java.nio.*;

/** A {@link TextureData} implementation which should be used to create float textures. */
public class FloatTextureData implements TextureData{

    int width;
    int height;

    int internalFormat;
    int format;
    int type;

    boolean isGpuOnly;

    boolean isPrepared = false;
    FloatBuffer buffer;

    public FloatTextureData(int w, int h, int internalFormat, int format, int type, boolean isGpuOnly){
        this.width = w;
        this.height = h;
        this.internalFormat = internalFormat;
        this.format = format;
        this.type = type;
        this.isGpuOnly = isGpuOnly;
    }

    @Override
    public boolean isCustom(){
        return true;
    }

    @Override
    public boolean isPrepared(){
        return isPrepared;
    }

    @Override
    public void prepare(){
        if(isPrepared) throw new ArcRuntimeException("Already prepared");
        if(!isGpuOnly){
            int amountOfFloats = 4;
            if(Core.graphics.getGLVersion().type.equals(GlType.OpenGL)){
                if(internalFormat == GL30.GL_RGBA16F || internalFormat == GL30.GL_RGBA32F) amountOfFloats = 4;
                if(internalFormat == GL30.GL_RGB16F || internalFormat == GL30.GL_RGB32F) amountOfFloats = 3;
                if(internalFormat == GL30.GL_RG16F || internalFormat == GL30.GL_RG32F) amountOfFloats = 2;
                if(internalFormat == GL30.GL_R16F || internalFormat == GL30.GL_R32F) amountOfFloats = 1;
            }
            this.buffer = Buffers.newFloatBuffer(width * height * amountOfFloats);
        }
        isPrepared = true;
    }

    @Override
    public void consumeCustomData(int target){
        if(Core.app.isAndroid() || Core.app.isIOS() || Core.app.isWeb()){

            if(!Core.graphics.supportsExtension("OES_texture_float"))
                throw new ArcRuntimeException("Extension OES_texture_float not supported!");

            // GLES and WebGL defines texture format by 3rd and 8th argument,
            // so to get a float texture one needs to supply GL_RGBA and GL_FLOAT there.
            Gl.texImage2D(target, 0, GL20.GL_RGBA, width, height, 0, GL20.GL_RGBA, GL20.GL_FLOAT, buffer);

        }else{
            if(!Core.graphics.isGL30Available()){
                if(!Core.graphics.supportsExtension("GL_ARB_texture_float"))
                    throw new ArcRuntimeException("Extension GL_ARB_texture_float not supported!");
            }
            // in desktop OpenGL the texture format is defined only by the third argument,
            // hence we need to use GL_RGBA32F there (this constant is unavailable in GLES/WebGL)
            Gl.texImage2D(target, 0, internalFormat, width, height, 0, format, GL20.GL_FLOAT, buffer);
        }
    }

    @Override
    public Pixmap consumePixmap(){
        throw new ArcRuntimeException("This TextureData implementation does not return a Pixmap");
    }

    @Override
    public boolean disposePixmap(){
        throw new ArcRuntimeException("This TextureData implementation does not return a Pixmap");
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
        return Format.rgba8888; // it's not true, but FloatTextureData.getFormat() isn't used anywhere
    }

    @Override
    public boolean useMipMaps(){
        return false;
    }

    public FloatBuffer getBuffer(){
        return buffer;
    }
}
