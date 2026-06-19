package arc.graphics.gl;

import arc.*;
import arc.graphics.*;

/**
 * This class exists to wrap Texture in a way that can be used in TextureRegions, where it actually refers to a specific level of a texture array.
 * This is used in batches, and needs special instanceof checks to work properly. It doesn't have a real handle. The whole abstraction falls apart, really.
 * */
public class ArraySliceTexture extends Texture{
    public final TextureArray array;
    public final int index;

    public ArraySliceTexture(TextureArray array, int index){
        super(GL30.GL_TEXTURE_2D_ARRAY, array.getTextureObjectHandle());
        if(index >= array.getDepth()) throw new IllegalArgumentException("Array slice texture index out of bounds: " + index + " >= " + array.getDepth());

        this.array = array;
        this.index = index;
        this.width = array.width;
        this.height = array.height;
    }

    @Override
    public void draw(Pixmap pixmap, int x, int y){
        bind();
        Core.gl30.glTexSubImage3D(glTarget, 0, x, y, index, pixmap.width, pixmap.height, 1, pixmap.getGLFormat(), pixmap.getGLType(), pixmap.pixels);
    }

    @Override
    public void bind(){
        array.bind();
    }

    @Override
    public void bind(int unit){
        array.bind(unit);
    }

    @Override
    public int getDepth(){
        return index;
    }

    @Override
    public void dispose(){
        //slices shouldn't need to be disposed, dispose the whole array instead
    }
}
