package io.anuke.arc.typelabel;

import io.anuke.arc.graphics.Color;
import io.anuke.arc.graphics.g2d.BitmapFont.Glyph;
import io.anuke.arc.graphics.g2d.GlyphLayout.GlyphRun;

/** Extension of {@link Glyph} with additional data exposed to the user. */
public class TypingGlyph extends Glyph{

    /** {@link GlyphRun} this glyph belongs to. */
    public GlyphRun run = null;

    /** Internal index associated with this glyph. Internal use only. Defaults to -1. */
    int internalIndex = -1;

    /** Color of this glyph. If set to null, the run's color will be used. Defaults to null. */
    public Color color = null;

}
