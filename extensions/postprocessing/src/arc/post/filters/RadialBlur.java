package arc.post.filters;

import arc.post.PostFilter;

public final class RadialBlur extends PostFilter{
    public int blurLen;
    public float strength = 0.5f, x = 0.5f, y = 0.5f;
    public float zoom = 1f;

    public RadialBlur(Quality quality){
        super("radial-blur", "radial-blur", "#define BLUR_LENGTH " + quality.length
        + "\n#define ONE_ON_BLUR_LENGTH " + 1f / (float)quality.length);
        this.blurLen = quality.length;
    }

    public RadialBlur(){
        this(Quality.Low);
    }

    @Override
    protected void update(){
        shader.setUniformf("blur_div", strength / (float)blurLen);
        shader.setUniformf("offset_x", x);
        shader.setUniformf("offset_y", y);
        shader.setUniformf("zoom", zoom);
    }

    public enum Quality{
        VeryHigh(16), High(8), Normal(5), Medium(4), Low(2);

        final int length;

        Quality(int value){
            this.length = value;
        }
    }
}
