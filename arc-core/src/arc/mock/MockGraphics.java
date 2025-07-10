package arc.mock;

import arc.*;
import arc.Graphics.Cursor.*;
import arc.graphics.*;
import arc.graphics.gl.*;

/**
 * The headless backend does its best to mock elements. This is intended to make code-sharing between
 * server and client as simple as possible.
 */
public class MockGraphics extends Graphics{
    long frameId = -1;
    float deltaTime = 0;
    long frameStart = 0;
    int frames = 0;
    int fps;
    long lastTime = System.nanoTime();
    GLVersion glVersion = new GLVersion(Application.ApplicationType.headless, "", "", "");

    @Override
    public boolean isGL30Available(){
        return false;
    }

    @Override
    public GL20 getGL20(){
        return null;
    }

    @Override
    public void setGL20(GL20 gl20){

    }

    @Override
    public GL30 getGL30(){
        return null;
    }

    @Override
    public void setGL30(GL30 gl30){

    }

    @Override
    public int getWidth(){
        return 0;
    }

    @Override
    public int getHeight(){
        return 0;
    }

    @Override
    public int getBackBufferWidth(){
        return 0;
    }

    @Override
    public int getBackBufferHeight(){
        return 0;
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
        return false;
    }

    @Override
    public boolean setWindowedMode(int width, int height){
        return false;
    }

    @Override
    public void setTitle(String title){

    }

    @Override
    public void setVSync(boolean vsync){

    }

    @Override
    public BufferFormat getBufferFormat(){
        return null;
    }

    @Override
    public boolean supportsExtension(String extension){
        return false;
    }

    @Override
    public boolean isContinuousRendering(){
        return false;
    }

    @Override
    public void setContinuousRendering(boolean isContinuous){

    }

    @Override
    public void requestRendering(){

    }

    @Override
    public boolean isFullscreen(){
        return false;
    }

    public void updateTime(){
        long time = System.nanoTime();
        deltaTime = (time - lastTime) / 1000000000.0f;
        lastTime = time;

        if(time - frameStart >= 1000000000){
            fps = frames;
            frames = 0;
            frameStart = time;
        }
        frames++;
    }

    public void incrementFrameId(){
        frameId++;
    }

    @Override
    public Cursor newCursor(Pixmap pixmap, int xHotspot, int yHotspot){
        return null;
    }

    @Override
    public void setCursor(Cursor cursor){
    }

    @Override
    public void setSystemCursor(SystemCursor systemCursor){
    }

    @Override
    public void setBorderless(boolean undecorated){

    }
}
