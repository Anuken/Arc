package arc.fx.filters;

import arc.*;
import arc.fx.*;

/**
 * Bias filter.
 * @author Toni Sagrista
 */
public final class BiasFilter extends FxFilter{
    public float bias;

    public BiasFilter(){
        super(compileShader(
        Core.files.classpath("shaders/screenspace.vert"),
        Core.files.classpath("bias")));
        rebind();
    }

    @Override
    public void setParams(){
        shader.setUniformi("u_texture0", u_texture0);
        shader.setUniformf("u_bias", bias);
    }
}
