package arc.maps.loaders;

import arc.assets.AssetDescriptor;
import arc.assets.AssetManager;
import arc.assets.loaders.FileHandleResolver;
import arc.assets.loaders.TextureLoader;
import arc.assets.loaders.TextureLoader.TextureParameter;
import arc.assets.loaders.resolvers.InternalFileHandleResolver;
import arc.struct.Array;
import arc.struct.ObjectMap;
import arc.files.Fi;
import arc.graphics.Texture;
import arc.graphics.g2d.TextureRegion;
import arc.maps.*;
import arc.maps.loaders.ImageResolver.AssetManagerImageResolver;
import arc.maps.loaders.ImageResolver.DirectImageResolver;
import arc.util.ArcRuntimeException;
import arc.util.serialization.SerializationException;
import arc.util.serialization.XmlReader.Element;

import java.io.IOException;

/** synchronous loader for TMX maps created with the Tiled tool */
public class TmxMapLoader extends BaseTmxMapLoader<TmxMapLoader.Parameters>{

    public TmxMapLoader(){
        super(new InternalFileHandleResolver());
    }

    /**
     * Creates loader
     */
    public TmxMapLoader(FileHandleResolver resolver){
        super(resolver);
    }

    /**
     * Loads the {@link TiledMap} from the given file. The file is resolved via the {@link FileHandleResolver} set in the
     * constructor of this class. By default it will resolve to an internal file. The map will be loaded for a y-up coordinate
     * system.
     * @param fileName the filename
     * @return the TiledMap
     */
    public TiledMap load(String fileName){
        return load(fileName, new TmxMapLoader.Parameters());
    }

    /**
     * Loads the {@link TiledMap} from the given file. The file is resolved via the {@link FileHandleResolver} set in the
     * constructor of this class. By default it will resolve to an internal file.
     * @param fileName the filename
     * @param parameters specifies whether to use y-up, generate mip maps etc.
     * @return the TiledMap
     */
    public TiledMap load(String fileName, TmxMapLoader.Parameters parameters){
        try{
            this.convertObjectToTileSpace = parameters.convertObjectToTileSpace;
            this.flipY = parameters.flipY;
            Fi tmxFile = resolve(fileName);
            root = xml.parse(tmxFile);
            ObjectMap<String, Texture> textures = new ObjectMap<>();
            Array<Fi> textureFiles = loadTilesets(root, tmxFile);
            textureFiles.addAll(loadImages(root, tmxFile));

            for(Fi textureFile : textureFiles){
                Texture texture = new Texture(textureFile, parameters.generateMipMaps);
                texture.setFilter(parameters.textureMinFilter, parameters.textureMagFilter);
                textures.put(textureFile.path(), texture);
            }

            DirectImageResolver imageResolver = new DirectImageResolver(textures);
            TiledMap map = loadTilemap(root, tmxFile, imageResolver);
            map.setOwnedResources(textures.values().toArray());
            return map;
        }catch(IOException e){
            throw new ArcRuntimeException("Couldn't load tilemap '" + fileName + "'", e);
        }
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, Fi tmxFile, TmxMapLoader.Parameters parameter){
        map = null;

        if(parameter != null){
            convertObjectToTileSpace = parameter.convertObjectToTileSpace;
            flipY = parameter.flipY;
        }else{
            convertObjectToTileSpace = false;
            flipY = true;
        }
        try{
            map = loadTilemap(root, tmxFile, new AssetManagerImageResolver(manager));
        }catch(Exception e){
            throw new ArcRuntimeException("Couldn't load tilemap '" + fileName + "'", e);
        }
    }

    @Override
    public TiledMap loadSync(AssetManager manager, String fileName, Fi file, TmxMapLoader.Parameters parameter){
        return map;
    }

