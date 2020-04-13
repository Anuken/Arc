package arc.graphics.vector;

import arc.graphics.*;
import arc.struct.*;
import arc.util.*;
import arc.util.pooling.Pool.*;

import java.nio.*;

import static arc.graphics.vector.GlUniforms.*;

public class GlContext implements Poolable{
    private final CanvasShader shader = new CanvasShader();
    //private final IntBuffer tmpHandle = Buffers.newIntBuffer(1);
    private final FloatBuffer uniformArray = Buffers.newFloatBuffer(UNIFORMARRAY_SIZE * 4);
    int activeTexture;
    int boundTexture;
    int boundGradientImage;
    int stencilMask;
    int stencilFunction;
    int stencilFunctionRef;
    int stencilFuncMask;
    BlendMode blendMode = BlendMode.over;
    FloatBuffer vertsBuffer;
    private boolean antiAlias;
    private boolean stencilStrokes;
    private boolean debug;
    private int vertexBuffer;
    private FloatBuffer view = Buffers.newFloatBuffer(2);

    private static int getVertFloatsCount(Array<GlCall> calls){
        int vertFloats = 0;
        for(GlCall call : calls){
            for(GlPathComponent path : call.components){
                vertFloats += path.triangleFanVertices.size;
                vertFloats += path.triangleStripVertices.size;
            }
            vertFloats += call.triangleVertices.size;
        }

        return vertFloats * 4;
    }

    void init(int width, int height, boolean antiAlias, boolean stencilStrokes, boolean debug){
        setViewport(width, height);
        this.antiAlias = antiAlias;
        this.stencilStrokes = stencilStrokes;
        this.debug = debug;

        shader.init(antiAlias, debug);

        vertexBuffer = Gl.genBuffer();
        checkError("create done");
        Gl.finish();
    }

    private void setViewport(int width, int height){
        view.put(0, width);
        view.put(1, height);
        view.rewind();
    }

    int getWidth(){
        return (int)view.get(0);
    }

    int getHeight(){
        return (int)view.get(1);
    }

    private void bindTexture(int tex){
        if(boundTexture != tex){
            boundTexture = tex;
            if(activeTexture != 0){
                Gl.activeTexture(GL20.GL_TEXTURE0);
                activeTexture = 0;
            }
            Gl.bindTexture(GL20.GL_TEXTURE_2D, tex);
        }
    }

    private void bindGradient(int gradientImage){
        if(boundGradientImage != gradientImage){
            boundGradientImage = gradientImage;
            if(activeTexture != 1){
                Gl.activeTexture(GL20.GL_TEXTURE1);
                activeTexture = 1;
            }
            Gl.bindTexture(GL20.GL_TEXTURE_2D, gradientImage);
        }
    }

    private void stencilMask(int mask){
        if(stencilMask != mask){
            stencilMask = mask;
            Gl.stencilMask(mask);
        }
    }

    private void stencilFunction(int func, int ref, int mask){
        if((stencilFunction != func) || (stencilFunctionRef != ref) || (stencilFuncMask != mask)){
            stencilFunction = func;
            stencilFunctionRef = ref;
            stencilFuncMask = mask;
            Gl.stencilFunc(func, ref, mask);
        }
    }

