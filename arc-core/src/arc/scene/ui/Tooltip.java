package arc.scene.ui;

import arc.*;
import arc.struct.Array;
import arc.func.Cons;
import arc.input.KeyCode;
import arc.math.Interpolation;
import arc.math.geom.Vec2;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.layout.Table;
import arc.util.*;
import arc.util.Timer.Task;

import static arc.math.Interpolation.fade;
import static arc.scene.actions.Actions.*;

/**
 * A listener that shows a tooltip element when another element is hovered over with the mouse.
 * @author Nathan Sweet
 */
public class Tooltip extends InputListener{
    static Vec2 tmp = new Vec2();

    final Tooltips manager;
    final Table container;
    boolean instant = true, always;
    Element targetActor;
    Runnable show;

    public Tooltip(Cons<Table> contents){
        this(contents, Tooltips.getInstance());
    }

    public Tooltip(Cons<Table> contents, Runnable show){
        this(contents, Tooltips.getInstance());
        this.show = show;
    }

    public Tooltip(Cons<Table> contents, Tooltips manager){
        this.manager = manager;

        container = new Table(){
            public void act(float delta){
                super.act(delta);
                if(targetActor != null && targetActor.getScene() == null) remove();
            }
        };
        contents.get(container);
        container.touchable(Touchable.disabled);
    }

    public Tooltips getManager(){
        return manager;
    }

    public Table getContainer(){
        return container;
    }

    /** If true, this tooltip is shown without delay when hovered. */
    public void setInstant(boolean instant){
        this.instant = instant;
    }

