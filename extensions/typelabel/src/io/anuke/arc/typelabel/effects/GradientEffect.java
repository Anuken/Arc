package io.anuke.arc.typelabel.effects;

import io.anuke.arc.graphics.Color;
import io.anuke.arc.typelabel.*;

/** Tints the text in a gradient pattern. */
public class GradientEffect extends Effect{
    private static final float DEFAULT_DISTANCE = 0.975f;
    private static final float DEFAULT_FREQUENCY = 2f;

    private Color color1 = null; // First color of the gradient.
    private Color color2 = null; // Second color of the gradient.
    private float distance = 1; // How extensive the rainbow effect should be.
    private float frequency = 1; // How frequently the color pattern should move through the text.

    public GradientEffect(TypeLabel label, String[] params){
        super(label);

        // Color 1
        if(params.length > 0){
            this.color1 = paramAsColor(params[0]);
        }

        // Color 2
        if(params.length > 1){
            this.color2 = paramAsColor(params[1]);
        }

        // Distance
        if(params.length > 2){
            this.distance = paramAsFloat(params[2], 1);
        }

        // Frequency
        if(params.length > 3){
            this.frequency = paramAsFloat(params[3], 1);
        }

        // Validate parameters
        if(this.color1 == null) this.color1 = new Color(Color.WHITE);
        if(this.color2 == null) this.color2 = new Color(Color.WHITE);
    }

    @Override
    protected void onApply(TypingGlyph glyph, int localIndex, float delta){
        // Calculate progress
        float distanceMod = (1f / distance) * (1f - DEFAULT_DISTANCE);
        float frequencyMod = (1f / frequency) * DEFAULT_FREQUENCY;
        float progress = calculateProgress(frequencyMod, distanceMod * localIndex, true);

        // Calculate color
        if(glyph.color == null) glyph.color = new Color(Color.WHITE);
        glyph.color.set(color1).lerp(color2, progress);
    }

}
