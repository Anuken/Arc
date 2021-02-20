package arc.flabel.effects;

import arc.flabel.*;
import arc.graphics.Color;

/** Tints the text in a rainbow pattern. */
public class RainbowEffect extends FEffect{
    private static final float defaultDistance = 0.975f, defaultFrequency = 2f;

    public float distance = 1; // How extensive the rainbow effect should be.
    public float frequency = 1; // How frequently the color pattern should move through the text.
    public float saturation = 1; // Color saturation
    public float brightness = 1; // Color brightness

    @Override
    protected void onApply(FLabel label, FGlyph glyph, int localIndex, float delta){
        // Calculate progress
        float distanceMod = (1f / distance) * (1f - defaultDistance);
        float frequencyMod = (1f / frequency) * defaultFrequency;
        float progress = calculateProgress(frequencyMod, distanceMod * localIndex, false);

        // Calculate color
        if(glyph.color == null){
            glyph.color = new Color(Color.white);
        }
        Color.HSVtoRGB(360f * progress, saturation * 100f, brightness * 100f, glyph.color);
    }

}
