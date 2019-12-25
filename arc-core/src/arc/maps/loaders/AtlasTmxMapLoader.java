package arc.maps.loaders;

import arc.assets.AssetDescriptor;
import arc.assets.AssetManager;
import arc.assets.loaders.FileHandleResolver;
import arc.assets.loaders.resolvers.InternalFileHandleResolver;
import arc.struct.Array;
import arc.struct.ObjectMap;
import arc.files.Fi;
import arc.graphics.Texture;
import arc.graphics.Texture.TextureFilter;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureAtlas.AtlasRegion;
import arc.maps.*;
import arc.util.ArcRuntimeException;
import arc.util.serialization.SerializationException;
import arc.util.serialization.XmlReader.Element;

import java.io.IOException;

/**
 * A TiledMap Loader which loads tiles from a TextureAtlas instead of separate images.
 * <p>
 * It requires a map-level property called 'atlas' with its value being the relative path to the TextureAtlas. The atlas must have
 * in it indexed regions named after the tilesets used in the map. The indexes shall be local to the tileset (not the global id).
 * Strip whitespace and rotation should not be used when creating the atlas.
 * @author Justin Shapcott
 * @author Manuel Bua
 */
public class AtlasTmxMapLoader extends BaseTmxMapLoader<AtlasTmxMapLoader.AtlasTiledMapLoaderParameters>{
    protected Array<Texture> trackedTextures = new Array<>();

    public AtlasTmxMapLoader(){
        super(new InternalFileHandleResolver());
    }

    public AtlasTmxMapLoader(FileHandleResolver resolver){
        super(resolver);
    }

    public TiledMap load(String fileName){
        return load(fileName, new AtlasTiledMapLoaderParameters());
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, Fi tmxFile, AtlasTiledMapLoaderParameters parameter){
        Array<AssetDescriptor> dependencies = new Array<>();
        try{
            root = xml.parse(tmxFile);

            Element properties = root.getChildByName("properties");
            if(properties != null){
                for(Element property : properties.getChildrenByName("property")){
                    String name = property.getAttribute("name");
                    String value = property.getAttribute("value");
                    if(name.startsWith("atlas")){
                        Fi atlasHandle = getRelativeFileHandle(tmxFile, value);
                        dependencies.add(new AssetDescriptor<>(atlasHandle, TextureAtlas.class));
                    }
                }
            }
        }catch(SerializationException e){
            throw new ArcRuntimeException("Unable to parse .tmx file.");
        }
        return dependencies;
    }

    public TiledMap load(String fileName, AtlasTiledMapLoaderParameters parameter){
        try{
            if(parameter != null){
                convertObjectToTileSpace = parameter.convertObjectToTileSpace;
                flipY = parameter.flipY;
            }else{
                convertObjectToTileSpace = false;
                flipY = true;
            }

            Fi tmxFile = resolve(fileName);
            root = xml.parse(tmxFile);
            ObjectMap<String, TextureAtlas> atlases = new ObjectMap<>();
            Fi atlasFile = loadAtlas(root, tmxFile);
            if(atlasFile == null){
                throw new ArcRuntimeException("Couldn't load atlas");
            }

            TextureAtlas atlas = new TextureAtlas(atlasFile);
            atlases.put(atlasFile.path(), atlas);

            AtlasResolver.DirectAtlasResolver atlasResolver = new AtlasResolver.DirectAtlasResolver(atlases);
            TiledMap map = loadMap(root, tmxFile, atlasResolver);
            map.setOwnedResources(atlases.values().toArray());
            setTextureFilters(parameter.textureMinFilter, parameter.textureMagFilter);
            return map;
        }catch(IOException e){
            throw new ArcRuntimeException("Couldn't load tilemap '" + fileName + "'", e);
        }
    }

    /** May return null. */
    protected Fi loadAtlas(Element root, Fi tmxFile) throws IOException{
        Element e = root.getChildByName("properties");

        if(e != null){
            for(Element property : e.getChildrenByName("property")){
                String name = property.getAttribute("name", null);
                String value = property.getAttribute("value", null);
                if(name.equals("atlas")){
                    if(value == null){
                        value = property.getText();
                    }

                    if(value == null || value.length() == 0){
                        // keep trying until there are no more atlas properties
                        continue;
                    }

                    return getRelativeFileHandle(tmxFile, value);
                }
            }
        }
        Fi atlasFile = tmxFile.sibling(tmxFile.nameWithoutExtension() + ".atlas");
        return atlasFile.exists() ? atlasFile : null;
    }

    private void setTextureFilters(TextureFilter min, TextureFilter mag){
        for(Texture texture : trackedTextures){
            texture.setFilter(min, mag);
        }
        trackedTextures.clear();
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, Fi tmxFile, AtlasTiledMapLoaderParameters parameter){
        map = null;

        if(parameter != null){
            convertObjectToTileSpace = parameter.convertObjectToTileSpace;
            flipY = parameter.flipY;
        }else{
            convertObjectToTileSpace = false;
            flipY = true;
        }

        try{
            map = loadMap(root, tmxFile, new AtlasResolver.AssetManagerAtlasResolver(manager));
        }catch(Exception e){
            throw new ArcRuntimeException("Couldn't load tilemap '" + fileName + "'", e);
        }
    }

