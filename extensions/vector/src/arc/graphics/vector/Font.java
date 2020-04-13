package arc.graphics.vector;

public abstract class Font{
    int xScale;
    int yScale;
    int xPpem;
    int yPpem;
    private int size;

    public abstract Glyph getGlyph(int code);

    public abstract Glyph getGlyph(String code);

    public abstract Glyph getGlyphFromName(String name);

    public abstract FontMetrics getFontMetrics();

    public abstract float getHorisontalKerning(Glyph first, Glyph second);

    public abstract float getVerticalKerning(Glyph first, Glyph second);

    public enum FontStyle{
        normal, italic, oblique
    }

    public enum FontVariant{
        normal, smallCaps
    }

    public enum FontWeight{
        thin, extraLight, light, normal, medium, semiBold, bold, extraBold, heavy
    }

    public enum FontStretch{
        ultraCondensed, extraCondensed, condensed, semiCondensed, normal, semiExpanded, expanded, extraExpanded, ultraExpanded
    }

    public static class FontFace{
        String fullName;
        String fontFamily;
        float fontSize;
        FontStyle fontStyle;
        FontVariant fontVariant;
        FontWeight fontWeight;
        FontStretch fontStretch;
        float underlinePosition;
        float underlineThickness;
        float strikethroughPosition;
        float strikethroughThickness;
        float overlinePosition;
        float overlineThickness;
    }

    public static abstract class Glyph{
        public final int id;
        public float advanceWidth;
        float width;
        float height;
        float xBearing;
        float yBearing;
        short xMin;
        short yMin;
        short xMax;
        short yMax;

        public Glyph(int id){
            this.id = id;
        }

        public abstract Path getOutline();

        public float getAdvanceWidth(){
            return advanceWidth;
        }
    }

    public static abstract class RasterGlyph{
        public final int id;
        float width;
        float height;
        float xBearing;
        float yBearing;

        public RasterGlyph(int id){
            this.id = id;
        }

        public abstract float getAdvanceWidth();
    }

    public static class GlyphMetrics{
        float lineHeight;
        float ascent;
        float descent;

		/*FT_Pos  width;
	    FT_Pos  height;

	    FT_Pos  horiBearingX;
	    FT_Pos  horiBearingY;
	    FT_Pos  horiAdvance;

	    FT_Pos  vertBearingX;
	    FT_Pos  vertBearingY;
	    FT_Pos  vertAdvance;*/
    }

    public static class FontMetrics{
        float lineHeight;
        float ascent;
        float descent;
        float leading;
    }

    public static class FontDescription{
        String familyName;
        FontStyle style;
        FontVariant variant;
        FontWeight weight;
        FontStretch stretch;
    }
}
