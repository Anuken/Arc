package arc.fx.filters;

import arc.*;
import arc.fx.*;

public class ChromaticAberrationFilter extends FxFilter{
    public float maxDistortion = 1.2f;

    public ChromaticAberrationFilter(int passes){
        super(compileShader(
        Core.files.classpath("shaders/screenspace.vert"),
        Core.files.classpath("shaders/chromatic-aberration.frag"),
        "#define PASSES " + passes));
        rebind();
    }

    @Override
    public void setParams(){
        shader.setUniformi("u_texture0", u_texture0);
        shader.setUniformf("u_maxDistortion", maxDistortion);
    }
}
