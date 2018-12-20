package io.anuke.arc.backends.lwjgl3;

import io.anuke.arc.Input;
import io.anuke.arc.graphics.glutils.HdpiMode;
import io.anuke.arc.input.InputDevice;
import io.anuke.arc.input.InputEventQueue;
import io.anuke.arc.input.KeyCode;
import io.anuke.arc.utils.Bits;
import io.anuke.arc.utils.Disposable;
import org.lwjgl.glfw.*;

public class Lwjgl3Input extends Input implements Disposable{
    private final Lwjgl3Window window;
    private final InputEventQueue eventQueue = new InputEventQueue();

    private int mouseX, mouseY;
    private int mousePressed;
    private int deltaX, deltaY;
    private boolean justTouched;
    private boolean keyJustPressed;
    private Bits justPressedKeys = new Bits(KeyCode.values().length);
    private char lastCharacter;
    private GLFWCharCallback charCallback = new GLFWCharCallback(){
        @Override
        public void invoke(long window, int codepoint){
            if((codepoint & 0xff00) == 0xf700) return;
            lastCharacter = (char)codepoint;
            Lwjgl3Input.this.window.getGraphics().requestRendering();
            eventQueue.keyTyped((char)codepoint);
        }
    };
    private GLFWKeyCallback keyCallback = new GLFWKeyCallback(){
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods){
            switch(action){
                case GLFW.GLFW_PRESS:

                    KeyCode gdxKey = Lwjgl3InputMap.getGdxKeyCode(key);
                    eventQueue.keyDown(gdxKey);
                    keyJustPressed = true;
                    justPressedKeys.set(gdxKey.ordinal());
                    Lwjgl3Input.this.window.getGraphics().requestRendering();

                    lastCharacter = 0;
                    char character = Lwjgl3InputMap.characterForKeyCode(gdxKey);
                    if(character != 0) charCallback.invoke(window, character);
                    break;
                case GLFW.GLFW_RELEASE:
                    Lwjgl3Input.this.window.getGraphics().requestRendering();
                    eventQueue.keyUp(Lwjgl3InputMap.getGdxKeyCode(key));
                    break;
                case GLFW.GLFW_REPEAT:
                    if(lastCharacter != 0){
                        Lwjgl3Input.this.window.getGraphics().requestRendering();
                        eventQueue.keyTyped(lastCharacter);
                    }
                    break;
            }
        }
    };
    private GLFWScrollCallback scrollCallback = new GLFWScrollCallback(){
        @Override
        public void invoke(long window, double scrollX, double scrollY){
            Lwjgl3Input.this.window.getGraphics().requestRendering();
            eventQueue.scrolled(-(float)scrollX, -(float)scrollY);
        }
    };

    private GLFWCursorPosCallback cursorPosCallback = new GLFWCursorPosCallback(){
        private int logicalMouseY;
        private int logicalMouseX;

        @Override
        public void invoke(long windowHandle, double x, double y){
            y = window.getGraphics().getLogicalHeight() - y;

            deltaX = (int)x - logicalMouseX;
            deltaY = (int)y - logicalMouseY;
            mouseX = logicalMouseX = (int)x;
            mouseY = logicalMouseY = (int)y;

            if(window.getConfig().hdpiMode == HdpiMode.Pixels){
                float xScale = window.getGraphics().getBackBufferWidth() / (float)window.getGraphics().getLogicalWidth();
                float yScale = window.getGraphics().getBackBufferHeight() / (float)window.getGraphics().getLogicalHeight();
                deltaX = (int)(deltaX * xScale);
                deltaY = (int)(deltaY * yScale);
                mouseX = (int)(mouseX * xScale);
                mouseY = (int)(mouseY * yScale);
            }

            Lwjgl3Input.this.window.getGraphics().requestRendering();
            if(mousePressed > 0){
                eventQueue.touchDragged(mouseX, mouseY, 0);
            }else{
                eventQueue.mouseMoved(mouseX, mouseY);
            }
        }
    };

    private GLFWMouseButtonCallback mouseButtonCallback = new GLFWMouseButtonCallback(){
        @Override
        public void invoke(long window, int button, int action, int mods){
            KeyCode gdxButton = Lwjgl3InputMap.toGdxButton(button);
            if(gdxButton == null) return;

            if(action == GLFW.GLFW_PRESS){
                mousePressed++;
                justTouched = true;
                Lwjgl3Input.this.window.getGraphics().requestRendering();
                eventQueue.touchDown(mouseX, mouseY, 0, gdxButton);
                eventQueue.keyDown(gdxButton);
                keyJustPressed = true;
                justPressedKeys.set(gdxButton.ordinal());
            }else{
                mousePressed = Math.max(0, mousePressed - 1);
                Lwjgl3Input.this.window.getGraphics().requestRendering();
                eventQueue.touchUp(mouseX, mouseY, 0, gdxButton);
                eventQueue.keyUp(gdxButton);
            }
        }
    };

    public Lwjgl3Input(Lwjgl3Window window){
        this.window = window;
        windowHandleChanged();
    }

    void resetPollingStates(){
        justTouched = false;
        keyJustPressed = false;
        justPressedKeys.clear();
        eventQueue.setProcessor(null);
        eventQueue.drain();
    }

    public void windowHandleChanged(){
        resetPollingStates();
        GLFW.glfwSetKeyCallback(window.getWindowHandle(), keyCallback);
        GLFW.glfwSetCharCallback(window.getWindowHandle(), charCallback);
        GLFW.glfwSetScrollCallback(window.getWindowHandle(), scrollCallback);
        GLFW.glfwSetCursorPosCallback(window.getWindowHandle(), cursorPosCallback);
        GLFW.glfwSetMouseButtonCallback(window.getWindowHandle(), mouseButtonCallback);
    }

    void update(){
        eventQueue.setProcessor(inputMultiplexer);
        eventQueue.drain();
    }

    void prepareNext(){
        justTouched = false;

        if(keyJustPressed){
            keyJustPressed = false;
            justPressedKeys.clear();
        }
        deltaX = 0;
        deltaY = 0;

        for(InputDevice device : devices){
            device.update();
        }
    }

    @Override
    public int mouseX(){
        return mouseX;
    }

    @Override
    public int mouseX(int pointer){
        return pointer == 0 ? mouseX : 0;
    }

    @Override
    public int deltaX(){
        return deltaX;
    }

    @Override
    public int deltaX(int pointer){
        return pointer == 0 ? deltaX : 0;
    }

    @Override
    public int mouseY(){
        return mouseY;
    }

    @Override
    public int mouseY(int pointer){
        return pointer == 0 ? mouseY : 0;
    }

    @Override
    public int deltaY(){
        return deltaY;
    }

    @Override
    public int deltaY(int pointer){
        return pointer == 0 ? deltaY : 0;
    }

    @Override
    public boolean isTouched(){
        return GLFW.glfwGetMouseButton(window.getWindowHandle(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS ||
        GLFW.glfwGetMouseButton(window.getWindowHandle(), GLFW.GLFW_MOUSE_BUTTON_2) == GLFW.GLFW_PRESS ||
        GLFW.glfwGetMouseButton(window.getWindowHandle(), GLFW.GLFW_MOUSE_BUTTON_3) == GLFW.GLFW_PRESS ||
        GLFW.glfwGetMouseButton(window.getWindowHandle(), GLFW.GLFW_MOUSE_BUTTON_4) == GLFW.GLFW_PRESS ||
        GLFW.glfwGetMouseButton(window.getWindowHandle(), GLFW.GLFW_MOUSE_BUTTON_5) == GLFW.GLFW_PRESS;
    }

    @Override
    public boolean justTouched(){
        return justTouched;
    }

    @Override
    public boolean isTouched(int pointer){
        return pointer == 0 && isTouched();
    }

    @Override
    public float getPressure(){
        return getPressure(0);
    }

    @Override
    public float getPressure(int pointer){
        return isTouched(pointer) ? 1 : 0;
    }

    /*
    @Override
    public boolean isKeyPressed(KeyCode key){
        if(key == KeyCode.ANY_KEY){
            return pressedKeys > 0;
        }else if(key.type == KeyType.mouse){
            return GLFW.glfwGetMouseButton(window.getWindowHandle(), Lwjgl3InputMap.getGlfwKeyCode(key)) == GLFW.GLFW_PRESS;
        }else if(key == KeyCode.SYM){
            return GLFW.glfwGetKey(window.getWindowHandle(), GLFW.GLFW_KEY_LEFT_SUPER) == GLFW.GLFW_PRESS ||
            GLFW.glfwGetKey(window.getWindowHandle(), GLFW.GLFW_KEY_RIGHT_SUPER) == GLFW.GLFW_PRESS;
        }else{
            return GLFW.glfwGetKey(window.getWindowHandle(), Lwjgl3InputMap.getGlfwKeyCode(key)) == GLFW.GLFW_PRESS;
        }
    }

    @Override
    public boolean isKeyTapped(KeyCode key){
        if(key == KeyCode.ANY_KEY){
            return keyJustPressed;
        }
        return justPressedKeys.get(key.ordinal());
    }*/

    @Override
    public long getCurrentEventTime(){
        // queue sets its event time for each event dequeued/processed
        return eventQueue.getCurrentEventTime();
    }

    @Override
    public boolean isCursorCatched(){
        return GLFW.glfwGetInputMode(window.getWindowHandle(), GLFW.GLFW_CURSOR) == GLFW.GLFW_CURSOR_DISABLED;
    }

    @Override
    public void setCursorCatched(boolean catched){
        GLFW.glfwSetInputMode(window.getWindowHandle(), GLFW.GLFW_CURSOR, catched ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL);
    }

    @Override
    public void setCursorPosition(int x, int y){
        if(window.getConfig().hdpiMode == HdpiMode.Pixels){
            float xScale = window.getGraphics().getLogicalWidth() / (float)window.getGraphics().getBackBufferWidth();
            float yScale = window.getGraphics().getLogicalHeight() / (float)window.getGraphics().getBackBufferHeight();
            x = (int)(x * xScale);
            y = (int)(y * yScale);
        }
        GLFW.glfwSetCursorPos(window.getWindowHandle(), x, y);
    }

    @Override
    public void dispose(){
        keyCallback.free();
        charCallback.free();
        scrollCallback.free();
        cursorPosCallback.free();
        mouseButtonCallback.free();
    }
}
