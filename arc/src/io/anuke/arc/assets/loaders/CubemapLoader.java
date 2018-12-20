/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package io.anuke.arc.assets.loaders;

import io.anuke.arc.assets.AssetDescriptor;
import io.anuke.arc.assets.AssetLoaderParameters;
import io.anuke.arc.assets.AssetManager;
import io.anuke.arc.collection.Array;
import io.anuke.arc.files.FileHandle;
import io.anuke.arc.graphics.Cubemap;
import io.anuke.arc.graphics.CubemapData;
import io.anuke.arc.graphics.Pixmap;
import io.anuke.arc.graphics.Pixmap.Format;
import io.anuke.arc.graphics.Texture.TextureFilter;
import io.anuke.arc.graphics.Texture.TextureWrap;
import io.anuke.arc.graphics.TextureData;

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
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, CubemapParameter parameter){
        info.filename = fileName;
        if(parameter == null || parameter.cubemapData == null){
            Pixmap pixmap = null;
            Format format = null;
            boolean genMipMaps = false;
            info.cubemap = null;

            if(parameter != null){
                format = parameter.format;
                info.cubemap = parameter.cubemap;
            }
        }else{
            info.data = parameter.cubemapData;
            info.cubemap = parameter.cubemap;
        }
        if(!info.data.isPrepared()) info.data.prepare();
    }

    @Override
    public Cubemap loadSync(AssetManager manager, String fileName, FileHandle file, CubemapParameter parameter){
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
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, CubemapParameter parameter){
        return null;
    }

    static public class CubemapLoaderInfo{
        String filename;
        CubemapData data;
        Cubemap cubemap;
    }

    static public class CubemapParameter extends AssetLoaderParameters<Cubemap>{
        /** the format of the final Texture. Uses the source images format if null **/
        public Format format = null;
        /** The texture to put the {@link TextureData} in, optional. **/
        public Cubemap cubemap = null;
        /** CubemapData for textures created on the fly, optional. When set, all format and genMipMaps are ignored */
        public CubemapData cubemapData = null;
        public TextureFilter minFilter = TextureFilter.Nearest;
        public TextureFilter magFilter = TextureFilter.Nearest;
        public TextureWrap wrapU = TextureWrap.ClampToEdge;
        public TextureWrap wrapV = TextureWrap.ClampToEdge;
    }
}
