package arc.fx.filters;

import arc.*;
import arc.fx.*;

public class CopyFilter extends FxFilter{

    public CopyFilter(){
        super(compileShader(
        Core.files.classpath("shaders/screenspace.vert"),
        Core.files.classpath("shaders/copy.frag")));
    }

    @Override
    public void setParams(){
        shader.setUniformf("u_texture0", u_texture0);
    }
}
