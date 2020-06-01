package arc.graphics.g2d;

import arc.*;
import arc.graphics.*;
import arc.graphics.Pixmap.*;
import arc.graphics.VertexAttributes.*;
import arc.graphics.gl.*;

/** @author kalle_h
 * Requires bloom shaders in 'bloomshaders' folder. */
public class Bloom{
    /**
     * To use implement bloom more like a glow. Texture alpha channel can be
     * used as mask which parts are glowing and which are not. see more info at:
     * http://www.gamasutra.com/view/feature/2107/realtime_glow.php
     * <p>
     * NOTE: needs to be set before the bloom instance is created.
     */
    public static boolean useAlphaChannelAsMask = false;

    /** how many blur pass */
    public int blurPasses = 1;

    private Shader thresholdShader;
    private Shader bloomShader;

    private Mesh fullScreenQuad;

    private Texture pingPongTex1;
    private Texture pingPongTex2;
    private Texture original;

    private FrameBuffer frameBuffer;
    private FrameBuffer pingPongBuffer1;
    private FrameBuffer pingPongBuffer2;

    private Shader blurShader;

    private float bloomIntensity;
    private float originalIntensity;
    private float threshold;
    private int w;
    private int h;
    private boolean blending = false;
    private boolean capturing = false;
    private float r, g, b, a;
    private boolean disposeFBO = true;

    /** Rebind the context. Necessary on Android/IOS. */
    public void resume(){
        bloomShader.bind();
        bloomShader.setUniformi("u_texture0", 0);
        bloomShader.setUniformi("u_texture1", 1);

        setSize(w, h);
        setThreshold(threshold);
        setBloomIntesity(bloomIntensity);
        setOriginalIntesity(originalIntensity);
    }

    /**
     * Initialize bloom class that capsulate original scene capturate,
     * thresholding, gaussian blurring and blending. Default values: depth = true
     * blending = false 32bits = true
     */
    public Bloom(){
        initialize(Core.graphics.getWidth() / 4, Core.graphics.getHeight() / 4, null, false, false);
    }

    public Bloom(boolean useBlending){
        initialize(Core.graphics.getWidth() / 4, Core.graphics.getHeight() / 4, null, false, useBlending);
    }

    /**
     * Initialize bloom class that capsulate original scene capturate,
     * thresholding, gaussian blurring and blending.
     *
     * @param FBO_W
     * @param FBO_H how big fbo is used for bloom texture, smaller = more blur and
     * lot faster but aliasing can be problem
     * @param hasDepth do rendering need depth buffer
     * @param useBlending does fbo need alpha channel and is blending enabled when final
     * image is rendered. This allow to combine background graphics
     * and only do blooming on certain objects param use32bitFBO does
     * fbo use higher precision than 16bits.
     */
    public Bloom(int FBO_W, int FBO_H, boolean hasDepth, boolean useBlending){
        initialize(FBO_W, FBO_H, null, hasDepth, useBlending);

    }

    /**
     * EXPERT FUNCTIONALITY. no error checking. Use this only if you know what
     * you are doing. Remember that bloom.capture() clear the screen so use
     * continue instead if that is a problem.
     * <p>
     * Initialize bloom class that capsulate original scene capturate,
     * thresholding, gaussian blurring and blending.
     * <p>
     * * @param sceneIsCapturedHere diposing is user responsibility.
     *
     * @param FBO_W
     * @param FBO_H how big fbo is used for bloom texture, smaller = more blur and
     * lot faster but aliasing can be problem
     * @param useBlending does fbo need alpha channel and is blending enabled when final
     * image is rendered. This allow to combine background graphics
     * and only do blooming on certain objects param use32bitFBO does
     * fbo use higher precision than 16bits.
     */
    public Bloom(int FBO_W, int FBO_H, FrameBuffer sceneIsCapturedHere, boolean useBlending){
        initialize(FBO_W, FBO_H, sceneIsCapturedHere, false, useBlending);
        disposeFBO = false;
    }

    public void resize(int width, int height){
        pingPongBuffer1.resize(width, height);
        pingPongBuffer2.resize(width, height);
        setSize(width, height);
    }

