package io.anuke.arc.assets.loaders;

import io.anuke.arc.assets.AssetDescriptor;
import io.anuke.arc.assets.AssetLoaderParameters;
import io.anuke.arc.assets.AssetManager;
import io.anuke.arc.assets.loaders.TextureLoader.TextureParameter;
import io.anuke.arc.collection.Array;
import io.anuke.arc.files.Fi;
import io.anuke.arc.graphics.Texture;
import io.anuke.arc.graphics.g2d.TextureAtlas;
import io.anuke.arc.graphics.g2d.TextureAtlas.TextureAtlasData;
import io.anuke.arc.graphics.g2d.TextureAtlas.TextureAtlasData.Page;

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
        for(Page page : data.getPages()){
            page.texture = assetManager.get(page.textureFile.path().replaceAll("\\\\", "/"), Texture.class);
        }

        TextureAtlas atlas = new TextureAtlas(data);
        data = null;
        return atlas;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, Fi atlasFile, TextureAtlasParameter parameter){
        Fi imgDir = atlasFile.parent();

        if(parameter != null)
            data = new TextureAtlasData(atlasFile, imgDir, parameter.flip);
        else{
            data = new TextureAtlasData(atlasFile, imgDir, false);
        }

        Array<AssetDescriptor> dependencies = new Array<>();
        for(Page page : data.getPages()){
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
