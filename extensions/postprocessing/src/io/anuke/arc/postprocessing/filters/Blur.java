package io.anuke.arc.postprocessing.filters;

import io.anuke.arc.collection.IntMap;
import io.anuke.arc.postprocessing.utils.PingPongBuffer;
import io.anuke.arc.util.Disposable;

public final class Blur extends MultipassFilter implements Disposable{
    public BlurType type = BlurType.Gaussian5x5;
    public float amount = 1f;
    public int passes = 1;

    private float invWidth, invHeight;
    private final IntMap<Convolve2D> convolve = new IntMap<>(Tap.values().length);

    public Blur(int width, int height){
        this.invWidth = 1f / (float)width;
        this.invHeight = 1f / (float)height;

        for(Tap tap : Tap.values()){
            convolve.put(tap.radius, new Convolve2D(tap.radius));
        }
    }

    public void resize(int width, int height){
        this.invWidth = 1f / (float)width;
        this.invHeight = 1f / (float)height;
    }

    @Override
    public void dispose(){
        for(Convolve2D c : convolve.values()){
            c.dispose();
        }
    }

    @Override
    public void render(PingPongBuffer buffer){
        Convolve2D c = convolve.get(this.type.tap.radius);

        for(int i = 0; i < this.passes; i++){
            c.render(buffer);
        }
    }

    private void computeBlurWeightings(){
        boolean hasdata = true;
        Convolve2D c = convolve.get(this.type.tap.radius);

        float[] outWeights = c.weights;
        float[] outOffsetsH = c.offsetsHor;
        float[] outOffsetsV = c.offsetsVert;

        float dx = this.invWidth;
        float dy = this.invHeight;

        switch(this.type){
            case Gaussian3x3:
            case Gaussian5x5:
                computeKernel(this.type.tap.radius, this.amount, outWeights);
                computeOffsets(this.type.tap.radius, this.invWidth, this.invHeight, outOffsetsH, outOffsetsV);
                break;

            case Gaussian3x3b:
                // weights and offsets are computed from a binomial distribution
                // and reduced to be used *only* with bilinearly-filtered texture lookups
                //
                // with radius = 1f

                // weights
                outWeights[0] = 0.352941f;
                outWeights[1] = 0.294118f;
                outWeights[2] = 0.352941f;

                // horizontal offsets
                outOffsetsH[0] = -1.33333f;
                outOffsetsH[1] = 0f;
                outOffsetsH[2] = 0f;
                outOffsetsH[3] = 0f;
                outOffsetsH[4] = 1.33333f;
                outOffsetsH[5] = 0f;

                // vertical offsets
                outOffsetsV[0] = 0f;
                outOffsetsV[1] = -1.33333f;
                outOffsetsV[2] = 0f;
                outOffsetsV[3] = 0f;
                outOffsetsV[4] = 0f;
                outOffsetsV[5] = 1.33333f;

                // scale offsets from binomial space to screen space
                for(int i = 0; i < c.length * 2; i++){
                    outOffsetsH[i] *= dx;
                    outOffsetsV[i] *= dy;
                }

                break;

            case Gaussian5x5b:

                // weights and offsets are computed from a binomial distribution
                // and reduced to be used *only* with bilinearly-filtered texture lookups
                //
                // with radius = 2f

                // weights
                outWeights[0] = 0.0702703f;
                outWeights[1] = 0.316216f;
                outWeights[2] = 0.227027f;
                outWeights[3] = 0.316216f;
                outWeights[4] = 0.0702703f;

                // horizontal offsets
                outOffsetsH[0] = -3.23077f;
                outOffsetsH[1] = 0f;
                outOffsetsH[2] = -1.38462f;
                outOffsetsH[3] = 0f;
                outOffsetsH[4] = 0f;
                outOffsetsH[5] = 0f;
                outOffsetsH[6] = 1.38462f;
                outOffsetsH[7] = 0f;
                outOffsetsH[8] = 3.23077f;
                outOffsetsH[9] = 0f;

                // vertical offsets
                outOffsetsV[0] = 0f;
                outOffsetsV[1] = -3.23077f;
                outOffsetsV[2] = 0f;
                outOffsetsV[3] = -1.38462f;
                outOffsetsV[4] = 0f;
                outOffsetsV[5] = 0f;
                outOffsetsV[6] = 0f;
                outOffsetsV[7] = 1.38462f;
                outOffsetsV[8] = 0f;
                outOffsetsV[9] = 3.23077f;

                // scale offsets from binomial space to screen space
                for(int i = 0; i < c.length * 2; i++){
                    outOffsetsH[i] *= dx;
                    outOffsetsV[i] *= dy;
                }

                break;
            default:
                hasdata = false;
                break;
        }

        if(hasdata){
            c.rebind();
        }
    }

    private void computeKernel(int blurRadius, float blurAmount, float[] outKernel){
        int radius = blurRadius;

        // float sigma = (float)radius / amount;
        float sigma = blurAmount;

        float twoSigmaSquare = 2.0f * sigma * sigma;
        float sigmaRoot = (float)Math.sqrt(twoSigmaSquare * Math.PI);
        float total = 0.0f;
        float distance = 0.0f;
        int index = 0;

        for(int i = -radius; i <= radius; ++i){
            distance = i * i;
            index = i + radius;
            outKernel[index] = (float)Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
            total += outKernel[index];
        }

        int size = (radius * 2) + 1;
        for(int i = 0; i < size; ++i){
            outKernel[i] /= total;
        }
    }

    private void computeOffsets(int blurRadius, float dx, float dy, float[] outOffsetH, float[] outOffsetV){
        int radius = blurRadius;

        final int X = 0, Y = 1;
        for(int i = -radius, j = 0; i <= radius; ++i, j += 2){
            outOffsetH[j + X] = i * dx;
            outOffsetH[j + Y] = 0;

            outOffsetV[j + X] = 0;
            outOffsetV[j + Y] = i * dy;
        }
    }

    @Override
    public void rebind(){
        computeBlurWeightings();
    }

    private enum Tap{
        Tap3x3(1), Tap5x5(2);

        public final int radius;

        Tap(int radius){
            this.radius = radius;
        }
    }

    public enum BlurType{
        Gaussian3x3(Tap.Tap3x3), Gaussian3x3b(Tap.Tap3x3), // R=5 (11x11, policy "higher-then-discard")
        Gaussian5x5(Tap.Tap5x5), Gaussian5x5b(Tap.Tap5x5), // R=9 (19x19, policy "higher-then-discard")
        ;

        public final Tap tap;

        BlurType(Tap tap){
            this.tap = tap;
        }
    }
}
