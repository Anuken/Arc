package arc.graphics;

import arc.files.Fi;
import arc.graphics.Pixmap.Format;
import arc.graphics.gl.FileTextureData;
import arc.graphics.gl.MipMapGenerator;

/**
 * Used by a {@link Texture} to load the pixel data. A TextureData can either return a {@link Pixmap} or upload the pixel data
 * itself. It signals it's type via {@link #isCustom()} to the Texture that's using it. The Texture will then either invoke
 * {@link #consumePixmap()} or {@link #consumeCustomData(int)}. These are the first methods to be called by Texture. After that
 * the Texture will invoke the other methods to find out about the size of the image data, the format, whether mipmaps should be
 * generated and whether the TextureData is able to manage the pixel data if the OpenGL ES context is lost.</p>
 * <p>
 * In case the TextureData implementation is custom, the implementation has to generate the
 * mipmaps itself if necessary. See {@link MipMapGenerator}.</p>
 * <p>
 * Before a call to either {@link #consumePixmap()} or {@link #consumeCustomData(int)}, Texture will bind the OpenGL ES
 * texture.</p>
 * <p>
 * Look at {@link FileTextureData} for example implementations of this interface.
 * @author mzechner
 */
public interface TextureData{

    /** @return whether to use #consumeCustomData */
    boolean isCustom();

    /** @return whether the TextureData is prepared or not. */
    boolean isPrepared();

    /**
     * Prepares the TextureData for a call to {@link #consumePixmap()} or {@link #consumeCustomData(int)}. This method can be
     * called from a non OpenGL thread and should thus not interact with OpenGL.
     */
    void prepare();

    /**
     * Returns the {@link Pixmap} for upload by Texture. A call to {@link #prepare()} must precede a call to this method. Any
     * internal data structures created in {@link #prepare()} should be disposed of here.
     * @return the pixmap.
     */
    Pixmap consumePixmap();

    default Pixmap getPixmap(){
        if(!isPrepared()){
            prepare();
        }
        return consumePixmap();
    }

    /** @return whether the caller of {@link #consumePixmap()} should dispose the Pixmap returned by {@link #consumePixmap()} */
    boolean disposePixmap();

    /**
     * Uploads the pixel data to the OpenGL ES texture. The caller must bind an OpenGL ES texture. A call to {@link #prepare()}
     * must precede a call to this method. Any internal data structures created in {@link #prepare()} should be disposed of here.
     */
    void consumeCustomData(int target);

    /** @return the width of the pixel data */
    int getWidth();

    /** @return the height of the pixel data */
    int getHeight();

    /** @return the {@link Format} of the pixel data */
    Format getFormat();

    /** @return whether to generate mipmaps or not. */
    boolean useMipMaps();

    static TextureData load(Fi file, boolean useMipMaps){
        return new FileTextureData(file,
            file.extEquals("apix") ? PixmapIO.readApix(file) :
            new Pixmap(file),
        useMipMaps);
    }

}
