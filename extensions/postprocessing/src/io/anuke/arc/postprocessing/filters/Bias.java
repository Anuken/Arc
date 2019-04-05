package io.anuke.arc.postprocessing.filters;

import io.anuke.arc.postprocessing.PostFilter;

public final class Bias extends PostFilter{
    public float bias;

    public Bias(){
        super("bias");
    }

    @Override
    protected void update(){
        shader.setUniformf("u_bias", bias);
    }
}
