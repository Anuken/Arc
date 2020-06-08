package arc.graphics.vector;

import arc.graphics.*;
import arc.graphics.Pixmap.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.pooling.Pool.*;

import static arc.graphics.GL20.GL_FRAMEBUFFER;

class RenderGraph implements Poolable{
    final Seq<CanvasLayer> allLayers = new Seq<>();
    final Seq<LayerRenderNode> layersStack = new Seq<>();
    private final Seq<VFrameBuffer> frameBuffersCache = new Seq<>();
    private final Seq<VFrameBuffer> frameBuffersStack = new Seq<>();
    int width;
    int height;
    GlContext glContext;
    int externalRootFrameBuffer;
    VFrameBuffer rootFrameBuffer;
    private RootRenderNode root = new RootRenderNode();
    private CompositeRenderNode currentLayer = root;
    private SimpleRenderNode lastSimple;

    void init(GlContext glContext, int externalRootFrameBuffer){
        this.glContext = glContext;
        this.width = glContext.getWidth();
        this.height = glContext.getHeight();
        this.externalRootFrameBuffer = externalRootFrameBuffer;
    }

    void init(GlContext glContext, Texture texture){
        this.glContext = glContext;
        this.width = glContext.getWidth();
        this.height = glContext.getHeight();
        this.rootFrameBuffer = VFrameBuffer.obtain(texture);
    }

    public void addCall(GlCall call){
        if(lastSimple == null){
            lastSimple = SimpleRenderNode.obtain(call);
            currentLayer.add(lastSimple);
        }else{
            lastSimple.addCall(call);
        }
    }

    public void addCalls( Seq<GlCall> calls){
        if(lastSimple == null){
            lastSimple = SimpleRenderNode.obtain(calls);
            currentLayer.add(lastSimple);
        }else{
            lastSimple.addCalls(calls);
        }
    }

    void pushLayer(CanvasLayer newLayer){
        allLayers.add(newLayer);
        LayerRenderNode layerRenderNode = LayerRenderNode.obtain(this, newLayer);
        layersStack.add(layerRenderNode);
        currentLayer.add(layerRenderNode);
        currentLayer = layerRenderNode;
        lastSimple = null;
    }

    void popLayer(){
        if(layersStack.size > 0){
            layersStack.pop();
            currentLayer = layersStack.peek();
            lastSimple = null;
        }
    }

    void render(){
        root.render(glContext);
    }

    private VFrameBuffer obtainFrameBuffer(){
        if(frameBuffersCache.size > 0){
            return frameBuffersCache.pop();
        }else{
            return VFrameBuffer.obtain(width, height, Format.RGBA8888);
        }
    }

    @Override
    public void reset(){
        clear();
        externalRootFrameBuffer = 0;
        if(rootFrameBuffer != null){
            rootFrameBuffer.free();
            rootFrameBuffer = null;
        }
        CanvasUtils.resetArray(frameBuffersCache);
        frameBuffersStack.clear();
        width = 0;
        height = 0;
        glContext = null;
    }

    void clear(){
        CanvasUtils.resetArray(allLayers);
        layersStack.clear();
        root.reset();
        currentLayer = root;
        lastSimple = null;
    }

    interface RenderNode{
    }

    public static class SimpleRenderNode implements RenderNode{
        private Seq<GlCall> calls = new Seq<>();

        public SimpleRenderNode(){

        }

        static SimpleRenderNode obtain( Seq<GlCall> calls){
            SimpleRenderNode command = new SimpleRenderNode();
            command.calls.addAll(calls);
            return command;
        }

        static SimpleRenderNode obtain(GlCall call){
            SimpleRenderNode command = new SimpleRenderNode();
            command.calls.add(call);
            return command;
        }

        public void addCalls( Seq<GlCall> calls){
            this.calls.addAll(calls);
        }

        public void addCall(GlCall call){
            this.calls.add(call);
        }
    }

    private static abstract class CompositeRenderNode implements RenderNode{
        Seq<RenderNode> nodes = new Seq<>();
        Seq<GlCall> calls = new Seq<>();

        void render(GlContext glContext){
            for(RenderNode node : nodes){
                if(node instanceof LayerRenderNode){
                    ((LayerRenderNode)node).render(glContext);
                }
            }

            startLayer();
            glContext.render(getCalls());
            calls.clear();
            endLayer();

            for(RenderNode node : nodes){
                if(node instanceof LayerRenderNode){
                    ((LayerRenderNode)node).resetBuffer();
                }
            }
        }

