package io.anuke.arc.graphics.g2d;

import io.anuke.arc.*;
import io.anuke.arc.Files.*;
import io.anuke.arc.collection.*;
import io.anuke.arc.files.*;
import io.anuke.arc.graphics.Pixmap.*;
import io.anuke.arc.graphics.*;
import io.anuke.arc.graphics.Texture.*;
import io.anuke.arc.graphics.g2d.TextureAtlas.TextureAtlasData.*;
import io.anuke.arc.scene.style.*;
import io.anuke.arc.util.*;
import io.anuke.arc.util.io.*;

import java.io.*;
import java.util.*;

import static io.anuke.arc.graphics.Texture.TextureWrap.*;

/**
 * Loads images from texture atlases created by TexturePacker.<br>
 * <br>
 * A TextureAtlas must be disposed to free up the resources consumed by the backing textures.
 * @author Nathan Sweet
 */
public class TextureAtlas implements Disposable{
    static final String[] tuple = new String[4];
    static final Comparator<Region> indexComparator = (region1, region2) -> {
        int i1 = region1.index;
        if(i1 == -1) i1 = Integer.MAX_VALUE;
        int i2 = region2.index;
        if(i2 == -1) i2 = Integer.MAX_VALUE;
        return i1 - i2;
    };
    private final ObjectSet<Texture> textures = new ObjectSet<>(4);
    private final Array<AtlasRegion> regions = new Array<>();
    private final ObjectMap<String, Drawable> drawables = new ObjectMap<>();
    private final ObjectMap<String, AtlasRegion> regionmap = new ObjectMap<>();
    protected AtlasRegion error, white;

    /** Returns a new texture atlas with only a blank texture region.*/
    public static TextureAtlas blankAtlas(){
        TextureAtlas a =  new TextureAtlas();
        a.white = new AtlasRegion(Pixmaps.blankTextureRegion());
        return a;
    }

    /** Creates an empty atlas to which regions can be added. */
    public TextureAtlas(){
    }

    /**
     * Loads the specified pack file using {@link FileType#Internal}, using the parent directory of the pack file to find the page
     * images.
     */
    public TextureAtlas(String internalPackFile){
        this(Core.files.internal(internalPackFile));
    }

    /** Loads the specified pack file, using the parent directory of the pack file to find the page images. */
    public TextureAtlas(FileHandle packFile){
        this(packFile, packFile.parent());
    }

    /**
     * @param flip If true, all regions loaded will be flipped for use with a perspective where 0,0 is the upper left corner.
     * @see #TextureAtlas(FileHandle)
     */
    public TextureAtlas(FileHandle packFile, boolean flip){
        this(packFile, packFile.parent(), flip);
    }

    public TextureAtlas(FileHandle packFile, FileHandle imagesDir){
        this(packFile, imagesDir, false);
    }

    /** @param flip If true, all regions loaded will be flipped for use with a perspective where 0,0 is the upper left corner. */
    public TextureAtlas(FileHandle packFile, FileHandle imagesDir, boolean flip){
        this(new TextureAtlasData(packFile, imagesDir, flip));
    }

    /** @param data May be null. */
    public TextureAtlas(TextureAtlasData data){
        if(data != null) load(data);
    }

    static String readValue(BufferedReader reader) throws IOException{
        String line = reader.readLine();
        int colon = line.indexOf(':');
        if(colon == -1) throw new ArcRuntimeException("Invalid line: " + line);
        return line.substring(colon + 1).trim();
    }

    /** Returns the number of tuple values read (1, 2 or 4). */
    static int readTuple(BufferedReader reader) throws IOException{
        String line = reader.readLine();
        int colon = line.indexOf(':');
        if(colon == -1) throw new ArcRuntimeException("Invalid line: " + line);
        int i = 0, lastMatch = colon + 1;
        for(i = 0; i < 3; i++){
            int comma = line.indexOf(',', lastMatch);
            if(comma == -1) break;
            tuple[i] = line.substring(lastMatch, comma).trim();
            lastMatch = comma + 1;
        }
        tuple[i] = line.substring(lastMatch).trim();
        return i + 1;
    }

