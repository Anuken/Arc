package arc.graphics.g2d;

import arc.*;
import arc.graphics.*;
import arc.graphics.Pixmap.*;
import arc.graphics.gl.*;

/**
 * @author kalle_h
 * @author Anuke
 * Requires bloom shaders in 'bloomshaders' folder.
 * */
public class Bloom{
    public int blurPasses = 1;

    private Shader thresholdShader, bloomShader, blurShader;
    private FrameBuffer buffer, pingPong1, pingPong2;

    private float bloomIntensity, originalIntensity, threshold;
    private boolean blending = false, capturing = false;
    private float r, g, b, a;

    /** Rebinds the context. Necessary on Android/IOS. TODO or is it? */
    public void resume(){
        bloomShader.bind();
        bloomShader.setUniformi("u_texture0", 0);
        bloomShader.setUniformi("u_texture1", 1);

        setSize(pingPong1.getWidth(), pingPong1.getHeight());
        setThreshold(threshold);
        setBloomIntesity(bloomIntensity);
        setOriginalIntesity(originalIntensity);
    }

    /** Creates a bloom instance with no blending, no depth and 1/4 the screen size. */
    public Bloom(){
        init(Core.graphics.getWidth() / 4, Core.graphics.getHeight() / 4, false, false);
    }

    public Bloom(boolean useBlending){
        init(Core.graphics.getWidth() / 4, Core.graphics.getHeight() / 4, false, useBlending);
    }

    /**
     * Initialize bloom class that capsulate original scene capturate,
     * thresholding, gaussian blurring and blending.
     *
     * @param hasDepth do rendering need depth buffer
     * @param useBlending does fbo need alpha channel and is blending enabled when final image is rendered.
     * This allows combining background graphics and only doing blooming on certain objects.
     */
    public Bloom(int width, int height, boolean hasDepth, boolean useBlending){
        init(width, height, hasDepth, useBlending);
    }

    public void resize(int width, int height){
        boolean changed = (pingPong1.getWidth() != width || pingPong1.getHeight() != height);

        if(changed){
            pingPong1.resize(width, height);
            pingPong2.resize(width, height);
            buffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
            setSize(width, height);
        }
    }

    private void init(int width, int height, boolean hasDepth, boolean useBlending){
        blending = useBlending;
        //rgba8888 is generally well-supported, rgb888 may be slower
        Format format = Format.rgba8888;

        buffer = new FrameBuffer(format, Core.graphics.getWidth(), Core.graphics.getHeight(), hasDepth);
        pingPong1 = new FrameBuffer(format, width, height, false);
        pingPong2 = new FrameBuffer(format, width, height, false);

        final String alpha = useBlending ? "alpha_" : "";

        bloomShader = createShader("screenspace", alpha + "bloom");
        thresholdShader = createShader("screenspace", alpha + "threshold");

        blurShader = createShader("blurspace", alpha + "gaussian");

        setSize(width, height);
        setBloomIntesity(2.5f);
        setOriginalIntesity(1f);
        setThreshold(0.5f);

        bloomShader.bind();
        bloomShader.setUniformi("u_texture0", 0);
        bloomShader.setUniformi("u_texture1", 1);
    }

    /** Set clearing color for capturing buffer */
    public void setClearColor(float r, float g, float b, float a){
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    /** Call this before rendering scene. */
    public void capture(){
        if(!capturing){
            capturing = true;
            buffer.begin();
            Gl.clearColor(r, g, b, a);
            Gl.clear(Gl.colorBufferBit | Gl.depthBufferBit);
        }
    }

    /** Pause capturing to fbo. */
    public void capturePause(){
        if(capturing){
            capturing = false;
            buffer.end();
        }
    }

    /** Start capturing again after pause, no clearing is done to framebuffer */
    public void captureContinue(){
        if(!capturing){
            capturing = true;
            buffer.begin();
        }
    }

    /** Renders the bloomed scene. */
    public void render(){
        if(capturing){
            capturing = false;
            buffer.end();
        }

        Gl.disable(Gl.blend);
        Gl.disable(Gl.depthTest);
        Gl.depthMask(false);

        gaussianBlur();

        if(blending){
            Gl.enable(Gl.blend);
            Gl.blendFunc(Gl.srcAlpha, Gl.oneMinusSrcAlpha);
        }

        pingPong1.getTexture().bind(1);
        buffer.blit(bloomShader);
    }

    private void gaussianBlur(){
        //cut bright areas of the picture and blit to smaller fbo

        pingPong1.begin();
        buffer.blit(thresholdShader);
        pingPong1.end();

        for(int i = 0; i < blurPasses; i++){

            // horizontal
            pingPong2.begin();
            blurShader.bind();
            blurShader.setUniformf("dir", 1f, 0f);
            pingPong1.blit(blurShader);
            pingPong2.end();

            // vertical
            pingPong1.begin();
            blurShader.bind();
            blurShader.setUniformf("dir", 0f, 1f);
            pingPong2.blit(blurShader);
            pingPong1.end();
        }
    }

    /**
     * set intensity for bloom. higher mean more brightening for spots that are
     * over threshold
     *
     * @param intensity multiplier for blurred texture in combining phase. must be
     * positive.
     */
    public void setBloomIntesity(float intensity){
        bloomIntensity = intensity;
        bloomShader.bind();
        bloomShader.setUniformf("BloomIntensity", intensity);
    }

    /**
     * set intensity for original scene. under 1 mean darkening and over 1 means
     * lightening
     *
     * @param intensity multiplier for captured texture in combining phase. must be
     * positive.
     */
    public void setOriginalIntesity(float intensity){
        originalIntensity = intensity;
        bloomShader.bind();
        bloomShader.setUniformf("OriginalIntensity", intensity);
    }

    /**
     * threshold for bright parts. everything under threshold is clamped to 0
     *
     * @param threshold must be in range 0..1
     */
    public void setThreshold(float threshold){
        this.threshold = threshold;
        thresholdShader.bind();
        thresholdShader.setUniformf("threshold", threshold, 1f / (1 - threshold));
    }

    private void setSize(int width, int height){
        blurShader.bind();
        blurShader.setUniformf("size", width, height);
    }

    /** Disposes all resources. */
    public void dispose(){
        try{
            buffer.dispose();
            pingPong1.dispose();
            pingPong2.dispose();

            blurShader.dispose();
            bloomShader.dispose();
            thresholdShader.dispose();
        }catch(Throwable ignored){

        }
    }

    private static Shader createShader(String vertexName, String fragmentName){
        return new Shader(Core.files.internal("bloomshaders/" + vertexName + ".vert"), Core.files.internal("bloomshaders/" + fragmentName + ".frag"));
    }

}
