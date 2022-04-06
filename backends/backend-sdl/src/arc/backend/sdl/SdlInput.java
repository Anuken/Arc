package arc.backend.sdl;

import arc.*;
import arc.backend.sdl.jni.*;
import arc.input.*;
import arc.scene.ui.*;
import arc.struct.*;
import arc.util.*;

import static arc.backend.sdl.jni.SDL.*;

public class SdlInput extends Input{
    class EditEvent{
        int start, length;
        String text;
    }
    public static final int NUM_POINTERS = 20;
    private final InputEventQueue queue = new InputEventQueue();
    private int[] pointerX = new int[NUM_POINTERS];
    private int[] pointerY = new int[NUM_POINTERS];
    private int[] deltaX = new int[NUM_POINTERS];
    private int[] deltaY = new int[NUM_POINTERS];
    private int[] touchId = new int[NUM_POINTERS];
    private int[] fingerId = new int[NUM_POINTERS];
    private int mousePressed;
    private byte[] strcpy = new byte[32];
    private Seq<EditEvent> stringEditEvents = new Seq<>();

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
        }else if(type == SDL_EVENT_MOUSE_BUTTON && fingerId[0] == 0){ //ignore mouse events if touch is currently used
            boolean down = input[1] == 1;
            int keycode = input[4];
            int x = input[2], y = input[3];
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
        }else if(type == SDL_EVENT_MOUSE_MOTION && fingerId[0] == 0){
            int x = input[1];
            int y = input[2];

            deltaX[0] = x - pointerX[0];
            deltaY[0] = y - pointerY[0];
            pointerX[0] = x;
            pointerY[0] = y;

            if(mousePressed > 0){
                queue.touchDragged(pointerX[0], pointerY[0], 0);
            }else{
                queue.mouseMoved(pointerX[0], pointerY[0]);
            }
        }else if(type == SDL_EVENT_MOUSE_WHEEL){
            int sx = input[1];
            int sy = input[2];
            queue.scrolled(-sx, -sy);
        }else if(type == SDL_EVENT_TOUCH){
            int x = input[2];
            int y = input[3];
            int dx = input[4];
            int dy = input[5];
            int tid = input[7];
            int fid = input[8];

            boolean motion = input[1] == 0;
            boolean down = input[1] == 1;

            if(motion){
                int pointerIndex = getPointerIndexByIds(fid, tid);
                if(pointerIndex != -1){
                    pointerX[pointerIndex] = x;
                    pointerY[pointerIndex] = y;
                    deltaX[pointerIndex] = dx;
                    deltaY[pointerIndex] = dy;
                    queue.touchDragged(x, y, pointerIndex);
                }
            }else if(down){
                int freeIndex = getFreePointerIndex();
                // skip if no free pointers, ignore double events
                if(freeIndex != -1 && getPointerIndexByIds(fid, tid) == -1){
                    pointerX[freeIndex] = x;
                    pointerY[freeIndex] = y;
                    deltaX[freeIndex] = dx;
                    deltaY[freeIndex] = dy;
                    touchId[freeIndex] = tid;
                    fingerId[freeIndex] = fid;
                    queue.touchDown(x, y, freeIndex, KeyCode.mouseLeft);
                }
            }else{
                int pointerIndex = getPointerIndexByIds(fid, tid);
                if(pointerIndex != -1){
                    pointerX[pointerIndex] = 0;
                    pointerY[pointerIndex] = 0;
                    deltaX[pointerIndex] = 0;
                    deltaY[pointerIndex] = 0;
                    touchId[pointerIndex] = 0;
                    fingerId[pointerIndex] = 0;
                    queue.touchUp(x, y, pointerIndex, KeyCode.mouseLeft);
                }
            }
        }else if(type == SDL_EVENT_TEXT_INPUT){
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

            //defer string edits after string completions
            String str = new String(strcpy, 0, length, Strings.utf8);
            stringEditEvents.add(new EditEvent(){{
                start = input[1];
                this.length = input[2];
                this.text = str;
            }});
        }
    }

    int getPointerIndexByIds(int fid, int tid) {
        for(int i = 0; i < NUM_POINTERS; i++) {
            if(fingerId[i] == fid && touchId[i] == tid) {return i;}
        }
        return -1;
    }

    // gets first pointer id that isn't taken
    int getFreePointerIndex() {
        for(int i = 0; i < NUM_POINTERS; i++) {
            if(fingerId[i] == 0) {return i;}
        }
        return -1;
    }

    //note: start and length parameters seem useless, ignore those
    void handleFieldCandidate(EditEvent e){
        class ImeData{
            String lastSetText;
            String realText;
            int cursor;
        }

        String text = e.text;
        if(Core.scene != null && Core.scene.getKeyboardFocus() instanceof TextField){
            TextField field = (TextField)Core.scene.getKeyboardFocus();

            if(field.imeData instanceof ImeData){
                ImeData data = (ImeData)field.imeData;

                //text modified externally, which means this data is invalid, kill it
                if(data.lastSetText != field.getText()){
                    field.imeData = null;
                }else if(text.length() == 0){
                    //cancel or end composition
                    field.imeData = null;
                    field.setText(data.realText);
                    field.clearSelection();
                    field.setCursorPosition(data.cursor);
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
            //fixme: setCursor will clearSelection, but IME cursor can inside selection.
            //  And TextField seems not support that.
//            field.setCursorPosition(insertPos+e.start);

            data.lastSetText = field.getText();
        }
    }

    //called before main loop
    void update(){
        queue.setProcessor(inputMultiplexer);
        queue.drain();

        for(EditEvent e : stringEditEvents){
            handleFieldCandidate(e);
        }
        stringEditEvents.clear();

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
        return pointerX[0];
    }

    @Override
    public int mouseX(int pointer){
        return pointerX[pointer];
    }

    @Override
    public int deltaX(){
        return deltaX[0];
    }

    @Override
    public int deltaX(int pointer){
        return deltaX[pointer];
    }

    @Override
    public int mouseY(){
        return pointerY[0];
    }

    @Override
    public int mouseY(int pointer){
        return pointerY[pointer];
    }

    @Override
    public int deltaY(){
        return deltaY[0];
    }

    @Override
    public int deltaY(int pointer){
        return deltaY[pointer];
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
        if(pointer == 0){ // mouse
            return mousePressed > 0 || fingerId[0] != 0;
        }else{
            return fingerId[pointer] != 0;
        }
    }

    @Override
    public long getCurrentEventTime(){
        return queue.getCurrentEventTime();
    }
}
