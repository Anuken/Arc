package arc.assets.loaders;

import arc.*;
import arc.assets.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.g2d.TextureAtlas.*;
import arc.graphics.g2d.TextureAtlas.TextureAtlasData.*;
import arc.struct.*;
import arc.util.*;

import java.util.concurrent.*;

/**
 * {@link AssetLoader} to load {@link TextureAtlas} instances. Passing a {@link TextureAtlasParameter} to
 * {@link AssetManager#load(String, Class, AssetLoaderParameters)} allows to specify whether the atlas regions should be flipped
 * on the y-axis or not.
 * @author mzechner
 */
public class TextureAtlasLoader extends AsynchronousAssetLoader<TextureAtlas, TextureAtlasLoader.TextureAtlasParameter>{
    TextureAtlasData data;
    Seq<Future<AsyncResult>> textureLoaders = new Seq<>();

    public TextureAtlasLoader(FileHandleResolver resolver){
        super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, Fi file, TextureAtlasParameter parameter){
        Fi imgDir = file.parent();

        if(parameter != null){
            data = new TextureAtlasData(file, imgDir, parameter.flip);
        }else{
            data = new TextureAtlasData(file, imgDir, false);
        }

        for(AtlasPage page : data.pages){
            textureLoaders.add(Core.executor.submit(() -> new AsyncResult(page, new Pixmap(page.textureFile))));
        }
    }

    @Override
    public TextureAtlas loadSync(AssetManager manager, String fileName, Fi file, TextureAtlasParameter parameter){
        try{
            int maxWidth = data.pages.max(a -> a.width).width;
            int maxHeight = data.pages.max(a -> a.height).height;
            Pixmap[] pixmaps = new Pixmap[data.pages.size];

            for(Future<AsyncResult> result : textureLoaders){
                AsyncResult res = result.get();
                pixmaps[data.pages.indexOf(res.page)] = res.pixmap;
            }

            data.texture = new TextureArray(new TextureArrayData(){
                @Override
                public boolean isPrepared(){
                    return false;
                }

                @Override
                public void prepare(){

                }

                @Override
                public void consumeTextureArrayData(){
                    for(int i = 0; i < pixmaps.length; i++){
                        Pixmap pixmap = pixmaps[i];
                        Core.gl30.glTexSubImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, 0, 0, i, pixmap.width, pixmap.height, 1, pixmap.getGLInternalFormat(), pixmap.getGLType(), pixmap.pixels);
                        pixmap.dispose();
                    }
                }

                @Override
                public int getWidth(){
                    return maxWidth;
                }

                @Override
                public int getHeight(){
                    return maxHeight;
                }

                @Override
                public int getDepth(){
                    return pixmaps.length;
                }

                @Override
                public int getInternalFormat(){
                    return Gl.unsignedByte;
                }

                @Override
                public int getGLType(){
                    return Gl.rgba;
                }
            });

            TextureAtlas atlas = new TextureAtlas(data);
            data = null;

            return atlas;
        }catch(Exception e){
            throw new ArcRuntimeException(e);
        }
    }

    @Override
    public Seq<AssetDescriptor> getDependencies(String fileName, Fi file, TextureAtlasParameter parameter){
        return null;
    }

    static class AsyncResult{
        final AtlasPage page;
        final Pixmap pixmap;

        public AsyncResult(AtlasPage page, Pixmap pixmap){
            this.page = page;
            this.pixmap = pixmap;
        }
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
