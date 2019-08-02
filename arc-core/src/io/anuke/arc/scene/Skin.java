package io.anuke.arc.scene;

import io.anuke.arc.collection.Array;
import io.anuke.arc.collection.ObjectMap;
import io.anuke.arc.files.FileHandle;
import io.anuke.arc.graphics.Color;
import io.anuke.arc.graphics.Texture;
import io.anuke.arc.graphics.g2d.BitmapFont;
import io.anuke.arc.graphics.g2d.NinePatch;
import io.anuke.arc.graphics.g2d.TextureAtlas;
import io.anuke.arc.graphics.g2d.TextureAtlas.AtlasRegion;
import io.anuke.arc.graphics.g2d.TextureRegion;
import io.anuke.arc.scene.style.*;
import io.anuke.arc.scene.style.SkinReader.ValueReader;
import io.anuke.arc.util.ArcRuntimeException;
import io.anuke.arc.util.Disposable;
import io.anuke.arc.util.serialization.JsonReader;
import io.anuke.arc.util.serialization.JsonValue;
import io.anuke.arc.util.serialization.SerializationException;

/**
 * A skin stores resources for UI widgets to use (texture regions, ninepatches, fonts, colors, etc). Resources are named and can
 * be looked up by name and type. Resources can be described in JSON. Skin provides useful conversions, such as allowing access to
 * regions in the atlas as ninepatches, sprites, drawables, etc. The get* methods return an instance of the object in the skin.
 * The new* methods return a copy of an instance in the skin.
 */
@SuppressWarnings("unchecked")
public class Skin implements Disposable{
    ObjectMap<Class, ObjectMap<String, Object>> resources = new ObjectMap<>();
    TextureAtlas atlas;
    FileHandle file;

    /** Creates an empty skin. */
    public Skin(){
    }

    /**
     * Creates a skin containing the resources in the specified skin JSON file. If a file in the same directory with a ".atlas"
     * extension exists, it is loaded as a {@link TextureAtlas} and the texture regions added to the skin. The atlas is
     * automatically disposed when the skin is disposed.
     */
    public Skin(FileHandle skinFile){
        FileHandle atlasFile = skinFile.sibling(skinFile.nameWithoutExtension() + ".atlas");
        if(atlasFile.exists()){
            atlas = new TextureAtlas(atlasFile);
            addRegions(atlas);
        }

        load(skinFile);
    }

    /**
     * Creates a skin containing the resources in the specified skin JSON file and the texture regions from the specified atlas.
     * The atlas is automatically disposed when the skin is disposed.
     */
    public Skin(FileHandle skinFile, TextureAtlas atlas){
        this.atlas = atlas;
        addRegions(atlas);
        load(skinFile);
    }

    /**
     * Creates a skin containing the texture regions from the specified atlas. The atlas is automatically disposed when the skin
     * is disposed.
     */
    public Skin(TextureAtlas atlas){
        this.atlas = atlas;
        addRegions(atlas);
    }

    public FileHandle getFile(){
        return file;
    }

    public BitmapFont font(){
        return getFont("default");
    }

    /** Adds all resources in the specified skin JSON file. */
    public void load(FileHandle skinFile){
        this.file = skinFile;

        JsonReader reader = new JsonReader();
        JsonValue value = reader.parse(skinFile).child;

        while(value != null){
            String type = value.name;

            ValueReader<?> valreader = SkinReader.getReader(type);
            if(valreader == null) throw new SerializationException("Unknown type: " + type);
            JsonValue child = value.child;
            while(child != null){
                Object result = valreader.read(this, child);
                add(child.name, result, result.getClass());

                if(result.getClass() != Drawable.class && result instanceof Drawable)
                    add(child.name, result, Drawable.class);

                child = child.next;
            }

            value = value.next;
        }
    }

    /** Adds all named texture regions from the atlas. The atlas will not be automatically disposed when the skin is disposed. */
    public void addRegions(TextureAtlas atlas){
        Array<AtlasRegion> regions = atlas.getRegions();
        for(int i = 0, n = regions.size; i < n; i++){
            AtlasRegion region = regions.get(i);
            String name = region.name;
            if(region.index != -1){
                name += "_" + region.index;
            }
            add(name, region, TextureRegion.class);
        }
    }

    public void add(String name, Object resource){
        add(name, resource, resource.getClass());
    }

    public void add(String name, Object resource, Class type){
        if(name == null) throw new IllegalArgumentException("name cannot be null.");
        if(resource == null) throw new IllegalArgumentException("resource cannot be null.");
        ObjectMap<String, Object> typeResources = resources.get(type);
        if(typeResources == null){
            typeResources = new ObjectMap<>(type == TextureRegion.class || type == Drawable.class ? 256 : 64);
            resources.put(type, typeResources);
        }
        typeResources.put(name, resource);
    }

