package io.anuke.arc.assets.loaders;

import io.anuke.arc.assets.AssetDescriptor;
import io.anuke.arc.assets.AssetLoaderParameters;
import io.anuke.arc.assets.AssetManager;
import io.anuke.arc.collection.Array;
import io.anuke.arc.files.Fi;
import io.anuke.arc.graphics.Texture;
import io.anuke.arc.graphics.Texture.TextureFilter;
import io.anuke.arc.graphics.g2d.BitmapFont;
import io.anuke.arc.graphics.g2d.BitmapFont.BitmapFontData;
import io.anuke.arc.graphics.g2d.TextureAtlas;
import io.anuke.arc.graphics.g2d.TextureAtlas.AtlasRegion;
import io.anuke.arc.graphics.g2d.TextureRegion;
import io.anuke.arc.util.ArcRuntimeException;

/**
 * {@link AssetLoader} for {@link BitmapFont} instances. Loads the font description file (.fnt) asynchronously, loads the
 * {@link Texture} containing the glyphs as a dependency. The {@link BitmapFontParameter} allows you to set things like texture
 * filters or whether to flip the glyphs vertically.
 * @author mzechner
 */
public class BitmapFontLoader extends AsynchronousAssetLoader<BitmapFont, BitmapFontLoader.BitmapFontParameter>{
    BitmapFontData data;

    public BitmapFontLoader(FileHandleResolver resolver){
        super(resolver);
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, Fi file, BitmapFontParameter parameter){
        Array<AssetDescriptor> deps = new Array();
        if(parameter != null && parameter.bitmapFontData != null){
            data = parameter.bitmapFontData;
            return deps;
        }

        data = new BitmapFontData(file, parameter != null && parameter.flip);
        if(parameter != null && parameter.atlasName != null){
            deps.add(new AssetDescriptor(parameter.atlasName, TextureAtlas.class));
        }else{
            for(int i = 0; i < data.getImagePaths().length; i++){
                String path = data.getImagePath(i);
                Fi resolved = resolve(path);

                TextureLoader.TextureParameter textureParams = new TextureLoader.TextureParameter();

                if(parameter != null){
                    textureParams.genMipMaps = parameter.genMipMaps;
                    textureParams.minFilter = parameter.minFilter;
                    textureParams.magFilter = parameter.magFilter;
                }

                AssetDescriptor descriptor = new AssetDescriptor(resolved, Texture.class, textureParams);
                deps.add(descriptor);
            }
        }

        return deps;
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, Fi file, BitmapFontParameter parameter){
    }

    @Override
    public BitmapFont loadSync(AssetManager manager, String fileName, Fi file, BitmapFontParameter parameter){
        if(parameter != null && parameter.atlasName != null){
            TextureAtlas atlas = manager.get(parameter.atlasName, TextureAtlas.class);
            String name = file.sibling(data.imagePaths[0]).nameWithoutExtension();
            AtlasRegion region = atlas.find(name);

            if(region == null)
                throw new ArcRuntimeException("Could not find font region " + name + " in atlas " + parameter.atlasName);
            return new BitmapFont(file, region);
        }else{
            int n = data.getImagePaths().length;
            Array<TextureRegion> regs = new Array(n);
            for(int i = 0; i < n; i++){
                regs.add(new TextureRegion(manager.get(data.getImagePath(i), Texture.class)));
            }
            return new BitmapFont(data, regs, true);
        }
    }

    /**
     * Parameter to be passed to {@link AssetManager#load(String, Class, AssetLoaderParameters)} if additional configuration is
     * necessary for the {@link BitmapFont}.
     * @author mzechner
     */
    public static class BitmapFontParameter extends AssetLoaderParameters<BitmapFont>{
        /** Flips the font vertically if {@code true}. Defaults to {@code false}. **/
        public boolean flip = false;

        /** Generates mipmaps for the font if {@code true}. Defaults to {@code false}. **/
        public boolean genMipMaps = false;

        /** The {@link TextureFilter} to use when scaling down the {@link BitmapFont}. Defaults to {@link TextureFilter#Nearest}. */
        public TextureFilter minFilter = TextureFilter.Nearest;

        /** The {@link TextureFilter} to use when scaling up the {@link BitmapFont}. Defaults to {@link TextureFilter#Nearest}. */
        public TextureFilter magFilter = TextureFilter.Nearest;

        /** optional {@link BitmapFontData} to be used instead of loading the {@link Texture} directly. **/
        public BitmapFontData bitmapFontData = null;

        /**
         * The name of the {@link TextureAtlas} to load the {@link BitmapFont} itself from. Optional; if {@code null}, will look for
         * a separate image
         */
        public String atlasName = null;
    }
}
