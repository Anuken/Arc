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
    }
}