        private Seq<GlCall> getCalls(){
            for(RenderNode node : nodes){
                if(node instanceof SimpleRenderNode){
                    calls.addAll(((SimpleRenderNode)node).calls);
                }else if(node instanceof LayerRenderNode){
                    calls.add(((LayerRenderNode)node).getCall());
                }
            }
            return calls;
        }

        abstract void startLayer();

        abstract void endLayer();

        void add(RenderNode renderNode){
            nodes.add(renderNode);
        }

        public void reset(){
            calls.clear();
            CanvasUtils.resetArray(nodes);
        }
    }

    private static class LayerRenderNode extends CompositeRenderNode{
        final Vertex vertex1;
        final Vertex vertex2;
        final Vertex vertex3;
        final Vertex vertex4;
        final Vertex vertex5;
        final Vertex vertex6;

        RenderGraph graph;
        CanvasLayer layer;

        VFrameBuffer frameBuffer;

        GlCall call;

        private LayerRenderNode(){
            call = GlCall.obtainFrameBufferRenderCall();
            call.triangleVertices.add(vertex1 = Vertex.obtain());
            call.triangleVertices.add(vertex2 = Vertex.obtain());
            call.triangleVertices.add(vertex3 = Vertex.obtain());
            call.triangleVertices.add(vertex4 = Vertex.obtain());
            call.triangleVertices.add(vertex5 = Vertex.obtain());
            call.triangleVertices.add(vertex6 = Vertex.obtain());
        }

        static LayerRenderNode obtain(RenderGraph graph, CanvasLayer layer){
            LayerRenderNode command = new LayerRenderNode();
            command.graph = graph;
            command.layer = layer;
            return command;
        }

        @Override
        void startLayer(){
            frameBuffer = graph.obtainFrameBuffer();
            frameBuffer.bind();
            Gl.clearColor(0, 0, 0, 0);
            Gl.clear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_STENCIL_BUFFER_BIT);
        }

        @Override
        void endLayer(){
            frameBuffer.unbind();
            Effect effect = layer.effect;
            if(effect != null){
                effect.process(frameBuffer.texture);
            }
        }

        GlCall getCall(){
            initVertices();
            call.uniform.imageHandle = frameBuffer.texture.getTextureObjectHandle();
            return call;
        }

        /*
         [-1:1,  1:1]     [0:1, 1:1]
         [-1:-1, 1:-1]    [0:0, 1:0]
         */
        private void initVertices(){
            Rect bounds = layer.bounds;
            float left = bounds.x;
            float top = bounds.y;
            float right = bounds.x + bounds.width;
            float bottom = bounds.y + bounds.height;

            int canvasWidth = graph.width;
            int canvasHeight = graph.height;
            float canvasWidthRatio = 1f / canvasWidth;
            float canvasHeightRatio = 1f / canvasHeight;
            float textureLeft = canvasWidthRatio * left;
            float textureTop = canvasHeightRatio * (canvasHeight - top);
            float textureRight = canvasWidthRatio * (bounds.width + left);
            float textureBottom = canvasHeightRatio * (canvasHeight - bottom);

            vertex1.set(left, top, textureLeft, textureTop);
            vertex2.set(left, bottom, textureLeft, textureBottom);
            vertex3.set(right, top, textureRight, textureTop);

            vertex4.set(right, top, textureRight, textureTop);
            vertex5.set(left, bottom, textureLeft, textureBottom);
            vertex6.set(right, bottom, textureRight, textureBottom);
        }

        void resetBuffer(){
            graph.frameBuffersCache.add(frameBuffer);
            frameBuffer = null;
        }

        public void reset(){
            super.reset();
            layer = null;
            graph = null;
        }
    }

    private class RootRenderNode extends CompositeRenderNode{
        @Override
        void startLayer(){
            if(rootFrameBuffer != null){
                rootFrameBuffer.bind();
                Gl.clearColor(0, 0, 0, 0);
                Gl.clear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_STENCIL_BUFFER_BIT);
            }
        }

        @Override
        void endLayer(){
            if(rootFrameBuffer != null){
                rootFrameBuffer.unbind();
            }else if(externalRootFrameBuffer != 0){
                Gl.bindFramebuffer(GL_FRAMEBUFFER, externalRootFrameBuffer);
            }
        }
    }
}
