package arc.backend.teavm;

import arc.*;
import arc.input.*;
import arc.util.*;
import org.teavm.jso.dom.events.*;
import org.teavm.jso.dom.html.*;

public class TeaInput extends Input implements EventListener{
    static final int MAX_TOUCHES = 20;
    private boolean justTouched = false;
    private boolean[] touched = new boolean[MAX_TOUCHES];
    private int[] touchX = new int[MAX_TOUCHES];
    private int[] touchY = new int[MAX_TOUCHES];
    private int[] deltaX = new int[MAX_TOUCHES];
    private int[] deltaY = new int[MAX_TOUCHES];
    private long currentEventTimeStamp;
    private final HTMLCanvasElement canvas;

    public TeaInput(HTMLCanvasElement canvas){
        this.canvas = canvas;
        hookEvents();
    }

    void preUpdate(){
        //poll for gamepads

        //TODO disabled until further notice
        /*
        Gamepad[] pads = Navigator.getGamepads();
        for(int i = 0; i < 4; i++){
            int index = i;
            TeaController device = (TeaController)devices.find(d -> d instanceof TeaController && ((TeaController)d).index() == index);
            if(device != null && pads[i] == null){ //disconnected
                devices.remove(device);
                inputMultiplexer.disconnected(device);
            }else if(device == null && pads[i] != null){ //connected
                device = new TeaController(pads[i]);
                devices.add(device);
                inputMultiplexer.connected(device);
            }else if(device != null && pads[i] != null){ //update device state
                device.gamepad = pads[i];
            }
        }*/

        for(InputDevice device : devices){
            device.preUpdate();
        }
    }

    void postUpdate(){
        for(InputDevice device : devices){
            device.postUpdate();
        }
    }

    @Override
    public int mouseX(){
        return touchX[0];
    }

    @Override
    public int mouseX(int pointer){
        return touchX[pointer];
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
        return touchY[0];
    }

