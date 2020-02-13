package arc.fx.filters;

import arc.*;
import arc.fx.*;

public class FilmGrainFilter extends FxFilter{
    public float seed = 0f;

    public FilmGrainFilter(){
        super(compileShader(
        Core.files.classpath("shaders/screenspace.vert"),
        Core.files.classpath("shaders/film-grain.frag")));
        rebind();
    }

    public void setSeed(float seed){
        this.seed = seed;
        rebind();
    }

    @Override
    public void setParams(){
        shader.setUniformi("u_texture0", u_texture0);
        shader.setUniformf("u_seed", seed);
    }

    @Override
    public void update(float delta){
        this.time = (this.time + delta) % 1f;
        seed = time;
    }
}
