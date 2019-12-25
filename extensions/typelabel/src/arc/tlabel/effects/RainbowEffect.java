package arc.tlabel.effects;

import arc.graphics.Color;
import arc.tlabel.*;

/** Tints the text in a rainbow pattern. */
public class RainbowEffect extends Effect{
    private static final float DEFAULT_DISTANCE = 0.975f;
    private static final float DEFAULT_FREQUENCY = 2f;

    private float distance = 1; // How extensive the rainbow effect should be.
    private float frequency = 1; // How frequently the color pattern should move through the text.
    private float saturation = 1; // Color saturation
    private float brightness = 1; // Color brightness

    public RainbowEffect(TypeLabel label){
        super(label);
    }

    @Override
    protected void onApply(TypingGlyph glyph, int localIndex, float delta){
        // Calculate progress
        float distanceMod = (1f / distance) * (1f - DEFAULT_DISTANCE);
        float frequencyMod = (1f / frequency) * DEFAULT_FREQUENCY;
        float progress = calculateProgress(frequencyMod, distanceMod * localIndex, false);

        // Calculate color
        if(glyph.color == null){
            glyph.color = new Color(Color.white);
        }
        Color.HSVtoRGB(360f * progress, saturation * 100f, brightness * 100f, glyph.color);
    }

}
