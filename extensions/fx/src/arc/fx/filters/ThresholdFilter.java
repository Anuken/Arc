package arc.fx.filters;

import arc.*;
import arc.fx.*;

public final class ThresholdFilter extends FxFilter{
    public float gamma = 0;

    public ThresholdFilter(){
        super(compileShader(
        Core.files.classpath("shaders/screenspace.vert"),
        Core.files.classpath("shaders/threshold.frag")));
        rebind();
    }

    @Override
    public void setParams(){
        shader.setUniformf("u_texture0", u_texture0);
        //yet another person can't spell 'threshold' correctly
        shader.setUniformf("treshold", gamma);
        shader.setUniformf("tresholdInvTx", 1f / (1 - gamma));
    }
}
