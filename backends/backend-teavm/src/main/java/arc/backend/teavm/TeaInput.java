package arc.backend.teavm;

import arc.*;
import arc.Input.*;
import arc.input.*;
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
    private boolean hasFocus = true;

    public TeaInput(HTMLCanvasElement canvas){
        this.canvas = canvas;
        hookEvents();
    }

    void prepareNext(){
        for(InputDevice device : devices){
            device.update();
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

    @Override
    public boolean isPeripheralAvailable(Peripheral peripheral){
        switch(peripheral){
            case Accelerometer:
            case Compass:
            case OnscreenKeyboard:
            case Vibrator:
                return false;
            case HardwareKeyboard:
                return true;
        }
        return false;
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
        document.addEventListener("keydown", this, false);
        document.addEventListener("keyup", this, false);
        document.addEventListener("keypress", this, false);

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
                float mouseX = getRelativeX(mouseEvent, canvas);
                float mouseY = getRelativeY(mouseEvent, canvas);
                if(mouseX < 0 || mouseX > Core.graphics.getWidth() || mouseY < 0 || mouseY > Core.graphics.getHeight()){
                    hasFocus = false;
                }
                return;
            }
            hasFocus = true;
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

        if(e.getType().equals("keydown") && hasFocus){
            KeyboardEvent keyEvent = (KeyboardEvent)e;
            KeyCode code = TeaKeymap.getCode(keyEvent.getKeyCode());
            if(code == KeyCode.BACKSPACE){
                inputMultiplexer.keyDown(code);
                inputMultiplexer.keyTyped('\b');
            }else{
                inputMultiplexer.keyDown(code);
            }
            e.preventDefault();
            e.stopPropagation();
        }

        if(e.getType().equals("keypress") && hasFocus){
            KeyboardEvent keyEvent = (KeyboardEvent)e;
            char c = (char)keyEvent.getCharCode();
            inputMultiplexer.keyTyped(c);
            e.preventDefault();
            e.stopPropagation();
        }

        if(e.getType().equals("keyup") && hasFocus){
            KeyboardEvent keyEvent = (KeyboardEvent)e;
            KeyCode code = TeaKeymap.getCode(keyEvent.getKeyCode());
            inputMultiplexer.keyUp(code);
            e.preventDefault();
            e.stopPropagation();
        }
    }

}
