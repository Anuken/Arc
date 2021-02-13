package arc.graphics;

import arc.files.Fi;
import arc.graphics.gl.FileTextureArrayData;

/**
 * Used by a {@link TextureArray} to load the pixel data. The TextureArray will request the TextureArrayData to prepare itself through
 * {@link #prepare()} and upload its data using {@link #consumeTextureArrayData()}. These are the first methods to be called by TextureArray.
 * After that the TextureArray will invoke the other methods to find out about the size of the image data, the format, whether the
 * TextureArrayData is able to manage the pixel data if the OpenGL ES context is lost.</p>
 * <p>
 * Before a call to either {@link #consumeTextureArrayData()}, TextureArray will bind the OpenGL ES texture.</p>
 * <p>
 * Look at {@link FileTextureArrayData} for example implementation of this interface.
 * @author Tomski
 */
public interface TextureArrayData{

    /** @return whether the TextureArrayData is prepared or not. */
    boolean isPrepared();

    /**
     * Prepares the TextureArrayData for a call to {@link #consumeTextureArrayData()}. This method can be called from a non OpenGL thread and
     * should thus not interact with OpenGL.
     */
    void prepare();

    /**
     * Uploads the pixel data of the TextureArray layers of the TextureArray to the OpenGL ES texture. The caller must bind an OpenGL ES texture. A
     * call to {@link #prepare()} must preceed a call to this method. Any internal data structures created in {@link #prepare()}
     * should be disposed of here.
     */
    void consumeTextureArrayData();

    /** @return the width of this TextureArray */
    int getWidth();

    /** @return the height of this TextureArray */
    int getHeight();

    /** @return the layer count of this TextureArray */
    int getDepth();

    /** @return whether this implementation can cope with a EGL context loss. */
    boolean isManaged();

    /** @return the internal format of this TextureArray */
    int getInternalFormat();

    /** @return the GL type of this TextureArray */
    int getGLType();

    /**
     * Provides static method to instantiate the right implementation.
     * @author Tomski
     */
    class TextureArrayFactory{

        public static TextureArrayData loadFromFiles(Pixmap.Format format, boolean useMipMaps, Fi... files){
            return new FileTextureArrayData(format, useMipMaps, files);
        }

    }

}