    private void setBlendMode(BlendMode blendMode){
        if(this.blendMode == blendMode){
            return;
        }

        if(this.blendMode == BlendMode.src){
            Gl.enable(GL20.GL_BLEND);
        }

        this.blendMode = blendMode;

        if(blendMode == BlendMode.src){
            Gl.disable(GL20.GL_BLEND);
            return;
        }

        switch(blendMode){
            case clear:
                Gl.blendFunc(GL20.GL_ZERO, GL20.GL_ZERO);
                break;
            case src:
                break;
            case dst:
                Gl.blendFunc(GL20.GL_ZERO, GL20.GL_ONE);
                break;
            case over:
                Gl.blendFunc(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
                break;
            case overReverse:
                Gl.blendFunc(GL20.GL_ONE_MINUS_DST_ALPHA, GL20.GL_ONE);
                break;
            case in:
                Gl.blendFunc(GL20.GL_DST_ALPHA, GL20.GL_ZERO);
                break;
            case inReverse:
                Gl.blendFunc(GL20.GL_ZERO, GL20.GL_SRC_ALPHA);
                break;
            case out:
                Gl.blendFunc(GL20.GL_ONE_MINUS_DST_ALPHA, GL20.GL_ZERO);
                break;
            case outReverse:
                Gl.blendFunc(GL20.GL_ZERO, GL20.GL_ONE_MINUS_SRC_ALPHA);
                break;
            case atop:
                Gl.blendFunc(GL20.GL_DST_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                break;
            case atopReverse:
                Gl.blendFunc(GL20.GL_ONE_MINUS_DST_ALPHA, GL20.GL_SRC_ALPHA);
                break;
            case xor:
                Gl.blendFunc(GL20.GL_ONE_MINUS_DST_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                break;
            case add:
                Gl.blendFunc(GL20.GL_ONE, GL20.GL_ONE);
                break;
        }
    }

    private Buffer getVertsBuffer(Array<GlCall> calls, int vertFloats){
        ensureVertsBufferCapacity(vertFloats);

        for(GlCall call : calls){
            for(GlPathComponent path : call.components){
                path.triangleFanVerticesOffset = vertsBuffer.position() / 4;
                for(Vertex vertex : path.triangleFanVertices){
                    fillVertsBuffer(vertex);
                }

                path.triangleStripVerticesOffset = vertsBuffer.position() / 4;
                for(Vertex vertex : path.triangleStripVertices){
                    fillVertsBuffer(vertex);
                }
            }

            call.triangleVerticesOffset = vertsBuffer.position() / 4;
            for(Vertex vertex : call.triangleVertices){
                fillVertsBuffer(vertex);
            }
        }

        return vertsBuffer.rewind();
    }

    private void fillVertsBuffer(Vertex vertex){
        vertsBuffer.put(vertex.x);
        vertsBuffer.put(vertex.y);
        vertsBuffer.put(vertex.u);
        vertsBuffer.put(vertex.v);
    }

    private void ensureVertsBufferCapacity(int vertFloats){
        if(vertsBuffer == null || vertsBuffer.capacity() < vertFloats){
            vertsBuffer = Buffers.newFloatBuffer(vertFloats);
        }

        vertsBuffer.clear();
        vertsBuffer.limit(vertFloats);
    }

    public void render(Array<GlCall> calls){
        if(calls.size == 0){
            return;
        }

        // Setup required GL state.
        Gl.useProgram(shader.programHandle);

        Gl.enable(GL20.GL_BLEND);
        Gl.blendFunc(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);

        Gl.enable(GL20.GL_CULL_FACE);
        Gl.cullFace(GL20.GL_BACK);
        Gl.frontFace(GL20.GL_CCW);

        Gl.disable(GL20.GL_DEPTH_TEST);
        Gl.disable(GL20.GL_SCISSOR_TEST);

        Gl.colorMask(true, true, true, true);
        Gl.stencilMask(0xffffffff);
        Gl.stencilOp(GL20.GL_KEEP, GL20.GL_KEEP, GL20.GL_KEEP);
        Gl.stencilFunc(GL20.GL_ALWAYS, 0, 0xffffffff);

        Gl.activeTexture(GL20.GL_TEXTURE1);
        Gl.bindTexture(GL20.GL_TEXTURE_2D, 0);

        Gl.activeTexture(GL20.GL_TEXTURE0);
        Gl.bindTexture(GL20.GL_TEXTURE_2D, 0);

        Gl.bindBuffer(GL20.GL_ARRAY_BUFFER, vertexBuffer);
        int vertFloats = getVertFloatsCount(calls);
        Buffer filledVertsBuffer = getVertsBuffer(calls, vertFloats);
        Gl.bufferData(GL20.GL_ARRAY_BUFFER, vertFloats * 4, filledVertsBuffer, GL20.GL_STREAM_DRAW);

        Gl.enableVertexAttribArray(0);
        Gl.enableVertexAttribArray(1);
        Gl.vertexAttribPointer(0, 2, GL20.GL_FLOAT, false, 4 * 4, 0);
        Gl.vertexAttribPointer(1, 2, GL20.GL_FLOAT, false, 4 * 4, 2 * 4);

        // Set view and texture just once per frame.
        Gl.uniform1i(shader.imageUniformLocation, 0);
        Gl.uniform1i(shader.gradientImageUniformLocation, 1);
        Gl.uniform2fv(shader.viewSizeUniformLocation, 1, view);

        resetStateData();
        renderCalls(calls);

        Gl.disableVertexAttribArray(0);
        Gl.disableVertexAttribArray(1);
        Gl.disable(GL20.GL_CULL_FACE);
        Gl.bindBuffer(GL20.GL_ARRAY_BUFFER, 0);
        Gl.useProgram(0);

        bindTexture(0);
        bindGradient(0);
    }

    private void resetStateData(){
        // TODO convert to state object
        activeTexture = 0;
        boundTexture = 0;
        boundGradientImage = 0;
        stencilMask = 0xffffffff;
        stencilFunction = GL20.GL_ALWAYS;
        stencilFunctionRef = 0;
        stencilFuncMask = 0xffffffff;
        blendMode = BlendMode.over;
    }

    private void renderCalls(Array<GlCall> calls){
        for(int i = 0; i < calls.size; i++){
            GlCall call = calls.get(i);
            setBlendMode(call.blendMode);

            switch(call.callType){
                case convexFill:
                    convexFill(call);
                    break;
                case fill:
                    /*
                     * if(isClipped(call)) { fillClipped(call); } else { fill(call); }
                     */
                    fill(call);
                    break;
                case stroke:
                    stroke(call);
                    break;
                case triangles:
                    triangles(call);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    public void setUniforms(GlUniforms uniforms){
        Gl.uniform4fv(shader.fragUniformLocation, UNIFORMARRAY_SIZE, uniforms.getUniformArray(uniformArray));

        if(uniforms.imageType != ImageType.none){
            bindTexture(uniforms.imageHandle);
            checkError("tex paint tex");
        }else{
            bindTexture(0);
        }

        if(uniforms.gradientType != GradientType.none){
            bindGradient(uniforms.gradient.getTexturehandle());
            checkError("tex paint tex");
        }else{
            bindGradient(0);
        }
    }

    private void convexFill(GlCall call){
        setUniforms(call.uniform);
        checkError("convex fill");

        Array<GlPathComponent> components = call.components;

        for(int i = 0; i < components.size; i++){
            GlPathComponent component = components.get(i);
            Gl.drawArrays(GL20.GL_TRIANGLE_FAN, component.triangleFanVerticesOffset,
            component.triangleFanVertices.size);
        }

        if(antiAlias){
            // Draw fringes
            for(int i = 0; i < components.size; i++){
                GlPathComponent component = components.get(i);
                Gl.drawArrays(GL20.GL_TRIANGLE_STRIP, component.triangleStripVerticesOffset,
                component.triangleStripVertices.size);
            }
        }
    }

    private void fill(GlCall call){
        // Draw shapes
        Gl.enable(GL20.GL_STENCIL_TEST);
        stencilMask(0xff);
        stencilFunction(GL20.GL_ALWAYS, 0, 0xff);
        Gl.colorMask(false, false, false, false);

        // set bindpoint for solid loc
        setUniforms(GlUniforms.stencilInstance);
        checkError("fill stencil");

        Gl.stencilOpSeparate(GL20.GL_FRONT, GL20.GL_KEEP, GL20.GL_KEEP, GL20.GL_INCR_WRAP);
        Gl.stencilOpSeparate(GL20.GL_BACK, GL20.GL_KEEP, GL20.GL_KEEP, GL20.GL_DECR_WRAP);
        Gl.disable(GL20.GL_CULL_FACE);

        Array<GlPathComponent> components = call.components;

        for(int i = 0; i < components.size; i++){
            GlPathComponent component = components.get(i);
            Gl.drawArrays(GL20.GL_TRIANGLE_FAN, component.triangleFanVerticesOffset,
            component.triangleFanVertices.size);
        }

        Gl.enable(GL20.GL_CULL_FACE);

        // Draw anti-aliased pixels
        Gl.colorMask(true, true, true, true);
        setUniforms(call.uniform);
        checkError("fill fill");

        if(antiAlias){
            stencilFunction(GL20.GL_EQUAL, 0x00, 0xff);
            Gl.stencilOp(GL20.GL_KEEP, GL20.GL_KEEP, GL20.GL_KEEP);

            // Draw fringes
            for(int i = 0; i < components.size; i++){
                GlPathComponent component = components.get(i);
                Gl.drawArrays(GL20.GL_TRIANGLE_STRIP, component.triangleStripVerticesOffset,
                component.triangleStripVertices.size);
            }
        }

        // Draw fill
        stencilFunction(GL20.GL_NOTEQUAL, 0x0, 0xff);
        Gl.stencilOp(GL20.GL_ZERO, GL20.GL_ZERO, GL20.GL_ZERO);
        Gl.drawArrays(GL20.GL_TRIANGLES, call.triangleVerticesOffset, call.triangleVertices.size);

        Gl.disable(GL20.GL_STENCIL_TEST);
    }

    private void stroke(GlCall call){
        int i;
        Array<GlPathComponent> components = call.components;

        if(stencilStrokes){
            Gl.enable(GL20.GL_STENCIL_TEST);
            stencilMask(0xff);

            // Fill the stroke base without overlap
            stencilFunction(GL20.GL_EQUAL, 0x0, 0xff);
            Gl.stencilOp(GL20.GL_KEEP, GL20.GL_KEEP, GL20.GL_INCR);
            setUniforms(call.stencilStrokesUniform);
            checkError("stroke fill 0");

            for(i = 0; i < components.size; i++){
                GlPathComponent component = components.get(i);
                Gl.drawArrays(GL20.GL_TRIANGLE_STRIP, component.triangleStripVerticesOffset,
                component.triangleStripVertices.size);
            }

            // Draw anti-aliased pixels.
            setUniforms(call.uniform);
            stencilFunction(GL20.GL_EQUAL, 0x00, 0xff);
            Gl.stencilOp(GL20.GL_KEEP, GL20.GL_KEEP, GL20.GL_KEEP);

            for(i = 0; i < components.size; i++){
                GlPathComponent component = components.get(i);
                Gl.drawArrays(GL20.GL_TRIANGLE_STRIP, component.triangleStripVerticesOffset,
                component.triangleStripVertices.size);
            }

            // Clear stencil buffer.
            Gl.colorMask(false, false, false, false);
            stencilFunction(GL20.GL_ALWAYS, 0x0, 0xff);
            Gl.stencilOp(GL20.GL_ZERO, GL20.GL_ZERO, GL20.GL_ZERO);
            checkError("stroke fill 1");

            for(i = 0; i < components.size; i++){
                GlPathComponent component = components.get(i);
                Gl.drawArrays(GL20.GL_TRIANGLE_STRIP, component.triangleStripVerticesOffset,
                component.triangleStripVertices.size);
            }
            // TODO Gl.clear(GL20.GL_STENCIL_BUFFER_BIT);

            Gl.colorMask(true, true, true, true);
            Gl.disable(GL20.GL_STENCIL_TEST);

        }else{
            setUniforms(call.uniform);
            checkError("stroke fill");
            // Draw Strokes
            for(i = 0; i < components.size; i++){
                GlPathComponent component = components.get(i);
                Gl.drawArrays(GL20.GL_TRIANGLE_STRIP, component.triangleStripVerticesOffset,
                component.triangleStripVertices.size);
            }
        }
    }

    private void triangles(GlCall call){
        setUniforms(call.uniform);
        checkError("triangles fill");
        Gl.drawArrays(call.vertexMode.glMode, call.triangleVerticesOffset, call.triangleVertices.size);
    }

    private void checkError(String str){
        if(debug){
            int err = Gl.getError();
            if(err != GL20.GL_NO_ERROR){
                Log.err(getClass().getName(), "Error " + err + " after " + str);
            }
        }
    }

    @Override
    public void reset(){
        antiAlias = false;
        stencilStrokes = false;
        debug = false;

        activeTexture = 0;
        boundTexture = 0;
        boundGradientImage = 0;
        stencilMask = 0;
        stencilFunction = 0;
        stencilFunctionRef = 0;
        stencilFuncMask = 0;
        blendMode = BlendMode.over;

        view.clear();
        shader.reset();

        if(vertexBuffer != 0){
            Gl.deleteBuffer(vertexBuffer);

            vertexBuffer = 0;
        }
    }
}
