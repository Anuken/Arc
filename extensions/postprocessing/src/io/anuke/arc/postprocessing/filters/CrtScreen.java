package io.anuke.arc.postprocessing.filters;

import io.anuke.arc.graphics.Color;
import io.anuke.arc.math.Mathf;
import io.anuke.arc.postprocessing.PostFilter;

public final class CrtScreen extends PostFilter{
    public float elapsedSecs, offset, zoom;
    public float cdRedCyan, cdBlueYellow;
    public final Color tint;
    public float distortion;
    public boolean dodistortion;
    public RgbMode mode;

    public CrtScreen(boolean barrelDistortion, RgbMode mode, int effectsSupport){
        super("screenspace", "crt-screen", (barrelDistortion ? "#define ENABLE_BARREL_DISTORTION\n" : "")
        + (mode == RgbMode.RgbShift ? "#define ENABLE_RGB_SHIFT\n" : "")
        + (mode == RgbMode.ChromaticAberrations ? "#define ENABLE_CHROMATIC_ABERRATIONS\n" : "")
        + (isSet(Effect.TweakContrast.v, effectsSupport) ? "#define ENABLE_TWEAK_CONTRAST\n" : "")
        + (isSet(Effect.Vignette.v, effectsSupport) ? "#define ENABLE_VIGNETTE\n" : "")
        + (isSet(Effect.Tint.v, effectsSupport) ? "#define ENABLE_TINT\n" : "")
        + (isSet(Effect.Scanlines.v, effectsSupport) ? "#define ENABLE_SCANLINES\n" : "")
        + (isSet(Effect.PhosphorVibrance.v, effectsSupport) ? "#define ENABLE_PHOSPHOR_VIBRANCE\n" : "")
        + (isSet(Effect.ScanDistortion.v, effectsSupport) ? "#define ENABLE_SCAN_DISTORTION\n" : "")
        );

        dodistortion = barrelDistortion;

        tint = new Color();

        tint.set(1f, 1f, 0.85f);
        distortion = 0.3f;
        zoom = 1f;
        this.mode = mode;
        cdRedCyan = cdBlueYellow = 0.1f;
        offset = 0.003f;
    }

    @Override
    protected void update(){
        shader.setUniformf("time", elapsedSecs % Mathf.PI);
        shader.setUniformf("tint", tint.r, tint.g, tint.b);

        if(mode == RgbMode.RgbShift){
            shader.setUniformf("offset", offset);
        }

        if(mode == RgbMode.ChromaticAberrations){
            shader.setUniformf("chromaticDispersion", cdRedCyan, cdBlueYellow);
        }

        if(dodistortion){
            shader.setUniformf("Distortion", distortion);
            shader.setUniformf("zoom", zoom);
        }
    }

    private static boolean isSet(int flag, int flags){
        return (flags & flag) == flag;
    }

    public enum RgbMode{
        None(0), RgbShift(1), ChromaticAberrations(2);

        public int v;

        RgbMode(int value){
            this.v = value;
        }
    }

    public enum Effect{
        None(0), TweakContrast(1), Vignette(2), Tint(4), Scanlines(8), PhosphorVibrance(16), ScanDistortion(32);

        public int v;

        Effect(int value){
            this.v = value;
        }
    }
}