    @Override
    public TiledMap loadSync(AssetManager manager, String fileName, Fi file, AtlasTiledMapLoaderParameters parameter){
        if(parameter != null){
            setTextureFilters(parameter.textureMinFilter, parameter.textureMagFilter);
        }

        return map;
    }

    protected TiledMap loadMap(Element root, Fi tmxFile, AtlasResolver resolver){
        TiledMap map = new TiledMap();

        String mapOrientation = root.getAttribute("orientation", null);
        int mapWidth = root.getIntAttribute("width", 0);
        int mapHeight = root.getIntAttribute("height", 0);
        int tileWidth = root.getIntAttribute("tilewidth", 0);
        int tileHeight = root.getIntAttribute("tileheight", 0);
        String mapBackgroundColor = root.getAttribute("backgroundcolor", null);

        MapProperties mapProperties = map.properties;
        if(mapOrientation != null){
            mapProperties.put("orientation", mapOrientation);
        }
        mapProperties.put("width", mapWidth);
        mapProperties.put("height", mapHeight);
        mapProperties.put("tilewidth", tileWidth);
        mapProperties.put("tileheight", tileHeight);
        if(mapBackgroundColor != null){
            mapProperties.put("backgroundcolor", mapBackgroundColor);
        }

        mapTileWidth = tileWidth;
        mapTileHeight = tileHeight;
        mapWidthInPixels = mapWidth * tileWidth;
        mapHeightInPixels = mapHeight * tileHeight;

        if(mapOrientation != null){
            if("staggered".equals(mapOrientation)){
                if(mapHeight > 1){
                    mapWidthInPixels += tileWidth / 2;
                    mapHeightInPixels = mapHeightInPixels / 2 + tileHeight / 2;
                }
            }
        }

        for(int i = 0, j = root.getChildCount(); i < j; i++){
            Element element = root.getChild(i);
            String elementName = element.getName();
            if(elementName.equals("properties")){
                loadProperties(map.properties, element);
            }else if(elementName.equals("tileset")){
                loadTileset(map, element, tmxFile, resolver);
            }else if(elementName.equals("layer")){
                loadTileLayer(map, map.layers, element);
            }else if(elementName.equals("objectgroup")){
                loadObjectGroup(map, map.layers, element);
            }
        }
        return map;
    }

