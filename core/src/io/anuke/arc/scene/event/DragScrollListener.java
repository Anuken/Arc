package io.anuke.arc.scene.event;

import io.anuke.arc.math.Interpolation;
import io.anuke.arc.scene.ui.ScrollPane;
import io.anuke.arc.util.Timer;
import io.anuke.arc.util.Timer.Task;

/**
 * Causes a scroll pane to scroll when a drag goes outside the bounds of the scroll pane.
 * @author Nathan Sweet
 */
public class DragScrollListener extends DragListener{
    Interpolation interpolation = Interpolation.exp5In;
    float minSpeed = 15, maxSpeed = 75, tickSecs = 0.05f;
    long startTime, rampTime = 1750;
    private ScrollPane scroll;
    private Task scrollUp, scrollDown;

    public DragScrollListener(final ScrollPane scroll){
        this.scroll = scroll;

        scrollUp = new Task(){
            public void run(){
                scroll.setScrollY(scroll.getScrollY() - getScrollPixels());
            }
        };
        scrollDown = new Task(){
            public void run(){
                scroll.setScrollY(scroll.getScrollY() + getScrollPixels());
            }
        };
    }

    public void setup(float minSpeedPixels, float maxSpeedPixels, float tickSecs, float rampSecs){
        this.minSpeed = minSpeedPixels;
        this.maxSpeed = maxSpeedPixels;
        this.tickSecs = tickSecs;
        rampTime = (long)(rampSecs * 1000);
    }

    float getScrollPixels(){
        return interpolation.apply(minSpeed, maxSpeed, Math.min(1, (System.currentTimeMillis() - startTime) / (float)rampTime));
    }

    public void drag(InputEvent event, float x, float y, int pointer){
        if(x >= 0 && x < scroll.getWidth()){
            if(y >= scroll.getHeight()){
                scrollDown.cancel();
                if(!scrollUp.isScheduled()){
                    startTime = System.currentTimeMillis();
                    Timer.schedule(scrollUp, tickSecs, tickSecs);
                }
                return;
            }else if(y < 0){
                scrollUp.cancel();
                if(!scrollDown.isScheduled()){
                    startTime = System.currentTimeMillis();
                    Timer.schedule(scrollDown, tickSecs, tickSecs);
                }
                return;
            }
        }
        scrollUp.cancel();
        scrollDown.cancel();
    }

    public void dragStop(InputEvent event, float x, float y, int pointer){
        scrollUp.cancel();
        scrollDown.cancel();
    }
}
