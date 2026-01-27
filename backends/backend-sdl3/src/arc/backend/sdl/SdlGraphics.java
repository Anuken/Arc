package arc.backend.sdl;

import arc.*;
import arc.Graphics.Cursor.*;
import arc.graphics.*;
import arc.graphics.GL20;
import arc.graphics.GL30;
import arc.graphics.gl.*;
import arc.struct.*;
import arc.util.*;
import org.lwjgl.opengl.*;
import org.lwjgl.sdl.*;
import org.lwjgl.system.*;

import java.nio.*;

public class SdlGraphics extends Graphics{
    private GL20 gl20;
    private GL30 gl30;
    private GLVersion glVersion;
    private BufferFormat bufferFormat;
    private SdlApplication app;
    private ObjectMap<SystemCursor, SdlCursor> cursors;

    private long lastFrameTime = -1;
    private float deltaTime;
    private long frameId;
    private long frameCounterStart = 0;
    private int frames;
    private int fps;

    int backBufferWidth;
    int backBufferHeight;
    int logicalWidth;
    int logicalHeight;

    SdlGraphics(SdlApplication app){
        this.app = app;

        GLCapabilities caps = GL.createCapabilities();

        Core.gl = Core.gl20 = gl20 = new SdlGL20();

        String versionString = gl20.glGetString(GL20.GL_VERSION);
        String vendorString = gl20.glGetString(GL20.GL_VENDOR);
        String rendererString = gl20.glGetString(GL20.GL_RENDERER);

        cursors = new ObjectMap<>();
        glVersion = new GLVersion(Application.ApplicationType.desktop, versionString, vendorString, rendererString);
        bufferFormat = new BufferFormat(app.config.r, app.config.g, app.config.b, app.config.a, app.config.depth, app.config.stencil, app.config.samples, false);

        if(!glVersion.atLeast(2, 0) ||
            !(glVersion.atLeast(3, 0) || caps.GL_EXT_framebuffer_object)){
            throw new ArcRuntimeException("OpenGL 2.0 or higher with the FBO extension is required. OpenGL version: " + versionString);
        }

        //use GL30 version if possible
        if(glVersion.atLeast(3, 0) && app.config.allowGl30){
            Core.gl = Core.gl20 = gl20 = Core.gl30 = gl30 = new SdlGL30();
        }

        clear(app.config.initialBackgroundColor);
        SDLVideo.SDL_GL_SwapWindow(app.window);
    }

    void update(){
        long time = System.nanoTime();
        if(lastFrameTime == -1)
            lastFrameTime = time;
        deltaTime = (time - lastFrameTime) / 1000000000.0f;
        lastFrameTime = time;

        if(time - frameCounterStart >= 1000000000){
            fps = frames;
            frames = 0;
            frameCounterStart = time;
        }
        frames++;
        frameId++;
    }

    void updateSize(int width, int height){
        logicalWidth = width;
        logicalHeight = height;

        if(OS.isMac){
            try(MemoryStack stack = MemoryStack.stackPush()){
                IntBuffer w = stack.mallocInt(1), h = stack.mallocInt(1);
                SDLVideo.SDL_GetWindowSizeInPixels(app.window, w, h);
                backBufferWidth = w.get(0);
                backBufferHeight = h.get(0);
            }
        }else{
            backBufferWidth = width;
            backBufferHeight = height;
        }

        gl20.glViewport(0, 0, backBufferWidth, backBufferHeight);
    }

    @Override
    public boolean isGL30Available(){
        return gl30 != null;
    }

    @Override
    public GL20 getGL20(){
        return gl20;
    }

    @Override
    public void setGL20(GL20 gl20){
        this.gl20 = gl20;
        Core.gl = Core.gl20 = gl20;
    }

    @Override
    public GL30 getGL30(){
        return gl30;
    }

    @Override
    public void setGL30(GL30 gl30){
        this.gl20 = this.gl30 = gl30;
        Core.gl = Core.gl20 = gl30;
    }

    @Override
    public int getWidth(){
        if(app.config.hdpiMode == HdpiMode.pixels){
            return backBufferWidth;
        }else{
            return logicalWidth;
        }
    }

    @Override
    public int getHeight(){
        if(app.config.hdpiMode == HdpiMode.pixels){
            return backBufferHeight;
        }else{
            return logicalHeight;
        }
    }

    @Override
    public int getBackBufferWidth(){
        return backBufferWidth;
    }

