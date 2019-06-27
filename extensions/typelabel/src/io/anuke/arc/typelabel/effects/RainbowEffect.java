package io.anuke.arc.typelabel.effects;

import io.anuke.arc.graphics.Color;
import io.anuke.arc.typelabel.*;

/** Tints the text in a rainbow pattern. */
public class RainbowEffect extends Effect{
    private static final float DEFAULT_DISTANCE = 0.975f;
    private static final float DEFAULT_FREQUENCY = 2f;

    private float distance = 1; // How extensive the rainbow effect should be.
    private float frequency = 1; // How frequently the color pattern should move through the text.
    private float saturation = 1; // Color saturation
    private float brightness = 1; // Color brightness

    public RainbowEffect(TypeLabel label, String[] params){
        super(label);

        // Distance
        if(params.length > 0){
            this.distance = paramAsFloat(params[0], 1);
        }

        // Frequency
        if(params.length > 1){
            this.frequency = paramAsFloat(params[1], 1);
        }

        // Saturation
        if(params.length > 2){
            this.saturation = paramAsFloat(params[2], 1);
        }

        // Brightness
        if(params.length > 3){
            this.brightness = paramAsFloat(params[3], 1);
        }
    }

    @Override
    protected void onApply(TypingGlyph glyph, int localIndex, float delta){
        // Calculate progress
        float distanceMod = (1f / distance) * (1f - DEFAULT_DISTANCE);
        float frequencyMod = (1f / frequency) * DEFAULT_FREQUENCY;
        float progress = calculateProgress(frequencyMod, distanceMod * localIndex, false);

        // Calculate color
        if(glyph.color == null){
            glyph.color = new Color(Color.WHITE);
        }
        Color.HSVtoRGB(360f * progress, saturation * 100f, brightness * 100f, glyph.color);
    }

}