    private void load(TextureAtlasData data){
        ObjectMap<Page, Texture> pageToTexture = new ObjectMap<>();
        for(Page page : data.pages){
            Texture texture = null;
            if(page.texture == null){
                texture = new Texture(page.textureFile, page.format, page.useMipMaps);
                texture.setFilter(page.minFilter, page.magFilter);
                texture.setWrap(page.uWrap, page.vWrap);
            }else{
                texture = page.texture;
                texture.setFilter(page.minFilter, page.magFilter);
                texture.setWrap(page.uWrap, page.vWrap);
            }
            textures.add(texture);
            pageToTexture.put(page, texture);
        }

        for(Region region : data.regions){
            int width = region.width;
            int height = region.height;
            AtlasRegion atlasRegion = new AtlasRegion(pageToTexture.get(region.page), region.left, region.top,
            region.rotate ? height : width, region.rotate ? width : height);
            atlasRegion.index = region.index;
            atlasRegion.name = region.name;
            atlasRegion.offsetX = region.offsetX;
            atlasRegion.offsetY = region.offsetY;
            atlasRegion.originalHeight = region.originalHeight;
            atlasRegion.originalWidth = region.originalWidth;
            atlasRegion.rotate = region.rotate;
            atlasRegion.splits = region.splits;
            atlasRegion.pads = region.pads;
            if(region.flip) atlasRegion.flip(false, true);
            regions.add(atlasRegion);
            regionmap.put(atlasRegion.name, atlasRegion);
        }

        error = find("error");
    }

    /** Adds a region to the atlas. The specified texture will be disposed when the atlas is disposed. */
    public AtlasRegion addRegion(String name, Texture texture, int x, int y, int width, int height){
        textures.add(texture);
        AtlasRegion region = new AtlasRegion(texture, x, y, width, height);
        region.name = name;
        region.originalWidth = width;
        region.originalHeight = height;
        region.index = -1;
        regions.add(region);
        regionmap.put(name, region);
        return region;
    }

    /** Adds a region to the atlas. The texture for the specified region will be disposed when the atlas is disposed. */
    public AtlasRegion addRegion(String name, TextureRegion textureRegion){
        return addRegion(name, textureRegion.texture, textureRegion.getX(), textureRegion.getY(),
        textureRegion.getWidth(), textureRegion.getHeight());
    }

    /** Returns all regions in the atlas. */
    public Array<AtlasRegion> getRegions(){
        return regions;
    }

    /** Returns the region map in the atlas. */
    public ObjectMap<String, AtlasRegion> getRegionMap(){
        return regionmap;
    }

    /** Returns the blank 1x1 texture region, if it exists.*/
    public AtlasRegion white(){
        if(white == null){
            white = find("white");
        }
        return white;
    }

    /** Finds and sets error region as name. */
    public boolean setErrorRegion(String name) {
        if(error != null || !has(name)) return false;
        error = find(name);
        return true;
    }

    public boolean isFound(TextureRegion region){
        return region != error;
    }

    /**
     * Returns the first region found with the specified name. This method's performance is no longer garbage.
     * @return The region, or the error region (if it is defined), or null.
     */
    public AtlasRegion find(String name){
        AtlasRegion r = regionmap.get(name);
        if(r == null && error == null && !(name.equals("error")))
            throw new IllegalArgumentException("The region \"" + name + "\" does not exist!");
        if(r == null) return error;
        return r;
    }

    public TextureRegion find(String name, TextureRegion def){
        TextureRegion region = regionmap.get(name);
        return region == null || region == error ? def : region;
    }

    /**
     * Returns the first region found with the specified name and index. This method uses string comparison to find the region, so
     * the result should be cached rather than calling this method multiple times.
     * @return The region, or null.
     */
    public AtlasRegion find(String name, int index){
        for(int i = 0, n = regions.size; i < n; i++){
            AtlasRegion region = regions.get(i);
            if(!region.name.equals(name)) continue;
            if(region.index != index) continue;
            return region;
        }
        return null;
    }

    public boolean has(String s){
        return regionmap.containsKey(s);
    }

    /** Always creates a new drawable by name.
     * If nothing is found, returns an 'error' texture region drawable. */
    public Drawable drawable(String name){
        if(drawables.containsKey(name)){
            return drawables.get(name);
        }

        Drawable out = null;

        if(has(name)){
            AtlasRegion region = find(name);

            if(region.splits != null){
                int[] splits = region.splits;
                NinePatch patch = new NinePatch(region, splits[0], splits[1], splits[2], splits[3]);
                int[] pads = region.pads;
                if(pads != null) patch.setPadding(pads[0], pads[1], pads[2], pads[3]);
                out = new ScaledNinePatchDrawable(patch);
            }else{
                out = new TextureRegionDrawable(region);
            }
        }

        if(error == null && out == null) throw new IllegalArgumentException("No drawable '" + name + "' found.");
        if(out == null) out = new TextureRegionDrawable(error);
        drawables.put(name, out);

        return out;
    }

    /**
     * Returns all regions with the specified name, ordered by smallest to largest {@link AtlasRegion#index index}. This method
     * uses string comparison to find the regions, so the result should be cached rather than calling this method multiple times.
     */
    public Array<AtlasRegion> findRegions(String name){
        Array<AtlasRegion> matched = new Array<>(AtlasRegion.class);
        for(int i = 0, n = regions.size; i < n; i++){
            AtlasRegion region = regions.get(i);
            if(region.name.equals(name)) matched.add(new AtlasRegion(region));
        }
        return matched;
    }