    public void remove(String name, Class type){
        if(name == null) throw new IllegalArgumentException("name cannot be null.");
        ObjectMap<String, Object> typeResources = resources.get(type);
        typeResources.remove(name);
    }

    /**
     * Returns a resource named "default" for the specified type.
     * @throws ArcRuntimeException if the resource was not found.
     */
    public <T> T get(Class<T> type){
        return get("default", type);
    }

    /**
     * Returns a named resource of the specified type.
     * @throws ArcRuntimeException if the resource was not found.
     */
    public <T> T get(String name, Class<T> type){
        if(name == null) throw new IllegalArgumentException("name cannot be null.");
        if(type == null) throw new IllegalArgumentException("type cannot be null.");

        if(type == Drawable.class) return (T)getDrawable(name);
        if(type == TextureRegion.class) return (T)getRegion(name);
        if(type == NinePatch.class) return (T)getPatch(name);

        ObjectMap<String, Object> typeResources = resources.get(type);
        if(typeResources == null)
            throw new ArcRuntimeException("No " + type.getName() + " registered with name: " + name);
        Object resource = typeResources.get(name);
        if(resource == null) throw new ArcRuntimeException("No " + type.getName() + " registered with name: " + name);
        return (T)resource;
    }

    /**
     * Returns a named resource of the specified type.
     * @return null if not found.
     */
    public <T> T optional(String name, Class<T> type){
        if(name == null) throw new IllegalArgumentException("name cannot be null.");
        if(type == null) throw new IllegalArgumentException("type cannot be null.");
        ObjectMap<String, Object> typeResources = resources.get(type);
        if(typeResources == null) return null;
        return (T)typeResources.get(name);
    }

    public boolean has(String name, Class type){
        ObjectMap<String, Object> typeResources = resources.get(type);
        return typeResources != null && typeResources.containsKey(name);
    }

    /** Returns the name to resource mapping for the specified type, or null if no resources of that type exist. */
    public <T> ObjectMap<String, T> getAll(Class<T> type){
        return (ObjectMap<String, T>)resources.get(type);
    }

    public Color getColor(String name){
        return get(name, Color.class);
    }

    public BitmapFont getFont(String name){
        return get(name, BitmapFont.class);
    }

    /**
     * Returns a registered texture region. If no region is found but a texture exists with the name, a region is created from the
     * texture and stored in the skin.
     */
    public TextureRegion getRegion(String name){
        TextureRegion region = optional(name, TextureRegion.class);
        if(region != null) return region;

        Texture texture = optional(name, Texture.class);
        if(texture == null) throw new ArcRuntimeException("No TextureRegion or Texture registered with name: " + name);
        region = new TextureRegion(texture);
        add(name, region, TextureRegion.class);
        return region;
    }

    /** @return an array with the {@link TextureRegion} that have an index != -1, or null if none are found. */
    public Array<TextureRegion> getRegions(String regionName){
        Array<TextureRegion> regions = null;
        int i = 0;
        TextureRegion region = optional(regionName + "_" + (i++), TextureRegion.class);
        if(region != null){
            regions = new Array<>();
            while(region != null){
                regions.add(region);
                region = optional(regionName + "_" + (i++), TextureRegion.class);
            }
        }
        return regions;
    }

    /**
     * Returns a registered tiled drawable. If no tiled drawable is found but a region exists with the name, a tiled drawable is
     * created from the region and stored in the skin.
     */
    public TiledDrawable getTiledDrawable(String name){
        TiledDrawable tiled = optional(name, TiledDrawable.class);
        if(tiled != null) return tiled;

        tiled = new TiledDrawable(getRegion(name));
        tiled.setName(name);
        add(name, tiled, TiledDrawable.class);
        return tiled;
    }

    /**
     * Returns a registered ninepatch. If no ninepatch is found but a region exists with the name, a ninepatch is created from the
     * region and stored in the skin. If the region is an {@link AtlasRegion} then the {@link AtlasRegion#splits} are used,
     * otherwise the ninepatch will have the region as the center patch.
     */
    public NinePatch getPatch(String name){
        NinePatch patch = optional(name, NinePatch.class);
        if(patch != null) return patch;

        try{
            TextureRegion region = getRegion(name);
            if(region instanceof AtlasRegion){
                int[] splits = ((AtlasRegion)region).splits;
                if(splits != null){
                    patch = new NinePatch(region, splits[0], splits[1], splits[2], splits[3]);
                    int[] pads = ((AtlasRegion)region).pads;
                    if(pads != null) patch.setPadding(pads[0], pads[1], pads[2], pads[3]);
                }
            }
            if(patch == null) patch = new NinePatch(region);
            add(name, patch, NinePatch.class);
            return patch;
        }catch(ArcRuntimeException ex){
            throw new ArcRuntimeException("No NinePatch, TextureRegion, or Texture registered with name: " + name);
        }
    }