    /** If true, this tooltip is shown even when tooltips are not {@link Tooltips#enabled}. */
    public void setAlways(boolean always){
        this.always = always;
    }

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
        if(instant){
            container.toFront();
            return false;
        }
        manager.touchDown(this);
        return false;
    }

    @Override
    public boolean mouseMoved(InputEvent event, float x, float y){
        if(container.hasParent()) return false;
        setContainerPosition(event.listenerActor, x, y);
        return true;
    }

    protected void setContainerPosition(Element element, float x, float y){
        this.targetActor = element;
        Scene stage = element.getScene();
        if(stage == null) return;

        container.pack();
        float offsetX = manager.offsetX, offsetY = manager.offsetY, dist = manager.edgeDistance;
        Vec2 point = element.localToStageCoordinates(tmp.set(x + offsetX, y - offsetY - container.getHeight()));
        if(point.y < dist) point = element.localToStageCoordinates(tmp.set(x + offsetX, y + offsetY));
        if(point.x < dist) point.x = dist;
        if(point.x + container.getWidth() > stage.getWidth() - dist)
            point.x = stage.getWidth() - dist - container.getWidth();
        if(point.y + container.getHeight() > stage.getHeight() - dist)
            point.y = stage.getHeight() - dist - container.getHeight();
        container.setPosition(point.x, point.y);

        point = element.localToStageCoordinates(tmp.set(element.getWidth() / 2, element.getHeight() / 2));
        point.sub(container.getX(), container.getY());
        container.setOrigin(point.x, point.y);
    }

    public void enter(InputEvent event, float x, float y, int pointer, Element fromActor){
        if(pointer != -1) return;
        if(Core.input.isTouched()) return;
        Element element = event.listenerActor;
        if(fromActor != null && fromActor.isDescendantOf(element)) return;
        setContainerPosition(element, x, y);
        manager.enter(this);

        if(show != null)
            show.run();
    }

    public void exit(InputEvent event, float x, float y, int pointer, Element toActor){
        if(toActor != null && toActor.isDescendantOf(event.listenerActor)) return;
        hide();
    }

    public void hide(){
        manager.hide(this);
    }

    /**
     * Keeps track of an application's tooltips.
     * @author Nathan Sweet
     */
    public static class Tooltips{
        static private Tooltips instance;
        static private StaticReset reset = new StaticReset();
        final Array<Tooltip> shown = new Array<>();
        /**
         * Seconds from when an element is hovered to when the tooltip is shown. Default is 2. Call {@link #hideAll()} after changing to
         * reset internal state.
         */
        public float initialTime = 2;
        /** Once a tooltip is shown, this is used instead of {@link #initialTime}. Default is 0. */
        public float subsequentTime = 0;
        /** Seconds to use {@link #subsequentTime}. Default is 1.5. */
        public float resetTime = 1.5f;
        /** If false, tooltips will not be shown. Default is true. */
        public boolean enabled = true;
        /** If false, tooltips will be shown without animations. Default is true. */
        public boolean animations = false;
        /** The maximum width. The label will wrap if needed. Default is Integer.MAX_VALUE. */
        public float maxWidth = Integer.MAX_VALUE;
        /** The distance from the mouse position to offset the tooltip element. Default is 15,19. */
        public float offsetX = 15, offsetY = 19;
        /**
         * The distance from the tooltip element position to the edge of the screen where the element will be shown on the other side of
         * the mouse cursor. Default is 7.
         */
        public float edgeDistance = 7;
        float time = initialTime;
        final Task resetTask = new Task(){
            public void run(){
                time = initialTime;
            }
        };

        Tooltip showTooltip;
        final Task showTask = new Task(){
            public void run(){
                if(showTooltip == null) return;

                Scene stage = showTooltip.targetActor.getScene();
                if(stage == null) return;
                stage.add(showTooltip.container);
                showTooltip.container.toFront();
                shown.add(showTooltip);

                showTooltip.container.clearActions();
                showAction(showTooltip);

                if(!showTooltip.instant){
                    time = subsequentTime;
                    resetTask.cancel();
                }
            }
        };

        public static Tooltips getInstance(){
            if(reset.check()){
                instance = new Tooltips();
            }
            return instance;
        }

        public void touchDown(Tooltip tooltip){
            showTask.cancel();
            if(tooltip.container.remove()) resetTask.cancel();
            resetTask.run();
            if(enabled || tooltip.always){
                showTooltip = tooltip;
                Timer.schedule(showTask, time);
            }
        }

        public void enter(Tooltip tooltip){
            showTooltip = tooltip;
            showTask.cancel();
            if(enabled || tooltip.always){
                if(time == 0 || tooltip.instant)
                    showTask.run();
                else
                    Timer.schedule(showTask, time);
            }
        }

        public void hide(Tooltip tooltip){
            showTooltip = null;
            showTask.cancel();
            if(tooltip.container.hasParent()){
                shown.remove(tooltip, true);
                hideAction(tooltip);
                resetTask.cancel();
                Timer.schedule(resetTask, resetTime);
            }
        }

        /** Called when tooltip is shown. Default implementation sets actions to animate showing. */
        protected void showAction(Tooltip tooltip){
            float actionTime = animations ? (time > 0 ? 0.5f : 0.15f) : 0.1f;
            tooltip.container.setTransform(true);
            tooltip.container.getColor().a = 0.2f;
            tooltip.container.setScale(0.05f);
            tooltip.container.addAction(parallel(fadeIn(actionTime, fade), scaleTo(1, 1, actionTime, Interpolation.fade)));
        }

        /**
         * Called when tooltip is hidden. Default implementation sets actions to animate hiding and to remove the element from the stage
         * when the actions are complete. A subclass must at least remove the element.
         */
        protected void hideAction(Tooltip tooltip){
            tooltip.container
            .addAction(sequence(parallel(alpha(0.2f, 0.2f, fade), scaleTo(0.05f, 0.05f, 0.2f, Interpolation.fade)), remove()));
        }

        public void hideAll(){
            resetTask.cancel();
            showTask.cancel();
            time = initialTime;
            showTooltip = null;

            for(Tooltip tooltip : shown)
                tooltip.hide();
            shown.clear();
        }

        /** Shows all tooltips on hover without a delay for {@link #resetTime} seconds. */
        public void instant(){
            time = 0;
            showTask.run();
            showTask.cancel();
        }
    }
}