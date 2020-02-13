package arc.fx.util;

import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.pooling.*;

import java.nio.*;

/**
 * Wraps {@link FrameBuffer} and manages currently bound OpenGL FBO.
 * <p>
 * This implementation supports nested frame buffer drawing approach.
 * You can use multiple instances of this class to draw into one frame buffer while you drawing into another one,
 * the OpenGL state will be managed properly.
 * <br>
 * Here's an example:
 * <pre>
 * FboWrapper buffer0, buffer1;
 * // ...
 * void render() {
 *      // Any drawing here will be performed directly to the screen.
 *      buffer0.begin();
 *      // Any drawing here will be performed into buffer0's FBO.
 *      buffer1.begin();
 *      // Any drawing here will be performed into buffer1's FBO.
 *      buffer1.end();
 *      // Any drawing here will be performed into buffer0's FBO.
 *      buffer0.end();
 *      // Any drawing here will be performed directly to the screen.
 * }
 * </pre>
 * <p>
 * {@link FxBuffer} internally switches GL viewport between {@link #begin()} and {@link #end()}.
 * <br>
 * If you use any kind of batch renderers,
 * you should update their transform and projection matrices to setup viewport to the target frame buffer's size.
 * You can do so by registering {@link Renderer} using {@link #addRenderer(Renderer)} and {@link #removeRenderer(Renderer)}.
 * The registered renderers will automatically switch their matrices back and forth respectively upon {@link #begin()} and {@link #end()} calls.
 * They will also be flushed in the right time.
 * <p>
 * <b>NOTE:</b> Depth and stencil buffers are not supported.
 * @author metaphore
 */
public class FxBuffer implements Disposable{
    /** Current depth of buffer nesting rendering (keeps track of how many buffers currently activated). */
    public static int bufferNesting = 0;

    private static final IntBuffer tmpIntBuf = ByteBuffer.allocateDirect(16 * Integer.SIZE / 8).order(ByteOrder.nativeOrder()).asIntBuffer();
    private static final Rect tmpViewport = new Rect();
    private static final Mat zeroTransform = new Mat();

    private final Mat localProjection = new Mat();
    private final Mat localTransform = new Mat();

    private final RendererManager renderers = new RendererManager();

    private final Rect preservedViewport = new Rect();
    private final Pixmap.Format pixelFormat;
    private int previousFboHandle;

    private FrameBuffer fbo;
    private boolean initialized;
    private boolean drawing;

    public FxBuffer(Pixmap.Format pixelFormat){
        this.pixelFormat = pixelFormat;
    }

    public FrameBuffer getFbo(){
        return fbo;
    }

    public void initialize(int width, int height){
        if(initialized) dispose();

        initialized = true;

        int boundFboHandle = getBoundFboHandle();
        fbo = new FrameBuffer(pixelFormat, width, height, false);
        fbo.getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        Gl.bindFramebuffer(GL20.GL_FRAMEBUFFER, boundFboHandle);

        localProjection.setOrtho(0, 0, width, height);
        localTransform.set(zeroTransform);
    }

    @Override
    public void dispose(){
        if(!initialized) return;

        initialized = false;

        fbo.dispose();
        fbo = null;
    }

    public Pixmap.Format getPixelFormat(){
        return pixelFormat;
    }

    public boolean isInitialized(){
        return initialized;
    }

    /** @return true means {@link FxBuffer#begin()} was called */
    public boolean isDrawing(){
        return drawing;
    }

    public void addRenderer(Renderer renderer){
        renderers.addRenderer(renderer);
    }

    public void removeRenderer(Renderer renderer){
        renderers.removeRenderer(renderer);
    }

    public void clearRenderers(){
        renderers.clearRenderers();
    }

    public void setProjectionMatrix(Mat matrix){
        localProjection.set(matrix);
    }

    public void setTransformMatrix(Mat matrix){
        localTransform.set(matrix);
    }

    public Mat getProjectionMatrix(){
        return localProjection;
    }

    public Mat getTransformMatrix(){
        return localTransform;
    }