    /**
     * Returns the first region found with the specified name as a {@link NinePatch}. The region must have been packed with
     * ninepatch splits. This method uses string comparison to find the region and constructs a new ninepatch, so the result should
     * be cached rather than calling this method multiple times.
     * @return The ninepatch, or null.
     */
    public NinePatch createPatch(String name){
        for(int i = 0, n = regions.size; i < n; i++){
            AtlasRegion region = regions.get(i);
            if(region.name.equals(name)){
                int[] splits = region.splits;
                if(splits == null) throw new IllegalArgumentException("Region does not have ninepatch splits: " + name);
                NinePatch patch = new NinePatch(region, splits[0], splits[1], splits[2], splits[3]);
                if(region.pads != null)
                    patch.setPadding(region.pads[0], region.pads[1], region.pads[2], region.pads[3]);
                return patch;
            }
        }
        return null;
    }

    /** @return the textures of the pages, unordered */
    public ObjectSet<Texture> getTextures(){
        return textures;
    }

    /** @return the first texture of the pages.*/
    public Texture texture(){
        return textures.first();
    }

    /**
     * Releases all resources associated with this TextureAtlas instance. This releases all the textures backing all TextureRegions
     * and Sprites, which should no longer be used after calling dispose.
     */
    public void dispose(){
        for(Texture texture : textures)
            texture.dispose();
        textures.clear();
    }

    public static class TextureAtlasData{
        final Array<Page> pages = new Array<>();
        final Array<Region> regions = new Array<>();

        public TextureAtlasData(FileHandle packFile, FileHandle imagesDir, boolean flip){
            BufferedReader reader = new BufferedReader(new InputStreamReader(packFile.read()), 64);
            try{
                Page pageImage = null;
                while(true){
                    String line = reader.readLine();
                    if(line == null) break;
                    if(line.trim().length() == 0)
                        pageImage = null;
                    else if(pageImage == null){
                        FileHandle file = imagesDir.child(line);

                        float width = 0, height = 0;
                        if(readTuple(reader) == 2){ // size is only optional for an atlas packed with an old TexturePacker.
                            width = Integer.parseInt(tuple[0]);
                            height = Integer.parseInt(tuple[1]);
                            readTuple(reader);
                        }
                        Format format = Format.valueOf(tuple[0]);

                        readTuple(reader);
                        TextureFilter min = TextureFilter.valueOf(tuple[0]);
                        TextureFilter max = TextureFilter.valueOf(tuple[1]);

                        String direction = readValue(reader);
                        TextureWrap repeatX = ClampToEdge;
                        TextureWrap repeatY = ClampToEdge;
                        if(direction.equals("x"))
                            repeatX = Repeat;
                        else if(direction.equals("y"))
                            repeatY = Repeat;
                        else if(direction.equals("xy")){
                            repeatX = Repeat;
                            repeatY = Repeat;
                        }

                        pageImage = new Page(file, width, height, min.isMipMap(), format, min, max, repeatX, repeatY);
                        pages.add(pageImage);
                    }else{
                        boolean rotate = Boolean.valueOf(readValue(reader));

                        readTuple(reader);
                        int left = Integer.parseInt(tuple[0]);
                        int top = Integer.parseInt(tuple[1]);

                        readTuple(reader);
                        int width = Integer.parseInt(tuple[0]);
                        int height = Integer.parseInt(tuple[1]);

                        Region region = new Region();
                        region.page = pageImage;
                        region.left = left;
                        region.top = top;
                        region.width = width;
                        region.height = height;
                        region.name = line;
                        region.rotate = rotate;

                        if(readTuple(reader) == 4){ // split is optional
                            region.splits = new int[]{Integer.parseInt(tuple[0]), Integer.parseInt(tuple[1]),
                            Integer.parseInt(tuple[2]), Integer.parseInt(tuple[3])};

                            if(readTuple(reader) == 4){ // pad is optional, but only present with splits
                                region.pads = new int[]{Integer.parseInt(tuple[0]), Integer.parseInt(tuple[1]),
                                Integer.parseInt(tuple[2]), Integer.parseInt(tuple[3])};

                                readTuple(reader);
                            }
                        }

                        region.originalWidth = Integer.parseInt(tuple[0]);
                        region.originalHeight = Integer.parseInt(tuple[1]);

                        readTuple(reader);
                        region.offsetX = Integer.parseInt(tuple[0]);
                        region.offsetY = Integer.parseInt(tuple[1]);

                        region.index = Integer.parseInt(readValue(reader));

                        if(flip) region.flip = true;

                        regions.add(region);
                    }
                }
            }catch(Exception ex){
                throw new ArcRuntimeException("Error reading pack file: " + packFile, ex);
            }finally{
                Streams.closeQuietly(reader);
            }

            regions.sort(indexComparator);
        }

