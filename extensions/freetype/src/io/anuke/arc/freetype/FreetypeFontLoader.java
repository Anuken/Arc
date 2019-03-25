package io.anuke.arc.freetype;

import io.anuke.arc.assets.AssetDescriptor;
import io.anuke.arc.assets.AssetLoaderParameters;
import io.anuke.arc.assets.AssetManager;
import io.anuke.arc.assets.loaders.AsynchronousAssetLoader;
import io.anuke.arc.assets.loaders.FileHandleResolver;
import io.anuke.arc.collection.Array;
import io.anuke.arc.files.FileHandle;
import io.anuke.arc.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import io.anuke.arc.graphics.g2d.BitmapFont;

/**
 * Creates {@link BitmapFont} instances from FreeType font files. Requires a {@link FreeTypeFontLoaderParameter} to be
 * passed to {@link AssetManager#load(String, Class, AssetLoaderParameters)} which specifies the name of the TTF
 * file as well the parameters used to generate the BitmapFont (size, characters, etc.)
 */
public class FreetypeFontLoader extends AsynchronousAssetLoader<BitmapFont, FreetypeFontLoader.FreeTypeFontLoaderParameter>{
    public FreetypeFontLoader(FileHandleResolver resolver){
        super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, FreeTypeFontLoaderParameter parameter){
        if(parameter == null)
            throw new RuntimeException("FreetypeFontParameter must be set in AssetManager#load to point at a TTF file!");
    }

    @Override
    public BitmapFont loadSync(AssetManager manager, String fileName, FileHandle file, FreeTypeFontLoaderParameter parameter){
        if(parameter == null)
            throw new RuntimeException("FreetypeFontParameter must be set in AssetManager#load to point at a TTF file!");
        FreeTypeFontGenerator generator = manager.get(parameter.fontFileName + ".gen", FreeTypeFontGenerator.class);
        return generator.generateFont(parameter.fontParameters);
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, FreeTypeFontLoaderParameter parameter){
        Array<AssetDescriptor> deps = new Array<>();
        deps.add(new AssetDescriptor<>(parameter.fontFileName + ".gen", FreeTypeFontGenerator.class));
        return deps;
    }

    public static class FreeTypeFontLoaderParameter extends AssetLoaderParameters<BitmapFont>{
        /** the name of the TTF file to be used to load the font **/
        public String fontFileName;
        /** the parameters used to generate the font, e.g. size, characters, etc. **/
        public FreeTypeFontParameter fontParameters = new FreeTypeFontParameter();
    }
}
