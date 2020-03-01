package arc.backend.sdl;

import arc.*;
import arc.backend.sdl.jni.SDL;
import arc.input.*;

import java.nio.charset.*;

public class SdlInput extends Input{
    private final InputEventQueue queue = new InputEventQueue();
    private int mouseX, mouseY;
    private int deltaX, deltaY;
    private int mousePressed;
    private Charset charset = Charset.forName("UTF-8");
    private byte[] strcpy = new byte[32];

    //handle encoded input data
    void handleInput(int[] input){
        int type = input[0];
        if(type == arc.backend.sdl.jni.SDL.SDL_EVENT_KEYBOARD){
            boolean down = input[1] == 1;
            int keycode = input[4];

            KeyCode key = SdlScanmap.getCode(keycode);
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

            //so is enter
            if(key == KeyCode.FORWARD_DEL && down){
                queue.keyTyped((char)127);
            }
        }else if(type == arc.backend.sdl.jni.SDL.SDL_EVENT_MOUSE_BUTTON){
            boolean down = input[1] == 1;
            int keycode = input[4];
            int x = input[2], y = Core.graphics.getHeight() - input[3];
            KeyCode key = keycode == arc.backend.sdl.jni.SDL.SDL_BUTTON_LEFT ? KeyCode.MOUSE_LEFT : keycode == arc.backend.sdl.jni.SDL.SDL_BUTTON_RIGHT ? KeyCode.MOUSE_RIGHT : keycode == arc.backend.sdl.jni.SDL.SDL_BUTTON_MIDDLE ? KeyCode.MOUSE_MIDDLE : keycode == arc.backend.sdl.jni.SDL.SDL_BUTTON_X1 ? KeyCode.MOUSE_BACK : keycode == arc.backend.sdl.jni.SDL.SDL_BUTTON_X2 ? KeyCode.MOUSE_FORWARD : null;
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
        }else if(type == arc.backend.sdl.jni.SDL.SDL_EVENT_MOUSE_MOTION){
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
        }else if(type == arc.backend.sdl.jni.SDL.SDL_EVENT_MOUSE_WHEEL){
            int sx = input[1];
            int sy = input[2];
            queue.scrolled(-sx, -sy);
        }else if(type == SDL.SDL_EVENT_TEXT_INPUT){
            int length = 0;
            for(int i = 0; i < 32; i++){
                char c = (char)input[i + 1];
                if(c == '\0'){
                    length = i;
                    break;
                }
            }
            for(int i = 0; i < length; i++){
                strcpy[i] = (byte)input[i + 1];
            }
            String s = new String(strcpy, 0, length, charset);
            for(int i = 0; i < s.length(); i++){
                queue.keyTyped(s.charAt(i));
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
