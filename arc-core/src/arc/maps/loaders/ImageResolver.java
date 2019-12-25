package arc.maps.loaders;

import arc.assets.AssetManager;
import arc.struct.ObjectMap;
import arc.graphics.Texture;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;

/**
 * Resolves an image by a string, wrapper around a Map or AssetManager to load maps either directly or via AssetManager.
 * @author mzechner
 */
public interface ImageResolver{
    /**
     * @return the Texture for the given image name or null.*/
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
