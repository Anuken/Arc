package arc.flabel.effects;

import arc.flabel.*;
import arc.graphics.*;

/** Blinks the entire text in two different colors at once, without interpolation. */
public class BlinkEffect extends FEffect{
    private static final float defaultFrequency = 1f;

    public Color color1 = new Color(Color.white); // First color of the effect.
    public Color color2 = new Color(Color.white); // Second color of the effect.
    public float frequency = 1; // How frequently the color pattern should move through the text.
    public float threshold = 0.5f; // Point to switch colors.

    @Override
    protected void onApply(FLabel label, FGlyph glyph, int localIndex, float delta){
        // Calculate progress
        float frequencyMod = (1f / frequency) * defaultFrequency;
        float progress = calculateProgress(frequencyMod);

        // Calculate color
        if(glyph.color == null) glyph.color = new Color(Color.white);
        glyph.color.set(progress <= threshold ? color1 : color2);
    }

}
