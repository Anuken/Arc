package io.anuke.arc.postprocessing.filters;

import arc.graphics.Texture;
import io.anuke.arc.postprocessing.PostFilter;

public class Vignetting extends PostFilter{
    public float x = 0.8f, y = 0.25f;
    public float intensity = 1f, saturation = 1f, saturationMul = 1f;
    public Texture texLut;
    public boolean dolut, dosat;
    public float lutintensity = 1f;
    public int[] lutindex = {-1, -1};
    public float lutStep, lutStepOffset, lutIndexOffset;
    public float centerX = 0.5f, centerY = 0.5f;

    public Vignetting(boolean controlSaturation){
        super("default", "vignetting", (controlSaturation ? "#define CONTROL_SATURATION\n#define ENABLE_GRADIENT_MAPPING" : "#define ENABLE_GRADIENT_MAPPING"));
        dosat = controlSaturation;
    }

    /** Sets the texture with which gradient mapping will be performed. */
    public void setLut(Texture texture){
        texLut = texture;
        dolut = (texLut != null);

        if(dolut){
            lutStep = 1f / (float)texture.getHeight();
            lutStepOffset = lutStep / 2f; // center texel
        }
    }

    @Override
    protected void update(){
        shader.setUniformf("u_texture1", u_texture1);

        if(dolut){
            shader.setUniformf("LutIndex", lutindex[0]);
            shader.setUniformf("LutIndex2", lutindex[1]);
            shader.setUniformf("LutIndexOffset", lutIndexOffset);
            shader.setUniformf("LutIntensity", lutintensity);
            shader.setUniformf("LutStep", lutStep);
            shader.setUniformf("LutStepOffset", lutStepOffset);
        }

        if(dosat){
            shader.setUniformf("Saturation", saturation);
            shader.setUniformf("SaturationMul", saturationMul);
        }

        shader.setUniformf("VignetteIntensity", intensity);
        shader.setUniformf("VignetteX", x);
        shader.setUniformf("VignetteY", y);
        shader.setUniformf("CenterX", centerX);
        shader.setUniformf("CenterY", centerY);
    }

    @Override
    protected void onBeforeRender(){
        super.onBeforeRender();
        if(dolut){
            texLut.bind(u_texture1);
        }
    }
}
