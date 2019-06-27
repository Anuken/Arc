package io.anuke.arc.typelabel.effects;

import io.anuke.arc.math.Interpolation;
import io.anuke.arc.typelabel.*;

/** Moves the text vertically in a sine wave pattern. */
public class WaveEffect extends Effect{
    private static final float DEFAULT_FREQUENCY = 15f;
    private static final float DEFAULT_DISTANCE = 0.33f;
    private static final float DEFAULT_INTENSITY = 0.5f;

    private float distance = 1; // How much of their height they should move
    private float frequency = 1; // How frequently the wave pattern repeats
    private float intensity = 1; // How fast the glyphs should move

    public WaveEffect(TypeLabel label, String[] params){
        super(label);

        // Distance
        if(params.length > 0){
            this.distance = paramAsFloat(params[0], 1);
        }

        // Frequency
        if(params.length > 1){
            this.frequency = paramAsFloat(params[1], 1);
        }

        // Intensity
        if(params.length > 2){
            this.intensity = paramAsFloat(params[2], 1);
        }

        // Duration
        if(params.length > 3){
            this.duration = paramAsFloat(params[3], -1);
        }
    }

    @Override
    protected void onApply(TypingGlyph glyph, int localIndex, float delta){
        // Calculate progress
        float progressModifier = (1f / intensity) * DEFAULT_INTENSITY;
        float normalFrequency = (1f / frequency) * DEFAULT_FREQUENCY;
        float progressOffset = localIndex / normalFrequency;
        float progress = calculateProgress(progressModifier, progressOffset);

        // Calculate offset
        float y = getLineHeight() * distance * Interpolation.sine.apply(-1, 1, progress) * DEFAULT_DISTANCE;

        // Calculate fadeout
        float fadeout = calculateFadeout();
        y *= fadeout;

        // Apply changes
        glyph.yoffset += y;
    }

}
