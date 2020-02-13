package arc.fx.filters;

import arc.*;
import arc.fx.*;

/**
 * Fisheye distortion filter
 * @author tsagrista
 */
public class FisheyeDistortionFilter extends FxFilter{

    public FisheyeDistortionFilter(){
        super(compileShader(
        Core.files.classpath("shaders/screenspace.vert"),
        Core.files.classpath("shaders/fisheye.frag")));
        rebind();
    }

    @Override
    public void setParams(){
        shader.setUniformi("u_texture0", u_texture0);
    }
}
