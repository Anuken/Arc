package arc.flabel.effects;

import arc.flabel.*;
import arc.math.*;

/** Makes the text jumps and falls as if there was gravity. */
public class JumpEffect extends FEffect{
    private static final float defaultFrequency = 50f, defaultDistance = 1.33f, defaultIntensity = 1f;

    public float distance = 1; // How much of their height they should move
    public float frequency = 1; // How frequently the wave pattern repeats
    public float intensity = 1; // How fast the glyphs should move

    @Override
    protected void onApply(FLabel label, FGlyph glyph, int localIndex, float delta){
        // Calculate progress
        float progressModifier = (1f / intensity) * defaultIntensity;
        float normalFrequency = (1f / frequency) * defaultFrequency;
        float progressOffset = localIndex / normalFrequency;
        float progress = calculateProgress(progressModifier, -progressOffset, false);

        // Calculate offset
        float interpolation = 0;
        float split = 0.2f;
        if(progress < split){
            interpolation = Interp.pow2Out.apply(0, 1, progress / split);
        }else{
            interpolation = Interp.bounceOut.apply(1, 0, (progress - split) / (1f - split));
        }
        float y = getLineHeight(label) * distance * interpolation * defaultDistance;

        // Calculate fadeout
        float fadeout = calculateFadeout();
        y *= fadeout;

        // Apply changes
        glyph.yoffset += y;
    }

}
