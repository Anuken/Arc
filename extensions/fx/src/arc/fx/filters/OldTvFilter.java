package arc.fx.filters;

import arc.*;
import arc.fx.*;
import arc.math.geom.*;

public class OldTvFilter extends FxFilter{
    private final Vec2 resolution = new Vec2();

    public OldTvFilter(){
        super(compileShader(
        Core.files.classpath("shaders/screenspace.vert"),
        Core.files.classpath("shaders/old-tv.frag")));
        rebind();
    }

    @Override
    public void resize(int width, int height){
        this.resolution.set(width, height);
        rebind();
    }

    @Override
    public void setParams(){
        shader.setUniformi("u_texture0", u_texture0);
        shader.setUniformf("u_resolution", resolution);
        shader.setUniformf("u_time", time);
    }
}
