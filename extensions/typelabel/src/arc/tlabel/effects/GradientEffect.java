package arc.tlabel.effects;

import arc.graphics.Color;
import arc.tlabel.*;

/** Tints the text in a gradient pattern. */
public class GradientEffect extends Effect{
    private static final float DEFAULT_DISTANCE = 0.975f;
    private static final float DEFAULT_FREQUENCY = 2f;

    private Color color1 = null; // First color of the gradient.
    private Color color2 = null; // Second color of the gradient.
    private float distance = 1; // How extensive the rainbow effect should be.
    private float frequency = 1; // How frequently the color pattern should move through the text.

    public GradientEffect(TypeLabel label){
        super(label);

        // Validate parameters
        if(this.color1 == null) this.color1 = new Color(Color.white);
        if(this.color2 == null) this.color2 = new Color(Color.white);
    }

    @Override
    protected void onApply(TypingGlyph glyph, int localIndex, float delta){
        // Calculate progress
        float distanceMod = (1f / distance) * (1f - DEFAULT_DISTANCE);
        float frequencyMod = (1f / frequency) * DEFAULT_FREQUENCY;
        float progress = calculateProgress(frequencyMod, distanceMod * localIndex, true);

        // Calculate color
        if(glyph.color == null) glyph.color = new Color(Color.white);
        glyph.color.set(color1).lerp(color2, progress);
    }

}
