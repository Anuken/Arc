package arc.flabel.effects;

import arc.flabel.*;
import arc.graphics.Color;

/** Tints the text in a gradient pattern. */
public class GradientEffect extends FEffect{
    private static final float defaultDistance = 0.975f;
    private static final float defaultFrequency = 2f;

    public Color color1 = new Color(Color.white); // First color of the gradient.
    public Color color2 = new Color(Color.white); // Second color of the gradient.
    public float distance = 1; // How extensive the rainbow effect should be.
    public float frequency = 1; // How frequently the color pattern should move through the text.

    @Override
    protected void onApply(FLabel label, FGlyph glyph, int localIndex, float delta){
        // Calculate progress
        float distanceMod = (1f / distance) * (1f - defaultDistance);
        float frequencyMod = (1f / frequency) * defaultFrequency;
        float progress = calculateProgress(frequencyMod, distanceMod * localIndex, true);

        // Calculate color
        if(glyph.color == null) glyph.color = new Color(Color.white);
        glyph.color.set(color1).lerp(color2, progress);
    }

}
