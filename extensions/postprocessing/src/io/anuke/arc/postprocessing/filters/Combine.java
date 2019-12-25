package io.anuke.arc.postprocessing.filters;

import arc.graphics.Texture;
import io.anuke.arc.postprocessing.PostFilter;

public final class Combine extends PostFilter{
    public float baseIntensity = 1f, baseSaturation = 1f, effectIntensity = 1f, effectSaturation = 1f;
    private Texture inputTexture2 = null;

    public Combine(){
        super("combine");
    }

    public Combine setInput(Texture texture1, Texture texture2){
        this.inputTexture = texture1;
        this.inputTexture2 = texture2;
        return this;
    }

    @Override
    protected void update(){
        shader.setUniformi("u_texture1", u_texture1);
        shader.setUniformf("Src1Intensity", baseIntensity);
        shader.setUniformf("Src1Saturation", baseSaturation);
        shader.setUniformf("Src2Intensity", effectIntensity);
        shader.setUniformf("Src1Saturation", effectSaturation);
    }

    @Override
    protected void onBeforeRender(){
        inputTexture.bind(u_texture0);
        inputTexture2.bind(u_texture1);
    }
}