    /**
     * Retrieves TiledMap resource dependencies
     * @param parameter not used for now
     * @return dependencies for the given .tmx file
     */
    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, Fi tmxFile, Parameters parameter){
        Array<AssetDescriptor> dependencies = new Array<>();
        try{
            root = xml.parse(tmxFile);
            boolean generateMipMaps = (parameter != null && parameter.generateMipMaps);
            TextureLoader.TextureParameter texParams = new TextureParameter();
            texParams.genMipMaps = generateMipMaps;
            if(parameter != null){
                texParams.minFilter = parameter.textureMinFilter;
                texParams.magFilter = parameter.textureMagFilter;
            }
            for(Fi image : loadTilesets(root, tmxFile)){
                dependencies.add(new AssetDescriptor(image, Texture.class, texParams));
            }
            for(Fi image : loadImages(root, tmxFile)){
                dependencies.add(new AssetDescriptor(image, Texture.class, texParams));
            }
            return dependencies;
        }catch(IOException e){
            throw new ArcRuntimeException("Couldn't load tilemap '" + fileName + "'", e);
        }
    }

    /**
     * Loads the map data, given the XML root element and an {@link ImageResolver} used to return the tileset Textures
     * @param root the XML root element
     * @param tmxFile the Filehandle of the tmx file
     * @param imageResolver the {@link ImageResolver}
     * @return the {@link TiledMap}
     */
    protected TiledMap loadTilemap(Element root, Fi tmxFile, ImageResolver imageResolver){
        TiledMap map = new TiledMap();

        String mapOrientation = root.getAttribute("orientation", null);
        int mapWidth = root.getIntAttribute("width", 0);
        int mapHeight = root.getIntAttribute("height", 0);
        int tileWidth = root.getIntAttribute("tilewidth", 0);
        int tileHeight = root.getIntAttribute("tileheight", 0);
        int hexSideLength = root.getIntAttribute("hexsidelength", 0);
        String staggerAxis = root.getAttribute("staggeraxis", null);
        String staggerIndex = root.getAttribute("staggerindex", null);
        String mapBackgroundColor = root.getAttribute("backgroundcolor", null);

        MapProperties mapProperties = map.properties;
        if(mapOrientation != null){
            mapProperties.put("orientation", mapOrientation);
        }
        mapProperties.put("width", mapWidth);
        mapProperties.put("height", mapHeight);
        mapProperties.put("tilewidth", tileWidth);
        mapProperties.put("tileheight", tileHeight);
        mapProperties.put("hexsidelength", hexSideLength);
        if(staggerAxis != null){
            mapProperties.put("staggeraxis", staggerAxis);
        }
        if(staggerIndex != null){
            mapProperties.put("staggerindex", staggerIndex);
        }
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

        Element properties = root.getChildByName("properties");
        if(properties != null){
            loadProperties(map.properties, properties);
        }
        Array<Element> tilesets = root.getChildrenByName("tileset");
        for(Element element : tilesets){
            loadTileSet(map, element, tmxFile, imageResolver);
            root.removeChild(element);
        }
        for(int i = 0, j = root.getChildCount(); i < j; i++){
            Element element = root.getChild(i);
            loadLayer(map, map.layers, element, tmxFile, imageResolver);
        }
        return map;
    }

    /**
     * Loads the tilesets
     * @param root the root XML element
     * @return a list of filenames for images containing tiles
     */
    protected Array<Fi> loadTilesets(Element root, Fi tmxFile) throws IOException{
        Array<Fi> images = new Array<>();
        for(Element tileset : root.getChildrenByName("tileset")){
            String source = tileset.getAttribute("source", null);
            if(source != null){
                Fi tsxFile = getRelativeFileHandle(tmxFile, source);
                tileset = xml.parse(tsxFile);
                Element imageElement = tileset.getChildByName("image");
                if(imageElement != null){
                    String imageSource = tileset.getChildByName("image").getAttribute("source");
                    Fi image = getRelativeFileHandle(tsxFile, imageSource);
                    images.add(image);
                }else{
                    for(Element tile : tileset.getChildrenByName("tile")){
                        String imageSource = tile.getChildByName("image").getAttribute("source");
                        Fi image = getRelativeFileHandle(tsxFile, imageSource);
                        images.add(image);
                    }
                }
            }else{
                Element imageElement = tileset.getChildByName("image");
                if(imageElement != null){
                    String imageSource = tileset.getChildByName("image").getAttribute("source");
                    Fi image = getRelativeFileHandle(tmxFile, imageSource);
                    images.add(image);
                }else{
                    for(Element tile : tileset.getChildrenByName("tile")){
                        String imageSource = tile.getChildByName("image").getAttribute("source");
                        Fi image = getRelativeFileHandle(tmxFile, imageSource);
                        images.add(image);
                    }
                }
            }
        }
        return images;
    }

    /**
     * Loads the images in image layers
     * @param root the root XML element
     * @return a list of filenames for images inside image layers
     */
    protected Array<Fi> loadImages(Element root, Fi tmxFile){
        Array<Fi> images = new Array<>();

        for(Element imageLayer : root.getChildrenByName("imagelayer")){
            Element image = imageLayer.getChildByName("image");
            String source = image.getAttribute("source", null);

            if(source != null){
                Fi handle = getRelativeFileHandle(tmxFile, source);

                if(!images.contains(handle, false)){
                    images.add(handle);
                }
            }
        }

        return images;
    }

    /**
     * Loads the specified tileset data, adding it to the struct of the specified map, given the XML element, the tmxFile and
     * an {@link ImageResolver} used to retrieve the tileset Textures.
     *
     * <p>
     * Default tileset's property keys that are loaded by default are:
     * </p>
     *
     * <ul>
     * <li><em>firstgid</em>, (int, defaults to 1) the first valid global id used for tile numbering</li>
     * <li><em>imagesource</em>, (String, defaults to empty string) the tileset source image filename</li>
     * <li><em>imagewidth</em>, (int, defaults to 0) the tileset source image width</li>
     * <li><em>imageheight</em>, (int, defaults to 0) the tileset source image height</li>
     * <li><em>tilewidth</em>, (int, defaults to 0) the tile width</li>
     * <li><em>tileheight</em>, (int, defaults to 0) the tile height</li>
     * <li><em>margin</em>, (int, defaults to 0) the tileset margin</li>
     * <li><em>spacing</em>, (int, defaults to 0) the tileset spacing</li>
     * </ul>
     *
     * <p>
     * The values are extracted from the specified Tmx file, if a value can't be found then the default is used.
     * </p>
     * @param map the Map whose tilesets struct will be populated
     * @param element the XML element identifying the tileset to load
     * @param tmxFile the Filehandle of the tmx file
     * @param imageResolver the {@link ImageResolver}
     */
    protected void loadTileSet(TiledMap map, Element element, Fi tmxFile, ImageResolver imageResolver){
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

            TileSet tileset = new TileSet();
            tileset.name = name;
            tileset.getProperties().put("firstgid", firstgid);
            if(image != null){
                TextureRegion texture = imageResolver.getImage(image.path());

                MapProperties props = tileset.getProperties();
                props.put("imagesource", imageSource);
                props.put("imagewidth", imageWidth);
                props.put("imageheight", imageHeight);
                props.put("tilewidth", tilewidth);
                props.put("tileheight", tileheight);
                props.put("margin", margin);
                props.put("spacing", spacing);

                int stopWidth = texture.getWidth() - tilewidth;
                int stopHeight = texture.getHeight() - tileheight;

                int id = firstgid;

                for(int y = margin; y <= stopHeight; y += tileheight + spacing){
                    for(int x = margin; x <= stopWidth; x += tilewidth + spacing){
                        TextureRegion tileRegion = new TextureRegion(texture, x, y, tilewidth, tileheight);
                        MapTile tile = new MapTile(tileRegion);
                        tile.id = id;
                        tile.offsetX = offsetX;
                        tile.offsetY = flipY ? -offsetY : offsetY;
                        tileset.put(id++, tile);
                    }
                }
            }else{
                Array<Element> tileElements = element.getChildrenByName("tile");
                for(Element tileElement : tileElements){
                    Element imageElement = tileElement.getChildByName("image");
                    if(imageElement != null){
                        imageSource = imageElement.getAttribute("source");
                        imageWidth = imageElement.getIntAttribute("width", 0);
                        imageHeight = imageElement.getIntAttribute("height", 0);

                        if(source != null){
                            image = getRelativeFileHandle(getRelativeFileHandle(tmxFile, source), imageSource);
                        }else{
                            image = getRelativeFileHandle(tmxFile, imageSource);
                        }
                    }
                    TextureRegion texture = imageResolver.getImage(image.path());
                    MapTile tile = new MapTile(texture);
                    tile.id = firstgid + tileElement.getIntAttribute("id");
                    tile.offsetX = offsetX;
                    tile.offsetY = flipY ? -offsetY : offsetY;
                    tileset.put(tile.id, tile);
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

    public static class Parameters extends BaseTmxMapLoader.Parameters{

    }

}
