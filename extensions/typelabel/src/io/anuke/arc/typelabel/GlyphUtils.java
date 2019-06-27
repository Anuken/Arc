package io.anuke.arc.typelabel;

import io.anuke.arc.collection.Array;
import io.anuke.arc.graphics.g2d.BitmapFont.Glyph;
import io.anuke.arc.util.pooling.Pool;

/** Utility class to manage {@link Glyph} pooling and cloning. */
class GlyphUtils{
    private static final Pool<TypingGlyph> pool = new Pool<TypingGlyph>(){
        @Override
        protected TypingGlyph newObject(){
            return new TypingGlyph();
        }

        protected void reset(TypingGlyph glyph){
            GlyphUtils.reset(glyph);
        }
    };

    /**
     * Returns a glyph from this pool. The glyph may be new (from {@link Pool#newObject()}) or reused (previously {@link
     * Pool#free(Object) freed}).
     */
    static TypingGlyph obtain(){
        return pool.obtain();
    }

    /**
     * Returns a glyph from this pool and clones it from the given one. The glyph may be new (from {@link
     * Pool#newObject()}) or reused (previously {@link Pool#free(Object) freed}).
     */
    static Glyph obtainClone(Glyph from){
        TypingGlyph glyph = pool.obtain();
        clone(from, glyph);
        return glyph;
    }

    /**
     * Puts the specified glyph in the pool, making it eligible to be returned by {@link #obtain()}. If the pool already
     * contains {@link #max} free glyphs, the specified glyph is reset but not added to the pool.
     */
    static void free(TypingGlyph glyph){
        pool.free(glyph);
    }

    /**
     * Puts the specified glyphs in the pool. Null glyphs within the array are silently ignored.
     */
    static void freeAll(Array<TypingGlyph> glyphs){
        pool.freeAll(glyphs);
    }

    /** Called when a glyph is freed to clear the state of the glyph for possible later reuse. */
    static void reset(TypingGlyph glyph){
        glyph.id = 0;
        glyph.srcX = 0;
        glyph.srcY = 0;
        glyph.width = 0;
        glyph.height = 0;
        glyph.u = 0;
        glyph.v = 0;
        glyph.u2 = 0;
        glyph.v2 = 0;
        glyph.xoffset = 0;
        glyph.yoffset = 0;
        glyph.xadvance = 0;
        glyph.kerning = null;
        glyph.fixedWidth = false;

        glyph.run = null;
        glyph.internalIndex = -1;
        glyph.color = null;
    }

    /** Copies all contents from the first glyph to the second one. */
    static void clone(Glyph from, TypingGlyph to){
        to.id = from.id;
        to.srcX = from.srcX;
        to.srcY = from.srcY;
        to.width = from.width;
        to.height = from.height;
        to.u = from.u;
        to.v = from.v;
        to.u2 = from.u2;
        to.v2 = from.v2;
        to.xoffset = from.xoffset;
        to.yoffset = from.yoffset;
        to.xadvance = from.xadvance;
        to.kerning = from.kerning; // Keep the same instance, there's no reason to deep clone it
        to.fixedWidth = from.fixedWidth;

        to.run = null;
        to.internalIndex = -1;
        to.color = null;
    }

}
