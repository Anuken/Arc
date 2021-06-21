package arc.assets.loaders;

import arc.assets.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.Pixmap.*;
import arc.graphics.Texture.*;
import arc.struct.*;

/**
 * {@link AssetLoader} for {@link Cubemap} instances. The pixel data is loaded asynchronously. The texture is then created on the
 * rendering thread, synchronously. Passing a {@link CubemapParameter} to
 * {@link AssetManager#load(String, Class, AssetLoaderParameters)} allows one to specify parameters as can be passed to the
 * various Cubemap constructors, e.g. filtering and so on.
 * @author mzechner, Vincent Bousquet
 */
public class CubemapLoader extends AsynchronousAssetLoader<Cubemap, CubemapLoader.CubemapParameter>{
    CubemapLoaderInfo info = new CubemapLoaderInfo();

    public CubemapLoader(FileHandleResolver resolver){
        super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, Fi file, CubemapParameter parameter){
        info.filename = fileName;
        if(parameter == null || parameter.cubemapData == null){
            info.cubemap = null;

            if(parameter != null){
                info.cubemap = parameter.cubemap;
            }
        }else{
            info.data = parameter.cubemapData;
            info.cubemap = parameter.cubemap;
        }
        if(!info.data.isPrepared()) info.data.prepare();
    }

    @Override
    public Cubemap loadSync(AssetManager manager, String fileName, Fi file, CubemapParameter parameter){
        if(info == null) return null;
        Cubemap cubemap = info.cubemap;
        if(cubemap != null){
            cubemap.load(info.data);
        }else{
            cubemap = new Cubemap(info.data);
        }
        if(parameter != null){
            cubemap.setFilter(parameter.minFilter, parameter.magFilter);
            cubemap.setWrap(parameter.wrapU, parameter.wrapV);
        }
        return cubemap;
    }

    @Override
    public Seq<AssetDescriptor> getDependencies(String fileName, Fi file, CubemapParameter parameter){
        return null;
    }

    public static class CubemapLoaderInfo{
        String filename;
        CubemapData data;
        Cubemap cubemap;
    }

    public static class CubemapParameter extends AssetLoaderParameters<Cubemap>{
        /** the format of the final Texture. Uses the source images format if null **/
        public Format format = null;
        /** The texture to put the {@link TextureData} in, optional. **/
        public Cubemap cubemap = null;
        /** CubemapData for textures created on the fly, optional. When set, all format and genMipMaps are ignored */
        public CubemapData cubemapData = null;
        public TextureFilter minFilter = TextureFilter.nearest;
        public TextureFilter magFilter = TextureFilter.nearest;
        public TextureWrap wrapU = TextureWrap.clampToEdge;
        public TextureWrap wrapV = TextureWrap.clampToEdge;
    }
}
