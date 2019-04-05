package io.anuke.arc.postprocessing.filters;

import io.anuke.arc.postprocessing.PostFilter;

public final class Threshold extends PostFilter{
    public float threshold = 0;

    public Threshold(){
        super("threshold");
    }

    @Override
    protected void update(){
        shader.setUniformf("treshold", threshold);
        shader.setUniformf("tresholdInvTx", 1f / (1 - threshold));
    }
}