    protected void loadTileset(TiledMap map, Element element, Fi tmxFile, AtlasResolver resolver){
        if(element.getName().equals("tileset")){
            String name = element.get("name", null);
            int firstgid = element.getIntAttribute("firstgid", 1);
            int tilewidth = element.getIntAttribute("tilewidth", 0);
            int tileheight = element.getIntAttribute("tileheight", 0);
            int spacing = element.getIntAttribute("spacing", 0);
            int margin = element.getIntAttribute("margin", 0);
            String source = element.getAttribute("source", null);

            int offsetX = 0;
            int offsetY = 0;

            String imageSource = "";
            int imageWidth = 0, imageHeight = 0;

            Fi image = null;
            if(source != null){
                Fi tsx = getRelativeFileHandle(tmxFile, source);
                try{
                    element = xml.parse(tsx);
                    name = element.get("name", null);
                    tilewidth = element.getIntAttribute("tilewidth", 0);
                    tileheight = element.getIntAttribute("tileheight", 0);
                    spacing = element.getIntAttribute("spacing", 0);
                    margin = element.getIntAttribute("margin", 0);
                    Element offset = element.getChildByName("tileoffset");
                    if(offset != null){
                        offsetX = offset.getIntAttribute("x", 0);
                        offsetY = offset.getIntAttribute("y", 0);
                    }
                    Element imageElement = element.getChildByName("image");
                    if(imageElement != null){
                        imageSource = imageElement.getAttribute("source");
                        imageWidth = imageElement.getIntAttribute("width", 0);
                        imageHeight = imageElement.getIntAttribute("height", 0);
                        image = getRelativeFileHandle(tsx, imageSource);
                    }
                }catch(SerializationException e){
                    throw new ArcRuntimeException("Error parsing external tileset.");
                }
            }else{
                Element offset = element.getChildByName("tileoffset");
                if(offset != null){
                    offsetX = offset.getIntAttribute("x", 0);
                    offsetY = offset.getIntAttribute("y", 0);
                }
                Element imageElement = element.getChildByName("image");
                if(imageElement != null){
                    imageSource = imageElement.getAttribute("source");
                    imageWidth = imageElement.getIntAttribute("width", 0);
                    imageHeight = imageElement.getIntAttribute("height", 0);
                    image = getRelativeFileHandle(tmxFile, imageSource);
                }
            }

            String atlasFilePath = map.properties.get("atlas");
            if(atlasFilePath == null){
                Fi atlasFile = tmxFile.sibling(tmxFile.nameWithoutExtension() + ".atlas");
                if(atlasFile.exists()) atlasFilePath = atlasFile.name();
            }
            if(atlasFilePath == null){
                throw new ArcRuntimeException("The map is missing the 'atlas' property");
            }

            // get the TextureAtlas for this tileset
            Fi atlasHandle = getRelativeFileHandle(tmxFile, atlasFilePath);
            atlasHandle = resolve(atlasHandle.path());
            TextureAtlas atlas = resolver.getAtlas(atlasHandle.path());
            String regionsName = name;

            for(Texture texture : atlas.getTextures()){
                trackedTextures.add(texture);
            }

            TileSet tileset = new TileSet();
            MapProperties props = tileset.getProperties();
            tileset.name = name;
            props.put("firstgid", firstgid);
            props.put("imagesource", imageSource);
            props.put("imagewidth", imageWidth);
            props.put("imageheight", imageHeight);
            props.put("tilewidth", tilewidth);
            props.put("tileheight", tileheight);
            props.put("margin", margin);
            props.put("spacing", spacing);

            if(imageSource != null && imageSource.length() > 0){
                int lastgid = firstgid + ((imageWidth / tilewidth) * (imageHeight / tileheight)) - 1;
                for(AtlasRegion region : atlas.findRegions(regionsName)){
                    // handle unused tile ids
                    if(region != null){
                        int tileid = region.index + firstgid;
                        if(tileid >= firstgid && tileid <= lastgid){
                            MapTile tile = new MapTile(region);
                            tile.id = tileid;
                            tile.offsetX = (float)offsetX;
                            float offsetY1 = flipY ? -offsetY : offsetY;
                            tile.offsetY = offsetY1;
                            tileset.put(tileid, tile);
                        }
                    }
                }
            }

            for(Element tileElement : element.getChildrenByName("tile")){
                int tileid = firstgid + tileElement.getIntAttribute("id", 0);
                MapTile tile = tileset.get(tileid);
                if(tile == null){
                    Element imageElement = tileElement.getChildByName("image");
                    if(imageElement != null){
                        // Is a tilemap with individual images.
                        String regionName = imageElement.getAttribute("source");
                        regionName = regionName.substring(0, regionName.lastIndexOf('.'));
                        AtlasRegion region = atlas.find(regionName);
                        if(region == null) throw new ArcRuntimeException("Tileset region not found: " + regionName);
                        tile = new MapTile(region);
                        tile.id = tileid;
                        tile.offsetX = offsetX;
                        tile.offsetY = (flipY ? -offsetY : offsetY);
                        tileset.put(tileid, tile);
                    }
                }
                if(tile != null){
                    String terrain = tileElement.getAttribute("terrain", null);
                    if(terrain != null){
                        tile.getProperties().put("terrain", terrain);
                    }
                    String probability = tileElement.getAttribute("probability", null);
                    if(probability != null){
                        tile.getProperties().put("probability", probability);
                    }
                    Element properties = tileElement.getChildByName("properties");
                    if(properties != null){
                        loadProperties(tile.getProperties(), properties);
                    }
                }
            }

            Array<Element> tileElements = element.getChildrenByName("tile");

            for(Element tileElement : tileElements){
                int localtid = tileElement.getIntAttribute("id", 0);
                MapTile tile = tileset.get(firstgid + localtid);
                if(tile != null){
                    Element objectgroupElement = tileElement.getChildByName("objectgroup");
                    if(objectgroupElement != null){

                        for(Element objectElement : objectgroupElement.getChildrenByName("object")){
                            loadObject(map, tile, objectElement);
                        }
                    }

                    String terrain = tileElement.getAttribute("terrain", null);
                    if(terrain != null){
                        tile.getProperties().put("terrain", terrain);
                    }
                    String probability = tileElement.getAttribute("probability", null);
                    if(probability != null){
                        tile.getProperties().put("probability", probability);
                    }
                    Element properties = tileElement.getChildByName("properties");
                    if(properties != null){
                        loadProperties(tile.getProperties(), properties);
                    }
                }
            }

            Element properties = element.getChildByName("properties");
            if(properties != null){
                loadProperties(tileset.getProperties(), properties);
            }
            map.tilesets.addTileSet(tileset);
        }
    }

    private interface AtlasResolver{

        TextureAtlas getAtlas(String name);

        class DirectAtlasResolver implements AtlasResolver{

            private final ObjectMap<String, TextureAtlas> atlases;

            public DirectAtlasResolver(ObjectMap<String, TextureAtlas> atlases){
                this.atlases = atlases;
            }

            @Override
            public TextureAtlas getAtlas(String name){
                return atlases.get(name);
            }

        }

        class AssetManagerAtlasResolver implements AtlasResolver{
            private final AssetManager assetManager;

            public AssetManagerAtlasResolver(AssetManager assetManager){
                this.assetManager = assetManager;
            }

            @Override
            public TextureAtlas getAtlas(String name){
                return assetManager.get(name, TextureAtlas.class);
            }
        }
    }

    public static class AtlasTiledMapLoaderParameters extends BaseTmxMapLoader.Parameters{
        /** force texture filters? **/
        public boolean forceTextureFilters = false;
    }

}
