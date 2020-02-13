package arc.fx.filters;

import arc.*;
import arc.fx.*;
import arc.fx.util.*;
import arc.graphics.*;

public final class CombineFilter extends FxFilter{
    public float src1int = 1f, src1sat = 1f, src2int = 1f, src2sat = 1f;
    public Texture inputTexture2 = null;

    public CombineFilter(){
        super(compileShader(
        Core.files.classpath("shaders/screenspace.vert"),
        Core.files.classpath("shaders/combine.frag")));
        rebind();
    }

    public CombineFilter setInput(FxBuffer buffer1, FxBuffer buffer2){
        this.inputTexture = buffer1.getFbo().getTexture();
        this.inputTexture2 = buffer2.getFbo().getTexture();
        return this;
    }

    public CombineFilter setInput(Texture texture1, Texture texture2){
        this.inputTexture = texture1;
        this.inputTexture2 = texture2;
        return this;
    }

    @Override
    public void setParams(){
        shader.setUniformi("u_texture0", u_texture0);
        shader.setUniformi("u_texture1", u_texture1);
        shader.setUniformf("u_src1Intensity", src1int);
        shader.setUniformf("u_src1Saturation", src2int);
        shader.setUniformf("u_src1Saturation", src1sat);
        shader.setUniformf("u_src2Saturation", src2sat);
    }

    @Override
    protected void onBeforeRender(){
        inputTexture.bind(u_texture0);
        inputTexture2.bind(u_texture1);
    }
}