        public Array<Page> getPages(){
            return pages;
        }

        public Array<Region> getRegions(){
            return regions;
        }

        public static class Page{
            public final FileHandle textureFile;
            public final float width, height;
            public final boolean useMipMaps;
            public final Format format;
            public final TextureFilter minFilter;
            public final TextureFilter magFilter;
            public final TextureWrap uWrap;
            public final TextureWrap vWrap;
            public Texture texture;

            public Page(FileHandle handle, float width, float height, boolean useMipMaps, Format format, TextureFilter minFilter,
                        TextureFilter magFilter, TextureWrap uWrap, TextureWrap vWrap){
                this.width = width;
                this.height = height;
                this.textureFile = handle;
                this.useMipMaps = useMipMaps;
                this.format = format;
                this.minFilter = minFilter;
                this.magFilter = magFilter;
                this.uWrap = uWrap;
                this.vWrap = vWrap;
            }
        }

        public static class Region{
            public Page page;
            public int index;
            public String name;
            public float offsetX;
            public float offsetY;
            public int originalWidth;
            public int originalHeight;
            public boolean rotate;
            public int left;
            public int top;
            public int width;
            public int height;
            public boolean flip;
            public int[] splits;
            public int[] pads;
        }
    }

    /** Describes the region of a packed image and provides information about the original image before it was packed. */
    public static class AtlasRegion extends TextureRegion{
        /**
         * The number at the end of the original image file name, or -1 if none.<br>
         * <br>
         * When sprites are packed, if the original file name ends with a number, it is stored as the index and is not considered as
         * part of the sprite's name. This is useful for keeping animation frames in order.
         * @see TextureAtlas#findRegions(String)
         */
        public int index;

        /**
         * The name of the original image file, up to the first underscore. Underscores denote special instructions to the texture
         * packer.
         */
        public String name;

        /** The offset from the left of the original image to the left of the packed image, after whitespace was removed for packing. */
        public float offsetX;

        /**
         * The offset from the bottom of the original image to the bottom of the packed image, after whitespace was removed for
         * packing.
         */
        public float offsetY;

        /** The width of the image, after whitespace was removed for packing. */
        public int packedWidth;

        /** The height of the image, after whitespace was removed for packing. */
        public int packedHeight;

        /** The width of the image, before whitespace was removed and rotation was applied for packing. */
        public int originalWidth;

        /** The height of the image, before whitespace was removed for packing. */
        public int originalHeight;

        /** If true, the region has been rotated 90 degrees counter clockwise. */
        public boolean rotate;

        /** The ninepatch splits, or null if not a ninepatch. Has 4 elements: left, right, top, bottom. */
        public int[] splits;

        /** The ninepatch pads, or null if not a ninepatch or the has no padding. Has 4 elements: left, right, top, bottom. */
        public int[] pads;

        public AtlasRegion(Texture texture, int x, int y, int width, int height){
            super(texture, x, y, width, height);
            originalWidth = width;
            originalHeight = height;
            packedWidth = width;
            packedHeight = height;
        }

        public AtlasRegion(){

        }

        public AtlasRegion(AtlasRegion region){
            set(region);
            index = region.index;
            name = region.name;
            offsetX = region.offsetX;
            offsetY = region.offsetY;
            packedWidth = region.packedWidth;
            packedHeight = region.packedHeight;
            originalWidth = region.originalWidth;
            originalHeight = region.originalHeight;
            rotate = region.rotate;
            splits = region.splits;
        }

        public AtlasRegion(TextureRegion region){
            set(region);
            name = "unknown";
        }

        @Override
        /** Flips the region, adjusting the offset so the image appears to be flip as if no whitespace has been removed for packing. */
        public void flip(boolean x, boolean y){
            super.flip(x, y);
            if(x) offsetX = originalWidth - offsetX - getRotatedPackedWidth();
            if(y) offsetY = originalHeight - offsetY - getRotatedPackedHeight();
        }

        /**
         * Returns the packed width considering the rotate value, if it is true then it returns the packedHeight, otherwise it
         * returns the packedWidth.
         */
        public float getRotatedPackedWidth(){
            return rotate ? packedHeight : packedWidth;
        }

        /**
         * Returns the packed height considering the rotate value, if it is true then it returns the packedWidth, otherwise it
         * returns the packedHeight.
         */
        public float getRotatedPackedHeight(){
            return rotate ? packedWidth : packedHeight;
        }

        public String toString(){
            return name;
        }
    }
}
