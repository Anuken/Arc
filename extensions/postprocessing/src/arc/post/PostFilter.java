package arc.post;

import arc.*;
import arc.graphics.Texture;
import arc.graphics.gl.FrameBuffer;
import arc.graphics.gl.Shader;
import arc.util.Disposable;

/** The base class for any single-pass filter. */
public abstract class PostFilter extends PostEffect implements Disposable{
    public static String shaderDirectory = "shaders/";

    protected static final FullscreenQuad quad = new FullscreenQuad();
    protected static final String fragExtension = ".fragment";
    protected static final String vertExtension = ".vertex";

    protected static final int u_texture0 = 0;
    protected static final int u_texture1 = 1;
    protected static final int u_texture2 = 2;
    protected static final int u_texture3 = 3;

    protected Texture inputTexture = null;
    protected FrameBuffer outputBuffer = null;
    protected Shader shader;

    public PostFilter(String fragmentName){
        this("default", fragmentName);
    }

    public PostFilter(String vertexName, String fragmentName){
        this.shader = new Shader(Core.files.internal(shaderDirectory + vertexName + vertExtension), Core.files.internal(shaderDirectory + fragmentName + fragExtension));
    }

    public PostFilter(String vertexName, String fragmentName, String defines){
        this.shader = new Shader(
            defines + "\n" + Core.files.internal(shaderDirectory + vertexName + vertExtension).readString(),
            defines + "\n" + Core.files.internal(shaderDirectory + fragmentName + fragExtension).readString()
        );
    }

    public PostFilter setInput(Texture input){
        this.inputTexture = input;
        return this;
    }

    public PostFilter setInput(FrameBuffer input){
        return setInput(input.getTexture());
    }

    public PostFilter setOutput(FrameBuffer output){
        this.outputBuffer = output;
        return this;
    }

    @Override
    public final void rebind(){
        shader.begin();
        shader.setUniformf("u_texture0", u_texture0);
        update();
        shader.end();
    }

    @Override
    public void render(final FrameBuffer src, final FrameBuffer dest){
        setInput(src).setOutput(dest).render();
    }

    /** Update filter parameters here. */
    protected abstract void update();

    /** This method will get called just before a rendering operation occurs. */
    protected void onBeforeRender(){
        inputTexture.bind(u_texture0);
    }

    public final void render(){
        if(outputBuffer != null) outputBuffer.begin();

        onBeforeRender();

        shader.begin();
        quad.render(shader);
        shader.end();

        if(outputBuffer != null) outputBuffer.end();
    }
    
    @Override
    public void dispose(){
        shader.dispose();
    }
}
