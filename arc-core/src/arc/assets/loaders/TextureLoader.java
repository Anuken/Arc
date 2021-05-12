package arc.assets.loaders;

import arc.assets.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.struct.*;

/**
 * {@link AssetLoader} for {@link Texture} instances. The pixel data is loaded asynchronously. The texture is then created on the
 * rendering thread, synchronously. Passing a {@link TextureParameter} to
 * {@link AssetManager#load(String, Class, AssetLoaderParameters)} allows one to specify parameters as can be passed to the
 * various Texture constructors, e.g. filtering, whether to generate mipmaps and so on.
 * @author mzechner
 */
public class TextureLoader extends AsynchronousAssetLoader<Texture, TextureLoader.TextureParameter>{
    TextureLoaderInfo info = new TextureLoaderInfo();

    public TextureLoader(FileHandleResolver resolver){
        super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, Fi file, TextureParameter parameter){
        info.filename = fileName;
        if(parameter == null || parameter.textureData == null){
            boolean genMipMaps = false;
            info.texture = null;

            if(parameter != null){
                genMipMaps = parameter.genMipMaps;
                info.texture = parameter.texture;
            }

            info.data = TextureData.load(file, genMipMaps);
        }else{
            info.data = parameter.textureData;
            info.texture = parameter.texture;
        }
        if(!info.data.isPrepared()) info.data.prepare();
    }

    @Override
    public Texture loadSync(AssetManager manager, String fileName, Fi file, TextureParameter parameter){
        if(info == null) return null;
        Texture texture = info.texture;
        if(texture != null){
            texture.load(info.data);
        }else{
            texture = new Texture(info.data);
        }
        if(parameter != null){
            texture.setFilter(parameter.minFilter, parameter.magFilter);
            texture.setWrap(parameter.wrapU, parameter.wrapV);
        }
        return texture;
    }

    @Override
    public Seq<AssetDescriptor> getDependencies(String fileName, Fi file, TextureParameter parameter){
        return null;
    }

    public static class TextureLoaderInfo{
        String filename;
        TextureData data;
        Texture texture;
    }

    public static class TextureParameter extends AssetLoaderParameters<Texture>{
        /** whether to generate mipmaps **/
        public boolean genMipMaps = false;
        /** The texture to put the {@link TextureData} in, optional. **/
        public Texture texture = null;
        /** TextureData for textures created on the fly, optional. When set, all format and genMipMaps are ignored */
        public TextureData textureData = null;
        public TextureFilter minFilter = TextureFilter.nearest;
        public TextureFilter magFilter = TextureFilter.nearest;
        public TextureWrap wrapU = TextureWrap.clampToEdge;
        public TextureWrap wrapV = TextureWrap.clampToEdge;

        public TextureParameter(){
        }
    }
}
