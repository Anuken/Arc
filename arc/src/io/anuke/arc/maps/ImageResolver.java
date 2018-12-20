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

package io.anuke.arc.maps;

import io.anuke.arc.assets.AssetManager;
import io.anuke.arc.collection.ObjectMap;
import io.anuke.arc.graphics.Texture;
import io.anuke.arc.graphics.g2d.TextureAtlas;
import io.anuke.arc.graphics.g2d.TextureRegion;

/**
 * Resolves an image by a string, wrapper around a Map or AssetManager to load maps either directly or via AssetManager.
 * @author mzechner
 */
public interface ImageResolver{
    /**
     * @return the Texture for the given image name or null.
     */
    TextureRegion getImage(String name);

    class DirectImageResolver implements ImageResolver{
        private final ObjectMap<String, Texture> images;

        public DirectImageResolver(ObjectMap<String, Texture> images){
            this.images = images;
        }

        @Override
        public TextureRegion getImage(String name){
            return new TextureRegion(images.get(name));
        }
    }

    class AssetManagerImageResolver implements ImageResolver{
        private final AssetManager assetManager;

        public AssetManagerImageResolver(AssetManager assetManager){
            this.assetManager = assetManager;
        }

        @Override
        public TextureRegion getImage(String name){
            return new TextureRegion(assetManager.get(name, Texture.class));
        }
    }

    class TextureAtlasImageResolver implements ImageResolver{
        private final TextureAtlas atlas;

        public TextureAtlasImageResolver(TextureAtlas atlas){
            this.atlas = atlas;
        }

        @Override
        public TextureRegion getImage(String name){
            return atlas.find(name);
        }
    }
}
