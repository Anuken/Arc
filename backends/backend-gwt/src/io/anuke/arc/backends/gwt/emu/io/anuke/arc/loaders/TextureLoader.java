package io.anuke.arc.assets.loaders;

import io.anuke.arc.assets.AssetDescriptor;
import io.anuke.arc.assets.AssetLoaderParameters;
import io.anuke.arc.assets.AssetManager;
import io.anuke.arc.collection.Array;
import io.anuke.arc.files.FileHandle;
import io.anuke.arc.graphics.Pixmap;
import io.anuke.arc.graphics.Pixmap.Format;
import io.anuke.arc.graphics.Texture;
import io.anuke.arc.graphics.Texture.TextureFilter;
import io.anuke.arc.graphics.Texture.TextureWrap;
import io.anuke.arc.graphics.TextureData;
import io.anuke.arc.graphics.glutils.FileTextureData;

public class TextureLoader extends AsynchronousAssetLoader<Texture, TextureLoader.TextureParameter>{
    TextureData data;
    Texture texture;

    public TextureLoader(FileHandleResolver resolver){
        super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle fileHandle, TextureParameter parameter){
        if(parameter == null || (parameter != null && parameter.textureData == null)){
            Pixmap pixmap = null;
            Format format = null;
            boolean genMipMaps = false;
            texture = null;

            if(parameter != null){
                format = parameter.format;
                genMipMaps = parameter.genMipMaps;
                texture = parameter.texture;
            }

            FileHandle handle = resolve(fileName);
            pixmap = new Pixmap(handle);
            data = new FileTextureData(handle, pixmap, format, genMipMaps);
        }else{
            data = parameter.textureData;
            if(!data.isPrepared()) data.prepare();
            texture = parameter.texture;
        }
    }

    @Override
    public Texture loadSync(AssetManager manager, String fileName, FileHandle fileHandle, TextureParameter parameter){
        Texture texture = this.texture;
        if(texture != null){
            texture.load(data);
        }else{
            texture = new Texture(data);
        }
        if(parameter != null){
            texture.setFilter(parameter.minFilter, parameter.magFilter);
            texture.setWrap(parameter.wrapU, parameter.wrapV);
        }
        return texture;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle fileHandle, TextureParameter parameter){
        return null;
    }

    static public class TextureParameter extends AssetLoaderParameters<Texture>{
        /** the format of the final Texture. Uses the source images format if null **/
        public Format format = null;
        /** whether to generate mipmaps **/
        public boolean genMipMaps = false;
        /** The texture to put the {@link TextureData} in, optional. **/
        public Texture texture = null;
        /** TextureData for textures created on the fly, optional. When set, all format and genMipMaps are ignored */
        public TextureData textureData = null;
        public TextureFilter minFilter = TextureFilter.Nearest;
        public TextureFilter magFilter = TextureFilter.Nearest;
        public TextureWrap wrapU = TextureWrap.ClampToEdge;
        public TextureWrap wrapV = TextureWrap.ClampToEdge;
    }
}
