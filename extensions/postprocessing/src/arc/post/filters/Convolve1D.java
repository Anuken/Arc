package arc.post.filters;

import arc.post.PostFilter;

public final class Convolve1D extends PostFilter{
    public int length;
    public float[] weights;
    public float[] offsets;

    public Convolve1D(int length){
        this(length, new float[length], new float[length * 2]);
    }

    public Convolve1D(int length, float[] weights_data){
        this(length, weights_data, new float[length * 2]);
    }

    public Convolve1D(int length, float[] weights_data, float[] offsets){
        super("default", "convolve-1d", "#define LENGTH " + length);
        setWeights(length, weights_data, offsets);
    }

    public void setWeights(int length, float[] weights, float[] offsets){
        this.weights = weights;
        this.length = length;
        this.offsets = offsets;
    }

    @Override
    public void dispose(){
        super.dispose();
        weights = null;
        offsets = null;
        length = 0;
    }

    @Override
    protected void update(){
        shader.setUniform1fv("SampleWeights", weights, 0, length);
        shader.setUniform2fv("SampleOffsets", offsets, 0, length * 2);
    }
}
