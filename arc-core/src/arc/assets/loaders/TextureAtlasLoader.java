package arc.assets.loaders;

import arc.assets.AssetDescriptor;
import arc.assets.AssetLoaderParameters;
import arc.assets.AssetManager;
import arc.assets.loaders.TextureLoader.TextureParameter;
import arc.struct.Seq;
import arc.files.Fi;
import arc.graphics.Texture;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureAtlas.TextureAtlasData;
import arc.graphics.g2d.TextureAtlas.TextureAtlasData.AtlasPage;

/**
 * {@link AssetLoader} to load {@link TextureAtlas} instances. Passing a {@link TextureAtlasParameter} to
 * {@link AssetManager#load(String, Class, AssetLoaderParameters)} allows to specify whether the atlas regions should be flipped
 * on the y-axis or not.
 * @author mzechner
 */
public class TextureAtlasLoader extends SynchronousAssetLoader<TextureAtlas, TextureAtlasLoader.TextureAtlasParameter>{
    TextureAtlasData data;

    public TextureAtlasLoader(FileHandleResolver resolver){
        super(resolver);
    }

    @Override
    public TextureAtlas load(AssetManager assetManager, String fileName, Fi file, TextureAtlasParameter parameter){
        for(AtlasPage page : data.getPages()){
            page.texture = assetManager.get(page.textureFile.path().replaceAll("\\\\", "/"), Texture.class);
        }

        TextureAtlas atlas = new TextureAtlas(data);
        data = null;
        return atlas;
    }

    @Override
    public Seq<AssetDescriptor> getDependencies(String fileName, Fi atlasFile, TextureAtlasParameter parameter){
        Fi imgDir = atlasFile.parent();

        if(parameter != null)
            data = new TextureAtlasData(atlasFile, imgDir, parameter.flip);
        else{
            data = new TextureAtlasData(atlasFile, imgDir, false);
        }

        Seq<AssetDescriptor> dependencies = new Seq<>();
        for(AtlasPage page : data.getPages()){
            TextureParameter params = new TextureParameter();
            params.format = page.format;
            params.genMipMaps = page.useMipMaps;
            params.minFilter = page.minFilter;
            params.magFilter = page.magFilter;
            dependencies.add(new AssetDescriptor<>(page.textureFile, Texture.class, params));
        }
        return dependencies;
    }

    public static class TextureAtlasParameter extends AssetLoaderParameters<TextureAtlas>{
        /** whether to flip the texture atlas vertically **/
        public boolean flip = false;

        public TextureAtlasParameter(){
        }

        public TextureAtlasParameter(boolean flip){
            this.flip = flip;
        }

        public TextureAtlasParameter(LoadedCallback loadedCallback){
            super(loadedCallback);
        }
    }
}