    private void initialize(int FBO_W, int FBO_H, FrameBuffer fbo, boolean hasDepth, boolean useBlending){
        blending = useBlending;
        Format format = useBlending ? Format.RGBA8888 : Format.RGB888;

        if(fbo == null){
            frameBuffer = new FrameBuffer(format, Core.graphics.getWidth(), Core.graphics.getHeight(), hasDepth);
        }else{
            frameBuffer = fbo;
        }

        pingPongBuffer1 = new FrameBuffer(format, FBO_W, FBO_H, false);
        pingPongBuffer2 = new FrameBuffer(format, FBO_W, FBO_H, false);

        fullScreenQuad = createFullScreenQuad();
        final String alpha = useBlending ? "alpha_" : "";

        bloomShader = createShader("screenspace", alpha + "bloom");

        if(useAlphaChannelAsMask){
            thresholdShader = createShader("screenspace", "maskedthreshold");
        }else{
            thresholdShader = createShader("screenspace", alpha + "threshold");
        }

        blurShader = createShader("blurspace", alpha + "gaussian");

        setSize(FBO_W, FBO_H);
        setBloomIntesity(2.5f);
        setOriginalIntesity(1f);
        setThreshold(0.5f);

        bloomShader.bind();
        bloomShader.setUniformi("u_texture0", 0);
        bloomShader.setUniformi("u_texture1", 1);
    }

    /**
     * Set clearing color for capturing buffer
     *
     * @param r
     * @param g
     * @param b
     * @param a
     */
    public void setClearColor(float r, float g, float b, float a){
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    /**
     * Call this before rendering scene.
     */
    public void capture(){
        if(!capturing){
            capturing = true;
            frameBuffer.begin();
            Gl.clearColor(r, g, b, a);
            Gl.clear(Gl.colorBufferBit | Gl.depthBufferBit);
        }
    }

    /**
     * Pause capturing to fbo.
     */
    public void capturePause(){
        if(capturing){
            capturing = false;
            frameBuffer.end();
        }
    }

    /** Start capturing again after pause, no clearing is done to framebuffer */
    public void captureContinue(){
        if(!capturing){
            capturing = true;
            frameBuffer.begin();
        }
    }

    /** Renders the bloomed scene. */
    public void render(){
        if(capturing){
            capturing = false;
            frameBuffer.end();
        }

        Gl.disable(Gl.blend);
        Gl.disable(Gl.depthTest);
        Gl.depthMask(false);

        gaussianBlur();

        if(blending){
            Gl.enable(Gl.blend);
            Gl.blendFunc(Gl.srcAlpha, Gl.oneMinusSrcAlpha);
        }

        pingPongTex1.bind(1);
        original.bind(0);

        bloomShader.bind();
        fullScreenQuad.render(bloomShader, Gl.triangleFan);
    }

    private void gaussianBlur(){
        //cut bright areas of the picture and blit to smaller fbo

        original.bind(0);
        pingPongBuffer1.begin();
        thresholdShader.bind();
        fullScreenQuad.render(thresholdShader, Gl.triangleFan, 0, 4);
        pingPongBuffer1.end();

        for(int i = 0; i < blurPasses; i++){
            pingPongTex1.bind(0);

            // horizontal
            pingPongBuffer2.begin();
            blurShader.bind();
            blurShader.setUniformf("dir", 1f, 0f);
            fullScreenQuad.render(blurShader, Gl.triangleFan, 0, 4);
            pingPongBuffer2.end();

            pingPongTex2.bind(0);
            // vertical
            pingPongBuffer1.begin();
            blurShader.bind();
            blurShader.setUniformf("dir", 0f, 1f);
            fullScreenQuad.render(blurShader, Gl.triangleFan, 0, 4);
            pingPongBuffer1.end();
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
        w = width;
        h = height;
        blurShader.bind();
        blurShader.setUniformf("size", width, height);

        original = frameBuffer.getTexture();
        pingPongTex1 = pingPongBuffer1.getTexture();
        pingPongTex2 = pingPongBuffer2.getTexture();
    }

    /** Disposes all resources. */
    public void dispose(){
        try{
            if(disposeFBO) frameBuffer.dispose();

            fullScreenQuad.dispose();

            pingPongBuffer1.dispose();
            pingPongBuffer2.dispose();

            blurShader.dispose();
            bloomShader.dispose();
            thresholdShader.dispose();
        }catch(Throwable ignored){

        }
    }

    private static Mesh createFullScreenQuad(){
        Mesh tmpMesh = new Mesh(true, 4, 0,
        new VertexAttribute(Usage.position, 2, "a_position"),
        new VertexAttribute(Usage.textureCoordinates, 2, "a_texCoord0")
        );

        tmpMesh.setVertices(new float[]{-1, -1, 0, 0, 1, -1, 1, 0, 1, 1, 1, 1, -1, 1, 0, 1});
        return tmpMesh;
    }

    private static Shader createShader(String vertexName, String fragmentName){
        String vertexShader = Core.files.internal("bloomshaders/" + vertexName + ".vert").readString();
        String fragmentShader = Core.files.internal("bloomshaders/" + fragmentName + ".frag").readString();
        return new Shader(vertexShader, fragmentShader);
    }

}
