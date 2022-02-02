package arc.backend.sdl;

import arc.*;
import arc.backend.sdl.jni.*;
import arc.input.*;
import arc.scene.ui.*;
import arc.util.*;

import static arc.backend.sdl.jni.SDL.*;

public class SdlInput extends Input{
    private final InputEventQueue queue = new InputEventQueue();
    private int mouseX, mouseY;
    private int deltaX, deltaY;
    private int mousePressed;
    private byte[] strcpy = new byte[32];

    //handle encoded input data
    void handleInput(int[] input){
        int type = input[0];
        if(type == SDL_EVENT_KEYBOARD){
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

            //special keys
            if(down){
                if(key == KeyCode.backspace) queue.keyTyped((char)8);
                if(key == KeyCode.tab) queue.keyTyped('\t');
                if(key == KeyCode.enter) queue.keyTyped((char)13);
                if(key == KeyCode.forwardDel || key == KeyCode.del) queue.keyTyped((char)127);
            }
        }else if(type == SDL_EVENT_MOUSE_BUTTON){
            boolean down = input[1] == 1;
            int keycode = input[4];
            int x = input[2], y = Core.graphics.getHeight() - input[3];
            KeyCode key =
                keycode == SDL_BUTTON_LEFT ? KeyCode.mouseLeft :
                keycode == SDL_BUTTON_RIGHT ? KeyCode.mouseRight :
                keycode == SDL_BUTTON_MIDDLE ? KeyCode.mouseMiddle :
                keycode == SDL_BUTTON_X1 ? KeyCode.mouseBack :
                keycode == SDL_BUTTON_X2 ? KeyCode.mouseForward : null;
            if(key != null){
                if(down){
                    mousePressed ++;
                    queue.touchDown(x, y, 0, key);
                }else{
                    mousePressed = Math.max(0, mousePressed - 1);
                    queue.touchUp(x, y, 0, key);
                }
            }
        }else if(type == SDL_EVENT_MOUSE_MOTION){
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
        }else if(type == SDL_EVENT_MOUSE_WHEEL){
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
            String s = new String(strcpy, 0, length, Strings.utf8);
            for(int i = 0; i < s.length(); i++){
                queue.keyTyped(s.charAt(i));
            }
        }else if(type == SDL.SDL_EVENT_TEXT_EDIT){
            int estart = input[1];
            int elength = input[2];

            int length = 0;
            for(int i = 0; i < 32; i++){
                char c = (char)input[i + 3];
                if(c == '\0'){
                    length = i;
                    break;
                }
            }
            for(int i = 0; i < length; i++){
                strcpy[i] = (byte)input[i + 3];
            }

            handleFieldCandidate(new String(strcpy, 0, length, Strings.utf8), estart, elength);
        }
    }

    //note: start and length parameters seem useless, ignore those
    void handleFieldCandidate(String text, int start, int length){

        class ImeData{
            String lastSetText;
            String realText;
            int cursor;
        }

        if(Core.scene != null && Core.scene.getKeyboardFocus() instanceof TextField){
            TextField field = (TextField)Core.scene.getKeyboardFocus();

            if(field.imeData instanceof ImeData){
                ImeData data = (ImeData)field.imeData;

                //text modified externally, which means this data is invalid, kill it
                if(data.lastSetText != field.getText()){
                    field.imeData = null;
                }
            }

            //there seem to be stray IME events with zero length, ignore those?
            if(text.length() == 0){
                return;
            }

            //re-initialize when invalidated or just beginning
            if(field.imeData == null){
                field.imeData = new ImeData(){{
                    cursor = field.getCursorPosition();
                    realText = field.getText();
                }};
            }

            ImeData data = (ImeData)field.imeData;
            String targetText = data.realText;
            int insertPos = data.cursor;

            field.setText(targetText.substring(0, Math.min(insertPos, targetText.length())) + text + targetText.substring(Math.min(insertPos, targetText.length())));
            field.setSelection(insertPos, insertPos + text.length());

            data.lastSetText = field.getText();
        }
    }

    //called before main loop
    void update(){
        queue.setProcessor(inputMultiplexer);
        queue.drain();

        for(InputDevice device : devices){
            device.preUpdate();
        }
    }

    //called after main loop
    void postUpdate(){
        for(InputDevice device : devices){
            device.postUpdate();
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
        return keyDown(KeyCode.mouseLeft) || keyDown(KeyCode.mouseRight);
    }

    @Override
    public boolean justTouched(){
        return keyTap(KeyCode.mouseLeft) || keyTap(KeyCode.mouseRight);
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
