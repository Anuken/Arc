package arc.tlabel.effects;

import arc.struct.IntFloatMap;
import arc.math.Interpolation;
import arc.tlabel.*;

/** Moves the text vertically easing it into the final position. Doesn't repeat itself. */
public class EaseEffect extends Effect{
    private static final float DEFAULT_DISTANCE = 0.15f;
    private static final float DEFAULT_INTENSITY = 0.075f;

    private float distance = 1; // How much of their height they should move
    private float intensity = 1; // How fast the glyphs should move
    private boolean elastic = false; // Whether or not the glyphs have an elastic movement

    private IntFloatMap timePassedByGlyphIndex = new IntFloatMap();

    public EaseEffect(TypeLabel label){
        super(label);
    }

    @Override
    protected void onApply(TypingGlyph glyph, int localIndex, float delta){
        // Calculate real intensity
        float realIntensity = intensity * (elastic ? 3f : 1f) * DEFAULT_INTENSITY;

        // Calculate progress
        float timePassed = timePassedByGlyphIndex.getAndIncrement(localIndex, 0, delta);
        float progress = timePassed / realIntensity;
        if(progress < 0 || progress > 1){
            return;
        }

        // Calculate offset
        Interpolation interpolation = elastic ? Interpolation.swingOut : Interpolation.sine;
        float interpolatedValue = interpolation.apply(1, 0, progress);
        float y = getLineHeight() * distance * interpolatedValue * DEFAULT_DISTANCE;

        // Apply changes
        glyph.yoffset += y;
    }

}
