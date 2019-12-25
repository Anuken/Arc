package arc.backend.teavm;

import arc.*;
import arc.Graphics.Cursor.*;
import arc.backend.teavm.TeaApplication.*;
import arc.graphics.*;
import arc.graphics.gl.*;
import org.teavm.jso.browser.*;
import org.teavm.jso.core.*;
import org.teavm.jso.dom.html.*;
import org.teavm.jso.webgl.*;

public class TeaGraphics extends Graphics{
    private HTMLCanvasElement element;
    private TeaApplicationConfig config;
    private WebGLRenderingContext context;
    long frameId = -1;
    float deltaTime;
    long lastTimeStamp;
    float time;
    int frames;
    int fps;
    private GL20 gl20;
    private GLVersion glVersion;

    public TeaGraphics(HTMLCanvasElement element, TeaApplicationConfig config){
        this.element = element;
        this.config = config;

        WebGLContextAttributes attr = WebGLContextAttributes.create();
        attr.setAlpha(config.alphaEnabled);
        attr.setAntialias(config.antialiasEnabled);
        attr.setStencil(config.stencilEnabled);
        attr.setPremultipliedAlpha(config.premultipliedAlpha);
        attr.setPreserveDrawingBuffer(config.drawingBufferPreserved);

        context = (WebGLRenderingContext)element.getContext("webgl", attr);
        context.viewport(0, 0, element.getWidth(), element.getHeight());
        gl20 = new TeaGL20(context);

        String versionString = gl20.glGetString(GL20.GL_VERSION);
        String vendorString = gl20.glGetString(GL20.GL_VENDOR);
        String rendererString = gl20.glGetString(GL20.GL_RENDERER);
        glVersion = new GLVersion(Application.ApplicationType.WebGL, versionString, vendorString, rendererString);
    }

    @Override
    public void setGL20(GL20 gl20){
        this.gl20 = gl20;
        Core.gl = gl20;
        Core.gl20 = gl20;
    }

    @Override
    public void setGL30(GL30 gl30){
    }

    @Override
    public int getBackBufferWidth(){
        return getWidth();
    }

    @Override
    public int getBackBufferHeight(){
        return getHeight();
    }

    @Override
    public GLVersion getGLVersion(){
        return glVersion;
    }

    @Override
    public Monitor getPrimaryMonitor(){
        return getMonitor();
    }

    @Override
    public Monitor getMonitor(){
        return new TeaVMMonitor(0, 0, "Primary Monitor");
    }

    @Override
    public Monitor[] getMonitors(){
        return new Monitor[]{getPrimaryMonitor()};
    }

    @Override
    public DisplayMode[] getDisplayModes(Monitor monitor){
        return getDisplayModes();
    }

    @Override
    public DisplayMode getDisplayMode(){
        return getDisplayModes()[0];
    }

    @Override
    public DisplayMode getDisplayMode(Monitor monitor){
        return getDisplayMode();
    }

    @Override
    public boolean setFullscreenMode(DisplayMode displayMode){
        return false;
    }

    @Override
    public boolean setWindowedMode(int width, int height){
        return false;
    }

    @Override
    public void setUndecorated(boolean undecorated){

    }

    @Override
    public void setResizable(boolean resizable){

    }

    @Override
    public Cursor newCursor(Pixmap pixmap, int xHotspot, int yHotspot){
        //TODO not implemented
        return () -> {
        };
    }

    @Override
    public void setCursor(Cursor cursor){
        //TODO not implemented
    }

    @Override
    protected void setSystemCursor(SystemCursor systemCursor){
        //TODO not implemented
    }

    @Override
    public boolean isGL30Available(){
        return false;
    }

    @Override
    public GL20 getGL20(){
        return gl20;
    }

    @Override
    public GL30 getGL30(){
        return null;
    }

    @Override
    public int getWidth(){
        return element.getWidth();
    }

    @Override
    public int getHeight(){
        return element.getHeight();
    }

    @Override
    public long getFrameId(){
        return frameId;
    }

    @Override
    public float getDeltaTime(){
        return deltaTime;
    }

    @Override
    public float getRawDeltaTime(){
        return deltaTime;
    }

    @Override
    public int getFramesPerSecond(){
        return (int)fps;
    }

    @Override
    public float getPpiX(){
        return 96;
    }

    @Override
    public float getPpiY(){
        return 96;
    }

    @Override
    public float getPpcX(){
        return 96 / 2.54f;
    }

    @Override
    public float getPpcY(){
        return 96 / 2.54f;
    }

    @Override
    public float getDensity(){
        return 0;
    }

    @Override
    public boolean supportsDisplayModeChange(){
        return true;
    }

    @Override
    public DisplayMode[] getDisplayModes(){
        Window window = Window.current();
        Screen screen = window.getScreen();
        return new DisplayMode[]{new DisplayMode(screen.getWidth(), screen.getHeight(), 60, 8){
        }};
    }

    @Override
    public void setTitle(String title){
    }

    @Override
    public void setVSync(boolean vsync){
    }

    @Override
    public BufferFormat getBufferFormat(){
        return new BufferFormat(8, 8, 8, 0, 16, config.stencilEnabled ? 8 : 0, 0, false);
    }

    @Override
    public boolean supportsExtension(String extension){
        JSArrayReader<JSString> array = context.getSupportedExtensions();
        for(int i = 0; i < array.getLength(); ++i){
            if(array.get(i).stringValue().equals(extension)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void setContinuousRendering(boolean isContinuous){
    }

    @Override
    public boolean isContinuousRendering(){
        return false;
    }

    @Override
    public void requestRendering(){
    }

    @Override
    public boolean isFullscreen(){
        return false;
    }

    public void update(){
        long currTimeStamp = System.currentTimeMillis();
        deltaTime = (currTimeStamp - lastTimeStamp) / 1000.0f;
        lastTimeStamp = currTimeStamp;
        time += deltaTime;
        frames++;
        if(time > 1){
            this.fps = frames;
            time = 0;
            frames = 0;
        }
    }

    static class TeaVMMonitor extends Monitor{
        protected TeaVMMonitor(int virtualX, int virtualY, String name){
            super(virtualX, virtualY, name);
        }
    }
}