    @Override
    public int getBackBufferHeight(){
        return backBufferHeight;
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
    public int getFramesPerSecond(){
        return fps;
    }

    @Override
    public GLVersion getGLVersion(){
        return glVersion;
    }

    @Override
    public float getPpiX(){
        return 0;
    }

    @Override
    public float getPpiY(){
        return 0;
    }

    @Override
    public float getPpcX(){
        return 0;
    }

    @Override
    public float getPpcY(){
        return 0;
    }

    @Override
    public float getDensity(){
        return 0;
    }

    @Override
    public boolean setFullscreen(){
        try(MemoryStack stack = MemoryStack.stackPush()){
            SDL_Rect rect = SDL_Rect.malloc(stack);

            int index = SDLVideo.SDL_GetDisplayForWindow(app.window);
            if(index < 0) return false;

            boolean result = SDLVideo.SDL_GetDisplayBounds(index, rect);
            if(!result) return false;

            SDLVideo.SDL_SetWindowSize(app.window, rect.w(), rect.h());
            SDLVideo.SDL_SetWindowFullscreen(app.window, true);
        }

        return true;
    }

    @Override
    public boolean setWindowedMode(int width, int height){
        SDLVideo.SDL_SetWindowFullscreen(app.window, false);
        SDLVideo.SDL_SetWindowSize(app.window, width, height);
        return true;
    }

    @Override
    public void setTitle(String title){
        SDLVideo.SDL_SetWindowTitle(app.window, title);
    }

    @Override
    public void setBorderless(boolean borderless){
        boolean maximized = (SDLVideo.SDL_GetWindowFlags(app.window) & SDLVideo.SDL_WINDOW_MAXIMIZED) == SDLVideo.SDL_WINDOW_MAXIMIZED;
        if(maximized && OS.isLinux){
            SDLVideo.SDL_RestoreWindow(app.window);
        }

        int index = SDLVideo.SDL_GetDisplayForWindow(app.window);
        if(index < 0) return;

        try(MemoryStack stack = MemoryStack.stackPush()){
            SDL_Rect rect = SDL_Rect.malloc(stack);

            boolean result = borderless ? SDLVideo.SDL_GetDisplayBounds(index, rect) : SDLVideo.SDL_GetDisplayUsableBounds(index, rect);
            if(!result) return;

            SDLVideo.SDL_SetWindowBordered(app.window, !borderless);

            if(maximized && OS.isLinux){
                SDLVideo.SDL_MaximizeWindow(app.window);
            }

            SDLVideo.SDL_SetWindowPosition(app.window, rect.x(), rect.y());
            SDLVideo.SDL_SetWindowSize(app.window, rect.w(), rect.h());
        }
    }

    @Override
    public void setWindowPosition(int x, int y){
        SDLVideo.SDL_SetWindowPosition(app.window, x, y);
    }

    @Override
    public void setWindowSize(int width, int height){
        SDLVideo.SDL_SetWindowSize(app.window, width, height);
    }

    @Override
    public void setVSync(boolean vsync){
        SDLVideo.SDL_GL_SetSwapInterval(vsync ? 1 : 0);
    }

    @Override
    public BufferFormat getBufferFormat(){
        return bufferFormat;
    }

    @Override
    public boolean supportsExtension(String extension){
        return SDLVideo.SDL_GL_ExtensionSupported(extension);
    }

    @Override
    public boolean isContinuousRendering(){
        return true;
    }

    @Override
    public void setContinuousRendering(boolean isContinuous){

    }

    @Override
    public void requestRendering(){

    }

    @Override
    public boolean isFullscreen(){
        return (SDLVideo.SDL_GetWindowFlags(app.window) & SDLVideo.SDL_WINDOW_FULLSCREEN) == SDLVideo.SDL_WINDOW_FULLSCREEN;
    }

    @Override
    public Cursor newCursor(Pixmap pixmap, int xHotspot, int yHotspot){
        SDL_Surface surface = SDLSurface.SDL_CreateSurfaceFrom(pixmap.width, pixmap.height, SDLPixels.SDL_PIXELFORMAT_RGBA32, pixmap.pixels, 4 * pixmap.width);
        long cursor = SDLMouse.SDL_CreateColorCursor(surface, xHotspot, yHotspot);
        return new SdlCursor(surface, cursor);
    }

    @Override
    protected void setCursor(Cursor cursor){
        SDLMouse.SDL_SetCursor(((SdlCursor)cursor).cursorHandle);
    }

    @Override
    protected void setSystemCursor(SystemCursor cursor){
        if(!cursors.containsKey(cursor)){
            long handle = SDLMouse.SDL_CreateSystemCursor(mapCursor(cursor));
            cursors.put(cursor, new SdlCursor(null, handle));
        }
        SDLMouse.SDL_SetCursor(cursors.get(cursor).cursorHandle);
    }

    @Override
    public void dispose(){
        super.dispose();

        cursors.each((ignored, value) -> value.dispose());
    }

    private int mapCursor(SystemCursor cursor){
        switch(cursor){
            case arrow: return SDLMouse.SDL_SYSTEM_CURSOR_DEFAULT;
            case ibeam: return SDLMouse.SDL_SYSTEM_CURSOR_TEXT;
            case crosshair: return SDLMouse.SDL_SYSTEM_CURSOR_CROSSHAIR;
            case hand: return SDLMouse.SDL_SYSTEM_CURSOR_POINTER;
            case horizontalResize: return SDLMouse.SDL_SYSTEM_CURSOR_EW_RESIZE;
            case verticalResize: return SDLMouse.SDL_SYSTEM_CURSOR_NS_RESIZE;
        }
        throw new IllegalArgumentException("this is impossible.");
    }

    public static class SdlCursor implements Cursor{
        SDL_Surface surfaceHandle;
        long cursorHandle;

        public SdlCursor(SDL_Surface surfaceHandle, long cursorHandle){
            this.surfaceHandle = surfaceHandle;
            this.cursorHandle = cursorHandle;
        }

        @Override
        public void dispose(){
            if(cursorHandle != 0) SDLMouse.SDL_DestroyCursor(cursorHandle);
            if(surfaceHandle != null) surfaceHandle.free();
            surfaceHandle = null;
            cursorHandle = 0;
        }
    }
}
