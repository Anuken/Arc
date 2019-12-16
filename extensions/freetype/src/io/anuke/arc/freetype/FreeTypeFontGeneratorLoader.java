package io.anuke.arc.freetype;

import io.anuke.arc.assets.AssetDescriptor;
import io.anuke.arc.assets.AssetLoaderParameters;
import io.anuke.arc.assets.AssetManager;
import io.anuke.arc.assets.loaders.FileHandleResolver;
import io.anuke.arc.assets.loaders.SynchronousAssetLoader;
import io.anuke.arc.collection.Array;
import io.anuke.arc.files.Fi;

/**
 * Makes {@link FreeTypeFontGenerator} managable via {@link AssetManager}.
 * <p>
 * Do
 * {@code assetManager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(new InternalFileHandleResolver()))}
 * to register it.
 * </p>
 * @author Daniel Holderbaum
 */
public class FreeTypeFontGeneratorLoader extends SynchronousAssetLoader<FreeTypeFontGenerator, FreeTypeFontGeneratorLoader.FreeTypeFontGeneratorParameters>{

    public FreeTypeFontGeneratorLoader(FileHandleResolver resolver){
        super(resolver);
    }

    @Override
    public FreeTypeFontGenerator load(AssetManager assetManager, String fileName, Fi file,
                                      FreeTypeFontGeneratorParameters parameter){
        FreeTypeFontGenerator generator = null;
        if(file.extension().equals("gen")){
            generator = new FreeTypeFontGenerator(file.sibling(file.nameWithoutExtension()));
        }else{
            generator = new FreeTypeFontGenerator(file);
        }
        return generator;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, Fi file, FreeTypeFontGeneratorParameters parameter){
        return null;
    }

    public static class FreeTypeFontGeneratorParameters extends AssetLoaderParameters<FreeTypeFontGenerator>{
    }
}