    /**
     * Returns a registered drawable. If no drawable is found but a region, ninepatch, or sprite exists with the name, then the
     * appropriate drawable is created and stored in the skin.
     */
    public Drawable getDrawable(String name){
        Drawable drawable = optional(name, Drawable.class);
        if(drawable != null) return drawable;

        // Use texture or texture region. If it has splits, use ninepatch. If it has rotation or whitespace stripping, use sprite.
        try{
            TextureRegion textureRegion = getRegion(name);
            if(textureRegion instanceof AtlasRegion){
                AtlasRegion region = (AtlasRegion)textureRegion;
                if(region.splits != null){
                    drawable = new ScaledNinePatchDrawable(getPatch(name));
                }
            }
            if(drawable == null) drawable = new TextureRegionDrawable(textureRegion);
        }catch(ArcRuntimeException ignored){
        }

        // Check for explicit registration of ninepatch, sprite, or tiled drawable.
        if(drawable == null){
            NinePatch patch = optional(name, NinePatch.class);
            if(patch != null){
                drawable = new NinePatchDrawable(patch);
            }else{
                throw new ArcRuntimeException("No Drawable, NinePatch, TextureRegion, or Texture registered with name: " + name);
            }
        }

        ((BaseDrawable)drawable).setName(name);

        add(name, drawable, Drawable.class);
        return drawable;
    }

    /**
     * Returns the name of the specified style object, or null if it is not in the skin. This compares potentially every style
     * object in the skin of the same type as the specified style, which may be a somewhat expensive operation.
     */
    public String find(Object resource){
        if(resource == null) throw new IllegalArgumentException("style cannot be null.");
        ObjectMap<String, Object> typeResources = resources.get(resource.getClass());
        if(typeResources == null) return null;
        return typeResources.findKey(resource, true);
    }

    /** Returns a copy of a drawable found in the skin via {@link #getDrawable(String)}. */
    public Drawable newDrawable(String name){
        return newDrawable(getDrawable(name));
    }

    /** Returns a tinted copy of a drawable found in the skin via {@link #getDrawable(String)}. */
    public Drawable newDrawable(String name, float r, float g, float b, float a){
        return newDrawable(getDrawable(name), new Color(r, g, b, a));
    }

    /** Returns a tinted copy of a drawable found in the skin via {@link #getDrawable(String)}. */
    public Drawable newDrawable(String name, Color tint){
        return newDrawable(getDrawable(name), tint);
    }

    /** Returns a copy of the specified drawable. */
    public Drawable newDrawable(Drawable drawable){
        if(drawable instanceof TiledDrawable) return new TiledDrawable((TiledDrawable)drawable);
        if(drawable instanceof TextureRegionDrawable) return new TextureRegionDrawable((TextureRegionDrawable)drawable);
        if(drawable instanceof NinePatchDrawable) return new NinePatchDrawable((NinePatchDrawable)drawable);
        throw new ArcRuntimeException("Unable to copy, unknown drawable type: " + drawable.getClass());
    }

    /** Returns a tinted copy of a drawable found in the skin via {@link #getDrawable(String)}. */
    public Drawable newDrawable(Drawable drawable, float r, float g, float b, float a){
        return newDrawable(drawable, new Color(r, g, b, a));
    }

    /** Returns a tinted copy of a drawable found in the skin via {@link #getDrawable(String)}. */
    public Drawable newDrawable(Drawable drawable, Color tint){
        Drawable newDrawable;
        if(drawable instanceof TextureRegionDrawable)
            newDrawable = ((TextureRegionDrawable)drawable).tint(tint);
        else if(drawable instanceof NinePatchDrawable)
            newDrawable = ((NinePatchDrawable)drawable).tint(tint);
        else
            throw new ArcRuntimeException("Unable to copy, unknown drawable type: " + drawable.getClass());

        if(newDrawable instanceof BaseDrawable){
            BaseDrawable named = (BaseDrawable)newDrawable;
            if(drawable instanceof BaseDrawable)
                named.setName(((BaseDrawable)drawable).getName() + " (" + tint + ")");
            else
                named.setName(" (" + tint + ")");
        }

        return newDrawable;
    }

    /** Returns the {@link TextureAtlas} passed to this skin constructor, or null. */
    public TextureAtlas getAtlas(){
        return atlas;
    }

    /** Disposes the {@link TextureAtlas} and all {@link Disposable} resources in the skin. */
    public void dispose(){
        if(atlas != null) atlas.dispose();
        for(ObjectMap<String, Object> entry : resources.values()){
            for(Object resource : entry.values())
                if(resource instanceof Disposable) ((Disposable)resource).dispose();
        }
    }
}
