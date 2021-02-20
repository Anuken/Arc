package arc.flabel.effects;

import arc.flabel.*;
import arc.struct.FloatSeq;
import arc.math.*;

/** Shakes the text in a random pattern. */
public class ShakeEffect extends FEffect{
    private static final float DEFAULT_DISTANCE = 0.12f;
    private static final float DEFAULT_INTENSITY = 0.5f;

    private final FloatSeq lastOffsets = new FloatSeq();

    private float distance = 1; // How far the glyphs should move
    private float intensity = 1; // How fast the glyphs should move

    public ShakeEffect(FLabel label){
        super(label);
    }

    @Override
    protected void onApply(FGlyph glyph, int localIndex, float delta){
        // Make sure we can hold enough entries for the current index
        if(localIndex >= lastOffsets.size / 2){
            lastOffsets.setSize(lastOffsets.size + 16);
        }

        // Get last offsets
        float lastX = lastOffsets.get(localIndex * 2);
        float lastY = lastOffsets.get(localIndex * 2 + 1);

        // Calculate new offsets
        float x = getLineHeight() * distance * Mathf.random(-1, 1) * DEFAULT_DISTANCE;
        float y = getLineHeight() * distance * Mathf.random(-1, 1) * DEFAULT_DISTANCE;

        // Apply intensity
        float normalIntensity = Mathf.clamp(intensity * DEFAULT_INTENSITY, 0, 1);
        x = Interp.linear.apply(lastX, x, normalIntensity);
        y = Interp.linear.apply(lastY, y, normalIntensity);

        // Apply fadeout
        float fadeout = calculateFadeout();
        x *= fadeout;
        y *= fadeout;
        x = Math.round(x);
        y = Math.round(y);

        // Store offsets for the next tick
        lastOffsets.set(localIndex * 2, x);
        lastOffsets.set(localIndex * 2 + 1, y);

        // Apply changes
        glyph.xoffset += x;
        glyph.yoffset += y;
    }

}
