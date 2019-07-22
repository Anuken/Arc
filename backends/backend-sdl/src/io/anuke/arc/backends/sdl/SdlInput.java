package io.anuke.arc.backends.sdl;

import io.anuke.arc.*;
import io.anuke.arc.input.*;
import sdl.*;

public class SdlInput extends Input{
    private final InputEventQueue queue = new InputEventQueue();
    private int mouseX, mouseY;
    private int deltaX, deltaY;
    private int mousePressed;

    //handle encoded input data
    void handleInput(int[] input){
        int type = input[0];
        if(type == SDL.SDL_EVENT_KEYBOARD){
            boolean down = input[1] == 1;
            int keycode = input[2];

            KeyCode key = SdlKeymap.getCode(keycode);
            //only process non-repeats
            if(input[3] == 0){
                if(down){
                    queue.keyDown(key);
                }else{
                    queue.keyUp(key);
                }
            }

            //backspace is special
            if(key == KeyCode.BACKSPACE && down){
                queue.keyTyped((char)8);
            }

            //so is enter
            if(key == KeyCode.ENTER && down){
                queue.keyTyped((char)13);
            }
        }else if(type == SDL.SDL_EVENT_MOUSE_BUTTON){
            boolean down = input[1] == 1;
            int keycode = input[4];
            int x = input[2], y = Core.graphics.getHeight() - input[3];
            KeyCode key = keycode == SDL.SDL_BUTTON_LEFT ? KeyCode.MOUSE_LEFT : keycode == SDL.SDL_BUTTON_RIGHT ? KeyCode.MOUSE_RIGHT : keycode == SDL.SDL_BUTTON_MIDDLE ? KeyCode.MOUSE_MIDDLE : null;
            if(key != null){
                if(down){
                    mousePressed ++;
                    queue.keyDown(key);
                    queue.touchDown(x, y, 0, key);
                }else{
                    mousePressed = Math.max(0, mousePressed - 1);
                    queue.keyUp(key);
                    queue.touchUp(x, y, 0, key);
                }
            }
        }else if(type == SDL.SDL_EVENT_MOUSE_MOTION){
            int x = input[1];
            int y = Core.graphics.getHeight() - input[2];

            deltaX = x - mouseX;
            deltaY = y - mouseY;
            mouseX = x;
            mouseY = y;

            if(mousePressed > 0){
                queue.touchDragged(mouseX, mouseY, 0);
            }else{
                queue.mouseMoved(mouseX, mouseY);
            }
        }else if(type == SDL.SDL_EVENT_MOUSE_WHEEL){
            int sx = input[1];
            int sy = input[2];
            queue.scrolled(-sx, -sy);
        }else if(type == SDL.SDL_EVENT_TEXT_INPUT){
            for(int i = 0; i < 32; i++){
                char c = (char)input[i + 1];
                queue.keyTyped(c);
                if(c == '\0'){
                    break;
                }
            }
        }
    }

    //called before main loop
    void update(){
        queue.setProcessor(inputMultiplexer);
        queue.drain();
    }

    //called after main loop
    void prepareNext(){
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
        return keyDown(KeyCode.MOUSE_LEFT) || keyDown(KeyCode.MOUSE_RIGHT);
    }

    @Override
    public boolean justTouched(){
        return keyTap(KeyCode.MOUSE_LEFT) || keyTap(KeyCode.MOUSE_RIGHT);
    }

    @Override
    public boolean isTouched(int pointer){
        return false;
    }

    @Override
    public long getCurrentEventTime(){
        return queue.getCurrentEventTime();
    }
}