    public void begin(){
        bufferNesting++;

        if(!initialized) throw new IllegalStateException("BatchedFboWrapper must be initialized first");
        if(drawing) throw new IllegalStateException("Already drawing");

        drawing = true;

        renderers.flush();
        previousFboHandle = getBoundFboHandle();
        preservedViewport.set(getViewport());
        Gl.bindFramebuffer(GL20.GL_FRAMEBUFFER, fbo.getFramebufferHandle());
        Gl.viewport(0, 0, getFbo().getWidth(), getFbo().getHeight());
        renderers.assignLocalMatrices(localProjection, localTransform);
    }

    public void end(){
        bufferNesting--;

        if(!initialized) throw new IllegalStateException("BatchedFboWrapper must be initialized first");
        if(!drawing) throw new IllegalStateException("Is not drawing");

        if(getBoundFboHandle() != fbo.getFramebufferHandle()){
            throw new IllegalStateException("Current bound OpenGL FBO's handle doesn't match to wrapped one. It seems like begin/end order was violated.");
        }

        drawing = false;

        renderers.flush();
        Gl.bindFramebuffer(GL20.GL_FRAMEBUFFER, previousFboHandle);
        Gl.viewport((int)preservedViewport.x, (int)preservedViewport.y, (int)preservedViewport.width, (int)preservedViewport.height);
        renderers.restoreOwnMatrices();
    }

    protected int getBoundFboHandle(){
        //TODO remove
        IntBuffer intBuf = tmpIntBuf;
        Gl.getIntegerv(Gl.framebufferBinding, intBuf);
        return intBuf.get(0);
    }

    protected Rect getViewport(){
        IntBuffer intBuf = tmpIntBuf;
        Gl.getIntegerv(Gl.viewport, intBuf);
        return tmpViewport.set(intBuf.get(0), intBuf.get(1), intBuf.get(2), intBuf.get(3));
    }

    private static class RendererManager implements Renderer{
        private final Array<Renderer> renderers = new Array<>();

        RendererManager(){}

        public void addRenderer(Renderer renderer){
            renderers.add(renderer);
        }

        public void removeRenderer(Renderer renderer){
            renderers.remove(renderer);
        }

        public void clearRenderers(){
            renderers.clear();
        }

        @Override
        public void flush(){
            for(int i = 0; i < renderers.size; i++){
                renderers.get(i).flush();
            }
        }

        @Override
        public void assignLocalMatrices(Mat projection, Mat transform){
            for(int i = 0; i < renderers.size; i++){
                renderers.get(i).assignLocalMatrices(projection, transform);
            }
        }

        @Override
        public void restoreOwnMatrices(){
            for(int i = 0; i < renderers.size; i++){
                renderers.get(i).restoreOwnMatrices();
            }
        }
    }

    public interface Renderer{
        void flush();

        void assignLocalMatrices(Mat projection, Mat transform);

        void restoreOwnMatrices();
    }

    public static abstract class RendererAdapter implements Renderer{
        private final Mat preservedProjection = new Mat();
        private final Mat preservedTransform = new Mat();

        @Override
        public void assignLocalMatrices(Mat projection, Mat transform){
            preservedProjection.set(getProjection());
            preservedTransform.set(getTransform());
            setProjection(projection);
        }

        @Override
        public void restoreOwnMatrices(){
            setProjection(preservedProjection);
        }

        protected abstract Mat getProjection();

        protected abstract Mat getTransform();

        protected abstract void setProjection(Mat projection);

        protected abstract void setTransform(Mat transform);
    }

    public static class BatchRendererAdapter extends RendererAdapter implements Pool.Poolable{

        @Override
        public void reset(){

        }

        @Override
        public void flush(){
            Draw.flush();
        }

        @Override
        protected Mat getProjection(){
            return Draw.proj();
        }

        @Override
        protected Mat getTransform(){
            return Draw.trans();
        }

        @Override
        protected void setProjection(Mat projection){
            Draw.proj(projection);
        }

        @Override
        protected void setTransform(Mat transform){
            Draw.trans(transform);
        }
    }
}