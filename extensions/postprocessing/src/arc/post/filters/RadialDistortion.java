package arc.post.filters;

import arc.post.PostFilter;

public final class RadialDistortion extends PostFilter{
    public float zoom = 1f, distortion = 0.3f;

    public RadialDistortion(){
        super("radial-distortion");
    }

    @Override
    protected void update(){
        shader.setUniformf("distortion", distortion);
        shader.setUniformf("zoom", zoom);
    }
}