    @Override
    public int mouseY(int pointer){
        return touchY[pointer];
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
        for(int pointer = 0; pointer < MAX_TOUCHES; pointer++){
            if(touched[pointer]){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean justTouched(){
        return justTouched;
    }

    @Override
    public boolean isTouched(int pointer){
        return touched[pointer];
    }

    @Override
    public void getRotationMatrix(float[] matrix){
    }

    @Override
    public long getCurrentEventTime(){
        return currentEventTimeStamp;
    }

    //TODO this is broken
    protected int getRelativeX(MouseEvent e, HTMLCanvasElement target){
//        System.out.println("left " + target.getOffsetHeight() + " " + target.getAbsoluteLeft() + " " + target.getScrollLeft());
        //float xScaleRatio = target.getWidth() * 1f / target.getClientWidth();
        return e.getClientX();
        //Math.round(xScaleRatio *
        //(e.getClientX() - target.getAbsoluteLeft() + target.getScrollLeft() + target.getOwnerDocument()
        //       .getScrollLeft()));
    }

    protected int getRelativeY(MouseEvent e, HTMLCanvasElement target){
//        System.out.println("right " + target.getAbsoluteTop());
        //float yScaleRatio = target.getHeight() * 1f / target.getClientHeight();
        return target.getHeight() - 1 - e.getClientY();
        //Math.round(yScaleRatio *
        //(e.getClientY() - target.getAbsoluteTop() + target.getScrollTop() + target.getOwnerDocument()
        //   .getScrollTop()));
    }

    private void hookEvents(){
        HTMLDocument document = canvas.getOwnerDocument();
        canvas.addEventListener("mousedown", this, true);
        document.addEventListener("mousedown", this, true);
        canvas.addEventListener("mouseup", this, true);
        document.addEventListener("mouseup", this, true);
        canvas.addEventListener("mousemove", this, true);
        document.addEventListener("mousemove", this, true);
        canvas.addEventListener("wheel", this, true);

        document.addEventListener("keydown", this, true);
        document.addEventListener("keyup", this, true);
        document.addEventListener("keypress", this, true);

        canvas.addEventListener("touchstart", this);
        canvas.addEventListener("touchmove", this);
        canvas.addEventListener("touchcancel", this);
        canvas.addEventListener("touchend", this);
    }

    private float getMovementXJSNI(Event event){
        return ((MouseEvent)event).getScreenX();
    }

    private float getMovementYJSNI(Event event){
        return ((MouseEvent)event).getScreenY();
    }

    @Override
    public void handleEvent(Event e){
        if(e.getType().equals("mousedown")){
            MouseEvent mouseEvent = (MouseEvent)e;
            if(e.getTarget() != canvas || touched[0]){
                return;
            }
            this.justTouched = true;
            this.touched[0] = true;
            this.deltaX[0] = 0;
            this.deltaY[0] = 0;
            if(isCursorCatched()){
                this.touchX[0] += getMovementXJSNI(e);
                this.touchY[0] += getMovementYJSNI(e);
            }else{
                this.touchX[0] = getRelativeX(mouseEvent, canvas);
                this.touchY[0] = getRelativeY(mouseEvent, canvas);
            }
            inputMultiplexer.touchDown(touchX[0], touchY[0], 0, TeaKeymap.getButton(mouseEvent.getButton()));
            e.preventDefault();
            e.stopPropagation();
        }

        if(e.getType().equals("mousemove")){
            MouseEvent mouseEvent = (MouseEvent)e;
            if(isCursorCatched()){
                this.deltaX[0] = (int)getMovementXJSNI(e);
                this.deltaY[0] = (int)getMovementYJSNI(e);
                this.touchX[0] += getMovementXJSNI(e);
                this.touchY[0] += getMovementYJSNI(e);
            }else{
                this.deltaX[0] = getRelativeX(mouseEvent, canvas) - touchX[0];
                this.deltaY[0] = getRelativeY(mouseEvent, canvas) - touchY[0];
                this.touchX[0] = getRelativeX(mouseEvent, canvas);
                this.touchY[0] = getRelativeY(mouseEvent, canvas);
            }
            if(touched[0]){
                inputMultiplexer.touchDragged(touchX[0], touchY[0], 0);
            }else{
                inputMultiplexer.mouseMoved(touchX[0], touchY[0]);
            }
        }

        if(e.getType().equals("mouseup")){
            if(!touched[0]){
                return;
            }
            MouseEvent mouseEvent = (MouseEvent)e;
            if(isCursorCatched()){
                this.deltaX[0] = (int)getMovementXJSNI(e);
                this.deltaY[0] = (int)getMovementYJSNI(e);
                this.touchX[0] += getMovementXJSNI(e);
                this.touchY[0] += getMovementYJSNI(e);
            }else{
                this.deltaX[0] = getRelativeX(mouseEvent, canvas) - touchX[0];
                this.deltaY[0] = getRelativeY(mouseEvent, canvas) - touchY[0];
                this.touchX[0] = getRelativeX(mouseEvent, canvas);
                this.touchY[0] = getRelativeY(mouseEvent, canvas);
            }
            this.touched[0] = false;
            inputMultiplexer.touchUp(touchX[0], touchY[0], 0, TeaKeymap.getButton(mouseEvent.getButton()));
            e.preventDefault();
            e.stopPropagation();
        }

        if(e.getType().equals("wheel")){
            WheelEvent wheel = (WheelEvent)e;
            inputMultiplexer.scrolled((float)wheel.getDeltaX(), (float)wheel.getDeltaY());
            e.preventDefault();
            e.stopPropagation();
        }

        if(e.getType().equals("keydown")){
            KeyboardEvent keyEvent = (KeyboardEvent)e;
            KeyCode code = TeaKeymap.getCode(keyEvent.getKeyCode());
            if(code == KeyCode.backspace){
                inputMultiplexer.keyDown(code);
                inputMultiplexer.keyTyped('\b');
            }else{
                inputMultiplexer.keyDown(code);
            }
            e.preventDefault();
            e.stopPropagation();
        }

        if(e.getType().equals("keypress")){
            KeyboardEvent keyEvent = (KeyboardEvent)e;
            char c = (char)keyEvent.getCharCode();
            inputMultiplexer.keyTyped(c);
            e.preventDefault();
            e.stopPropagation();
        }

        if(e.getType().equals("keyup")){
            KeyboardEvent keyEvent = (KeyboardEvent)e;
            KeyCode code = TeaKeymap.getCode(keyEvent.getKeyCode());
            inputMultiplexer.keyUp(code);
            e.preventDefault();
            e.stopPropagation();
        }

        currentEventTimeStamp = Time.millis();
    }

}
