package arc.maps.loaders;

import arc.assets.AssetLoaderParameters;
import arc.assets.loaders.AsynchronousAssetLoader;
import arc.assets.loaders.FileHandleResolver;
import arc.struct.Array;
import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.Texture.TextureFilter;
import arc.graphics.g2d.TextureRegion;
import arc.maps.*;
import arc.maps.objects.*;
import arc.maps.TileLayer.Cell;
import arc.math.geom.Polygon;
import arc.math.geom.Polyline;
import arc.util.ArcRuntimeException;
import arc.util.io.Streams;
import arc.util.serialization.Base64Coder;
import arc.util.serialization.XmlReader;
import arc.util.serialization.XmlReader.Element;

import java.io.*;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public abstract class BaseTmxMapLoader<P extends AssetLoaderParameters<TiledMap>> extends AsynchronousAssetLoader<TiledMap, P>{
    protected static final int FLAG_FLIP_HORIZONTALLY = 0x80000000;
    protected static final int FLAG_FLIP_VERTICALLY = 0x40000000;
    protected static final int FLAG_FLIP_DIAGONALLY = 0x20000000;
    protected static final int MASK_CLEAR = 0xE0000000;
    protected XmlReader xml = new XmlReader();
    protected Element root;
    protected boolean convertObjectToTileSpace;
    protected boolean flipY = true;
    protected int mapTileWidth;
    protected int mapTileHeight;
    protected int mapWidthInPixels;
    protected int mapHeightInPixels;
    protected TiledMap map;

    public BaseTmxMapLoader(FileHandleResolver resolver){
        super(resolver);
    }

    public static int[] getTileIds(Element element, int width, int height){
        Element data = element.getChildByName("data");
        String encoding = data.getAttribute("encoding", null);
        if(encoding == null){ // no 'encoding' attribute means that the encoding is XML
            throw new ArcRuntimeException("Unsupported encoding (XML) for TMX Layer Data");
        }
        int[] ids = new int[width * height];
        if(encoding.equals("csv")){
            String[] array = data.getText().split(",");
            for(int i = 0; i < array.length; i++)
                ids[i] = (int)Long.parseLong(array[i].trim());
        }else{
            if(encoding.equals("base64")){
                InputStream is = null;
                try{
                    String compression = data.getAttribute("compression", null);
                    byte[] bytes = Base64Coder.decode(data.getText());
                    if(compression == null)
                        is = new ByteArrayInputStream(bytes);
                    else if(compression.equals("gzip"))
                        is = new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes), bytes.length));
                    else if(compression.equals("zlib"))
                        is = new BufferedInputStream(new InflaterInputStream(new ByteArrayInputStream(bytes)));
                    else
                        throw new ArcRuntimeException("Unrecognised compression (" + compression + ") for TMX Layer Data");

                    byte[] temp = new byte[4];
                    for(int y = 0; y < height; y++){
                        for(int x = 0; x < width; x++){
                            int read = is.read(temp);
                            while(read < temp.length){
                                int curr = is.read(temp, read, temp.length - read);
                                if(curr == -1) break;
                                read += curr;
                            }
                            if(read != temp.length)
                                throw new ArcRuntimeException("Error Reading TMX Layer Data: Premature end of tile data");
                            ids[y * width + x] = unsignedByteToInt(temp[0]) | unsignedByteToInt(temp[1]) << 8
                            | unsignedByteToInt(temp[2]) << 16 | unsignedByteToInt(temp[3]) << 24;
                        }
                    }
                }catch(IOException e){
                    throw new ArcRuntimeException("Error Reading TMX Layer Data - IOException: " + e.getMessage());
                }finally{
                    Streams.close(is);
                }
            }else{
                // any other value of 'encoding' is one we're not aware of, probably a feature of a future version of Tiled
                // or another editor
                throw new ArcRuntimeException("Unrecognised encoding (" + encoding + ") for TMX Layer Data");
            }
        }
        return ids;
    }

    protected static int unsignedByteToInt(byte b){
        return b & 0xFF;
    }

    protected static Fi getRelativeFileHandle(Fi file, String path){
        StringTokenizer tokenizer = new StringTokenizer(path, "\\/");
        Fi result = file.parent();
        while(tokenizer.hasMoreElements()){
            String token = tokenizer.nextToken();
            if(token.equals(".."))
                result = result.parent();
            else{
                result = result.child(token);
            }
        }
        return result;
    }

    protected void loadTileGroup(TiledMap map, Array<MapLayer> parentLayers, Element element, Fi tmxFile, ImageResolver imageResolver){
        if(element.getName().equals("group")){
            MapGroupLayer groupLayer = new MapGroupLayer();
            loadBasicLayerInfo(groupLayer, element);

            Element properties = element.getChildByName("properties");
            if(properties != null){
                loadProperties(groupLayer.properties, properties);
            }

            for(int i = 0, j = element.getChildCount(); i < j; i++){
                Element child = element.getChild(i);
                loadLayer(map, groupLayer.layers, child, tmxFile, imageResolver);
            }

            for(MapLayer layer : groupLayer.layers){
                layer.setParent(groupLayer);
            }

            parentLayers.add(groupLayer);
        }
    }

    protected void loadLayer(TiledMap map, Array<MapLayer> parentLayers, Element element, Fi tmxFile, ImageResolver imageResolver){
        String name = element.getName();
        if(name.equals("group")){
            loadTileGroup(map, parentLayers, element, tmxFile, imageResolver);
        }else if(name.equals("layer")){
            loadTileLayer(map, parentLayers, element);
        }else if(name.equals("objectgroup")){
            loadObjectGroup(map, parentLayers, element);
        }else if(name.equals("imagelayer")){
            loadImageLayer(map, parentLayers, element, tmxFile, imageResolver);
        }
    }

    protected void loadTileLayer(TiledMap map, Array<MapLayer> parentLayers, Element element){
        if(element.getName().equals("layer")){
            int width = element.getIntAttribute("width", 0);
            int height = element.getIntAttribute("height", 0);
            int tileWidth = map.properties.get("tilewidth");
            int tileHeight = map.properties.get("tileheight");
            TileLayer layer = new TileLayer(width, height, tileWidth, tileHeight);

            loadBasicLayerInfo(layer, element);

            int[] ids = getTileIds(element, width, height);
            TileSets tilesets = map.tilesets;
            for(int y = 0; y < height; y++){
                for(int x = 0; x < width; x++){
                    int id = ids[y * width + x];
                    boolean flipHorizontally = ((id & FLAG_FLIP_HORIZONTALLY) != 0);
                    boolean flipVertically = ((id & FLAG_FLIP_VERTICALLY) != 0);
                    boolean flipDiagonally = ((id & FLAG_FLIP_DIAGONALLY) != 0);

                    MapTile tile = tilesets.getTile(id & ~MASK_CLEAR);
                    if(tile != null){
                        Cell cell = createTileLayerCell(flipHorizontally, flipVertically, flipDiagonally);
                        cell.tile = tile;
                        layer.setCell(x, flipY ? height - 1 - y : y, cell);
                    }
                }
            }

            Element properties = element.getChildByName("properties");
            if(properties != null){
                loadProperties(layer.properties, properties);
            }
            parentLayers.add(layer);
        }
    }

    protected void loadObjectGroup(TiledMap map, Array<MapLayer> parentLayers, Element element){
        if(element.getName().equals("objectgroup")){
            MapLayer layer = new MapLayer();
            loadBasicLayerInfo(layer, element);
            Element properties = element.getChildByName("properties");
            if(properties != null){
                loadProperties(layer.properties, properties);
            }

            for(Element objectElement : element.getChildrenByName("object")){
                loadObject(map, layer, objectElement);
            }

            parentLayers.add(layer);
        }
    }

    protected void loadImageLayer(TiledMap map, Array<MapLayer> parentLayers, Element element, Fi tmxFile, ImageResolver imageResolver){
        if(element.getName().equals("imagelayer")){
            int x = 0;
            int y = 0;
            if(element.hasAttribute("offsetx")){
                x = Integer.parseInt(element.getAttribute("offsetx", "0"));
            }else{
                x = Integer.parseInt(element.getAttribute("x", "0"));
            }
            if(element.hasAttribute("offsety")){
                y = Integer.parseInt(element.getAttribute("offsety", "0"));
            }else{
                y = Integer.parseInt(element.getAttribute("y", "0"));
            }
            if(flipY) y = mapHeightInPixels - y;

            TextureRegion texture = null;

            Element image = element.getChildByName("image");

            if(image != null){
                String source = image.getAttribute("source");
                Fi handle = getRelativeFileHandle(tmxFile, source);
                texture = imageResolver.getImage(handle.path());
                y -= texture.getHeight();
            }

            ImageLayer layer = new ImageLayer(texture, x, y);

            loadBasicLayerInfo(layer, element);

            Element properties = element.getChildByName("properties");
            if(properties != null){
                loadProperties(layer.properties, properties);
            }

            parentLayers.add(layer);
        }
    }

    protected void loadBasicLayerInfo(MapLayer layer, Element element){
        String name = element.getAttribute("name", null);
        float opacity = Float.parseFloat(element.getAttribute("opacity", "1.0"));
        boolean visible = element.getIntAttribute("visible", 1) == 1;
        float offsetX = element.getFloatAttribute("offsetx", 0);
        float offsetY = element.getFloatAttribute("offsety", 0);

        layer.name = name;
        layer.opacity = opacity;
        layer.visible = visible;
        layer.setOffsetX(offsetX);
        layer.setOffsetY(offsetY);
    }

    protected void loadObject(TiledMap map, MapLayer layer, Element element){
        loadObject(map, layer.objects, element, mapHeightInPixels);
    }

    protected void loadObject(TiledMap map, MapTile tile, Element element){
        loadObject(map, tile.getObjects(), element, tile.region.getHeight());
    }

    protected void loadObject(TiledMap map, Array<MapObject> objects, Element element, float heightInPixels){
        if(element.getName().equals("object")){
            MapObject object = null;

            float scaleX = convertObjectToTileSpace ? 1.0f / mapTileWidth : 1.0f;
            float scaleY = convertObjectToTileSpace ? 1.0f / mapTileHeight : 1.0f;

            float x = element.getFloatAttribute("x", 0) * scaleX;
            float y = (flipY ? (heightInPixels - element.getFloatAttribute("y", 0)) : element.getFloatAttribute("y", 0)) * scaleY;

            float width = element.getFloatAttribute("width", 0) * scaleX;
            float height = element.getFloatAttribute("height", 0) * scaleY;

            if(element.getChildCount() > 0){
                Element child = null;
                if((child = element.getChildByName("polygon")) != null){
                    String[] points = child.getAttribute("points").split(" ");
                    float[] vertices = new float[points.length * 2];
                    for(int i = 0; i < points.length; i++){
                        String[] point = points[i].split(",");
                        vertices[i * 2] = Float.parseFloat(point[0]) * scaleX;
                        vertices[i * 2 + 1] = Float.parseFloat(point[1]) * scaleY * (flipY ? -1 : 1);
                    }
                    Polygon polygon = new Polygon(vertices);
                    polygon.setPosition(x, y);
                    object = new PolygonMapObject(polygon);
                }else if((child = element.getChildByName("polyline")) != null){
                    String[] points = child.getAttribute("points").split(" ");
                    float[] vertices = new float[points.length * 2];
                    for(int i = 0; i < points.length; i++){
                        String[] point = points[i].split(",");
                        vertices[i * 2] = Float.parseFloat(point[0]) * scaleX;
                        vertices[i * 2 + 1] = Float.parseFloat(point[1]) * scaleY * (flipY ? -1 : 1);
                    }
                    Polyline polyline = new Polyline(vertices);
                    polyline.setPosition(x, y);
                    object = new PolylineMapObject(polyline);
                }else if((child = element.getChildByName("ellipse")) != null){
                    object = new EllipseMapObject(x, flipY ? y - height : y, width, height);
                }
            }
            if(object == null){
                String gid = null;
                if((gid = element.getAttribute("gid", null)) != null){
                    int id = (int)Long.parseLong(gid);
                    boolean flipHorizontally = ((id & FLAG_FLIP_HORIZONTALLY) != 0);
                    boolean flipVertically = ((id & FLAG_FLIP_VERTICALLY) != 0);

                    MapTile tile = map.tilesets.getTile(id & ~MASK_CLEAR);
                    TileMapObject tileMapObject = new TileMapObject(tile, flipHorizontally, flipVertically);
                    TextureRegion textureRegion = tileMapObject.textureRegion;
                    tileMapObject.properties.put("gid", id);
                    tileMapObject.x = x;
                    float y1 = flipY ? y : y - height;
                    tileMapObject.y = y1;
                    float objectWidth = element.getFloatAttribute("width", textureRegion.getWidth());
                    float objectHeight = element.getFloatAttribute("height", textureRegion.getHeight());
                    tileMapObject.scaleX = scaleX * (objectWidth / textureRegion.getWidth());
                    tileMapObject.scaleY = scaleY * (objectHeight / textureRegion.getHeight());
                    tileMapObject.rotation = element.getFloatAttribute("rotation", 0);
                    object = tileMapObject;
                }else{
                    object = new RectangleMapObject(x, flipY ? y - height : y, width, height);
                }
            }
            object.name = element.getAttribute("name", null);
            String rotation = element.getAttribute("rotation", null);
            if(rotation != null){
                object.properties.put("rotation", Float.parseFloat(rotation));
            }
            String type = element.getAttribute("type", null);
            if(type != null){
                object.properties.put("type", type);
            }
            int id = element.getIntAttribute("id", 0);
            if(id != 0){
                object.properties.put("id", id);
            }
            object.properties.put("x", x);

            if(object instanceof TileMapObject){
                object.properties.put("y", y);
            }else{
                object.properties.put("y", (flipY ? y - height : y));
            }
            object.properties.put("width", width);
            object.properties.put("height", height);
            object.visible = element.getIntAttribute("visible", 1) == 1;
            Element properties = element.getChildByName("properties");
            if(properties != null){
                loadProperties(object.properties, properties);
            }
            objects.add(object);
        }
    }

    protected void loadProperties(MapProperties properties, Element element){
        if(element == null) return;
        if(element.getName().equals("properties")){
            for(Element property : element.getChildrenByName("property")){
                String name = property.getAttribute("name", null);
                String value = property.getAttribute("value", null);
                String type = property.getAttribute("type", null);
                if(value == null){
                    value = property.getText();
                }
                Object castValue = castProperty(name, value, type);
                properties.put(name, castValue);
            }
        }
    }

    private Object castProperty(String name, String value, String type){
        if(type == null){
            return value;
        }else if(type.equals("int")){
            return Integer.valueOf(value);
        }else if(type.equals("float")){
            return Float.valueOf(value);
        }else if(type.equals("bool")){
            return Boolean.valueOf(value);
        }else if(type.equals("color")){
            // Tiled uses the format #AARRGGBB
            String opaqueColor = value.substring(3);
            String alpha = value.substring(1, 3);
            return Color.valueOf(opaqueColor + alpha);
        }else{
            throw new ArcRuntimeException("Wrong type given for property " + name + ", given : " + type
            + ", supported : string, bool, int, float, color");
        }
    }

    protected Cell createTileLayerCell(boolean flipHorizontally, boolean flipVertically, boolean flipDiagonally){
        Cell cell = new Cell();
        if(flipDiagonally){
            if(flipHorizontally && flipVertically){
                cell.flipHorizontally = true;
                cell.rotation = Cell.ROTATE_270;
            }else if(flipHorizontally){
                cell.rotation = Cell.ROTATE_270;
            }else if(flipVertically){
                cell.rotation = Cell.ROTATE_90;
            }else{
                cell.flipVertically = true;
                cell.rotation = Cell.ROTATE_270;
            }
        }else{
            cell.rotation = flipVertically ? Cell.ROTATE_180 : 0;
            cell.flipHorizontally = flipHorizontally;
            cell.flipVertically = flipVertically;
        }
        return cell;
    }

    public static class Parameters extends AssetLoaderParameters<TiledMap>{
        /** generate mipmaps? **/
        public boolean generateMipMaps = false;
        /** The TextureFilter to use for minification **/
        public TextureFilter textureMinFilter = TextureFilter.Nearest;
        /** The TextureFilter to use for magnification **/
        public TextureFilter textureMagFilter = TextureFilter.Nearest;
        /** Whether to convert the objects' pixel position and size to the equivalent in tile space. **/
        public boolean convertObjectToTileSpace = false;
        /**
         * Whether to flip all Y coordinates so that Y positive is down. All LibGDX renderers require flipped Y coordinates, and
         * thus flipY set to true. This parameter is included for non-rendering related purposes of TMX files, or custom renderers.
         */
        public boolean flipY = true;
    }

}
