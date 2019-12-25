package arc.tlabel.effects;

import arc.tlabel.*;
import arc.util.noise.Simplex;

/** Moves the text in a wind pattern. */
public class WindEffect extends Effect{
    private static final float DEFAULT_SPACING = 10f;
    private static final float DEFAULT_DISTANCE = 0.33f;
    private static final float DEFAULT_INTENSITY = 0.375f;
    private static final float DISTANCE_X_RATIO = 1.5f;
    private static final float DISTANCE_Y_RATIO = 1.0f;

    private Simplex noise = new Simplex(1);
    private float noiseCursorX = 0;
    private float noiseCursorY = 0;

    private float distanceX = 1; // How much of their line height glyphs should move in the X axis
    private float distanceY = 1; // How much of their line height glyphs should move in the Y axis
    private float spacing = 1; // How much space there should be between waves
    private float intensity = 1; // How strong the wind should be

    public WindEffect(TypeLabel label){
        super(label);
    }

    @Override
    public void update(float delta){
        super.update(delta);

        // Update noise cursor
        noiseCursorX += 0.1f * intensity * DEFAULT_INTENSITY;
        noiseCursorY += 0.1f * intensity * DEFAULT_INTENSITY;
    }

    @Override
    protected void onApply(TypingGlyph glyph, int localIndex, float delta){
        // Calculate progress
        float progressModifier = (1f / intensity) * DEFAULT_INTENSITY;
        float normalSpacing = (1f / spacing) * DEFAULT_SPACING;
        float progressOffset = localIndex / normalSpacing;
        float progress = calculateProgress(progressModifier, progressOffset);

        // Calculate noise
        float indexOffset = localIndex * 0.05f * spacing;
        float noiseX = (float) noise.octaveNoise2D(6, 0, 1f, noiseCursorX + indexOffset, 0);
        float noiseY = (float) noise.octaveNoise2D(6, 0, 1f, noiseCursorY + indexOffset, 0);

        // Calculate offset
        float lineHeight = getLineHeight();
        float x = lineHeight * noiseX * progress * distanceX * DISTANCE_X_RATIO * DEFAULT_DISTANCE;
        float y = lineHeight * noiseY * progress * distanceY * DISTANCE_Y_RATIO * DEFAULT_DISTANCE;

        // Calculate fadeout
        float fadeout = calculateFadeout();
        x *= fadeout;
        y *= fadeout;

        // Add flag effect to X offset
        x = Math.abs(x) * -Math.signum(distanceX);

        // Apply changes
        glyph.xoffset += x;
        glyph.yoffset += y;
    }

}
