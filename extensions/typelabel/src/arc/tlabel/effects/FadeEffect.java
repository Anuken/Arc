package arc.tlabel.effects;

import arc.struct.IntFloatMap;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.tlabel.*;

/** Fades the text's color from between colors or alphas. Doesn't repeat itself. */
public class FadeEffect extends Effect{
    private Color color1 = null; // First color of the effect.
    private Color color2 = null; // Second color of the effect.
    private float alpha1 = 0; // First alpha of the effect, in case a color isn't provided.
    private float alpha2 = 1; // Second alpha of the effect, in case a color isn't provided.
    private float fadeDuration = 1; // Duration of the fade effect

    private IntFloatMap timePassedByGlyphIndex = new IntFloatMap();

    public FadeEffect(TypeLabel label){
        super(label);
    }

    @Override
    protected void onApply(TypingGlyph glyph, int localIndex, float delta){
        // Calculate progress
        float timePassed = timePassedByGlyphIndex.getAndIncrement(localIndex, 0, delta);
        float progress = timePassed / fadeDuration;
        if(progress < 0 || progress > 1){
            return;
        }

        // Create glyph color if necessary
        if(glyph.color == null){
            glyph.color = new Color(glyph.run.color);
        }

        // Calculate initial color
        if(this.color1 == null){
            glyph.color.a = Mathf.lerp(glyph.color.a, this.alpha1, 1f - progress);
        }else{
            glyph.color.lerp(this.color1, 1f - progress);
        }

        // Calculate final color
        if(this.color2 == null){
            glyph.color.a = Mathf.lerp(glyph.color.a, this.alpha2, progress);
        }else{
            glyph.color.lerp(this.color2, progress);
        }
    }

}
