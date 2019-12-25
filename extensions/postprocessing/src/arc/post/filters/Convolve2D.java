package arc.post.filters;

import arc.post.PingPongBuffer;
import arc.util.Disposable;

/**
 * Encapsulates a separable 2D convolution kernel filter
 * @author bmanuel
 */
public final class Convolve2D extends MultipassFilter implements Disposable{
    public final int radius;
    public final int length; // NxN taps filter, w/ N=length
    public final float[] weights, offsetsHor, offsetsVert;

    private Convolve1D hor, vert;

    public Convolve2D(int radius){
        this.radius = radius;
        length = (radius * 2) + 1;

        hor = new Convolve1D(length);
        vert = new Convolve1D(length, hor.weights);

        weights = hor.weights;
        offsetsHor = hor.offsets;
        offsetsVert = vert.offsets;
    }

    @Override
    public void dispose(){
        hor.dispose();
        vert.dispose();
    }

    @Override
    public void rebind(){
        hor.rebind();
        vert.rebind();
    }

    @Override
    public void render(PingPongBuffer buffer){
        hor.setInput(buffer.capture()).render();
        vert.setInput(buffer.capture()).render();
    }
}
