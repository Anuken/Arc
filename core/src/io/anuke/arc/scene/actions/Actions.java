package io.anuke.arc.scene.actions;

import io.anuke.arc.function.Supplier;
import io.anuke.arc.graphics.Color;
import io.anuke.arc.math.Interpolation;
import io.anuke.arc.scene.Action;
import io.anuke.arc.scene.Element;
import io.anuke.arc.scene.event.EventListener;
import io.anuke.arc.scene.event.Touchable;
import io.anuke.arc.utils.pooling.Pools;

/**
 * Static convenience methods for using pooled actions, intended for static import.
 * @author Nathan Sweet
 */
public class Actions{
    /** Returns a new or pooled action of the specified type. */
    static public <T extends Action> T action(Class<T> type, Supplier<T> sup){
        T action = Pools.obtain(type, sup);
        action.setPool(Pools.get(type, sup));
        return action;
    }

    static public AddAction addAction(Action action){
        AddAction addAction = action(AddAction.class, AddAction::new);
        addAction.setAction(action);
        return addAction;
    }

    static public AddAction addAction(Action action, Element targetActor){
        AddAction addAction = action(AddAction.class, AddAction::new);
        addAction.setTarget(targetActor);
        addAction.setAction(action);
        return addAction;
    }

    static public RemoveAction removeAction(Action action){
        RemoveAction removeAction = action(RemoveAction.class, RemoveAction::new);
        removeAction.setAction(action);
        return removeAction;
    }

    static public RemoveAction removeAction(Action action, Element targetActor){
        RemoveAction removeAction = action(RemoveAction.class, RemoveAction::new);
        removeAction.setTarget(targetActor);
        removeAction.setAction(action);
        return removeAction;
    }

    /** Sets the origin to the center. */
    static public Action originCenter(){
        return new OriginAction();
    }

    /** Moves the actor instantly. */
    static public MoveToAction moveTo(float x, float y){
        return moveTo(x, y, 0, null);
    }

    static public MoveToAction moveTo(float x, float y, float duration){
        return moveTo(x, y, duration, null);
    }

    static public MoveToAction moveTo(float x, float y, float duration, Interpolation interpolation){
        MoveToAction action = action(MoveToAction.class, MoveToAction::new);
        action.setPosition(x, y);
        action.setDuration(duration);
        action.setInterpolation(interpolation);
        return action;
    }

    static public MoveToAction moveToAligned(float x, float y, int alignment){
        return moveToAligned(x, y, alignment, 0, null);
    }

    static public MoveToAction moveToAligned(float x, float y, int alignment, float duration){
        return moveToAligned(x, y, alignment, duration, null);
    }

    static public MoveToAction moveToAligned(float x, float y, int alignment, float duration, Interpolation interpolation){
        MoveToAction action = action(MoveToAction.class, MoveToAction::new);
        action.setPosition(x, y, alignment);
        action.setDuration(duration);
        action.setInterpolation(interpolation);
        return action;
    }

    /** Moves the actor instantly. */
    static public MoveByAction moveBy(float amountX, float amountY){
        return moveBy(amountX, amountY, 0, null);
    }

    static public MoveByAction moveBy(float amountX, float amountY, float duration){
        return moveBy(amountX, amountY, duration, null);
    }

    static public TranslateByAction translateBy(float amountX, float amountY, float duration, Interpolation interpolation){
        TranslateByAction action = action(TranslateByAction.class, TranslateByAction::new);
        action.setAmount(amountX, amountY);
        action.setDuration(duration);
        action.setInterpolation(interpolation);
        return action;
    }

    static public TranslateByAction translateBy(float amountX, float amountY){
        return translateBy(amountX, amountY, 0, null);
    }

    static public TranslateByAction translateBy(float amountX, float amountY, float duration){
        return translateBy(amountX, amountY, duration, null);
    }

    static public MoveByAction moveBy(float amountX, float amountY, float duration, Interpolation interpolation){
        MoveByAction action = action(MoveByAction.class, MoveByAction::new);
        action.setAmount(amountX, amountY);
        action.setDuration(duration);
        action.setInterpolation(interpolation);
        return action;
    }

    /** Sizes the actor instantly. */
    static public SizeToAction sizeTo(float x, float y){
        return sizeTo(x, y, 0, null);
    }

    static public SizeToAction sizeTo(float x, float y, float duration){
        return sizeTo(x, y, duration, null);
    }

    static public SizeToAction sizeTo(float x, float y, float duration, Interpolation interpolation){
        SizeToAction action = action(SizeToAction.class, SizeToAction::new);
        action.setSize(x, y);
        action.setDuration(duration);
        action.setInterpolation(interpolation);
        return action;
    }

    /** Sizes the actor instantly. */
    static public SizeByAction sizeBy(float amountX, float amountY){
        return sizeBy(amountX, amountY, 0, null);
    }

    static public SizeByAction sizeBy(float amountX, float amountY, float duration){
        return sizeBy(amountX, amountY, duration, null);
    }

    static public SizeByAction sizeBy(float amountX, float amountY, float duration, Interpolation interpolation){
        SizeByAction action = action(SizeByAction.class, SizeByAction::new);
        action.setAmount(amountX, amountY);
        action.setDuration(duration);
        action.setInterpolation(interpolation);
        return action;
    }

    /** Scales the actor instantly. */
    static public ScaleToAction scaleTo(float x, float y){
        return scaleTo(x, y, 0, null);
    }

    static public ScaleToAction scaleTo(float x, float y, float duration){
        return scaleTo(x, y, duration, null);
    }

    static public ScaleToAction scaleTo(float x, float y, float duration, Interpolation interpolation){
        ScaleToAction action = action(ScaleToAction.class, ScaleToAction::new);
        action.setScale(x, y);
        action.setDuration(duration);
        action.setInterpolation(interpolation);
        return action;
    }

    /** Scales the actor instantly. */
    static public ScaleByAction scaleBy(float amountX, float amountY){
        return scaleBy(amountX, amountY, 0, null);
    }

    static public ScaleByAction scaleBy(float amountX, float amountY, float duration){
        return scaleBy(amountX, amountY, duration, null);
    }

    static public ScaleByAction scaleBy(float amountX, float amountY, float duration, Interpolation interpolation){
        ScaleByAction action = action(ScaleByAction.class, ScaleByAction::new);
        action.setAmount(amountX, amountY);
        action.setDuration(duration);
        action.setInterpolation(interpolation);
        return action;
    }

    /** Rotates the actor instantly. */
    static public RotateToAction rotateTo(float rotation){
        return rotateTo(rotation, 0, null);
    }

    static public RotateToAction rotateTo(float rotation, float duration){
        return rotateTo(rotation, duration, null);
    }

    static public RotateToAction rotateTo(float rotation, float duration, Interpolation interpolation){
        RotateToAction action = action(RotateToAction.class, RotateToAction::new);
        action.setRotation(rotation);
        action.setDuration(duration);
        action.setInterpolation(interpolation);
        return action;
    }

    /** Rotates the actor instantly. */
    static public RotateByAction rotateBy(float rotationAmount){
        return rotateBy(rotationAmount, 0, null);
    }

    static public RotateByAction rotateBy(float rotationAmount, float duration){
        return rotateBy(rotationAmount, duration, null);
    }

    static public RotateByAction rotateBy(float rotationAmount, float duration, Interpolation interpolation){
        RotateByAction action = action(RotateByAction.class, RotateByAction::new);
        action.setAmount(rotationAmount);
        action.setDuration(duration);
        action.setInterpolation(interpolation);
        return action;
    }

    /** Sets the actor's color instantly. */
    static public ColorAction color(Color color){
        return color(color, 0, null);
    }

    /** Transitions from the color at the time this action starts to the specified color. */
    static public ColorAction color(Color color, float duration){
        return color(color, duration, null);
    }

    /** Transitions from the color at the time this action starts to the specified color. */
    static public ColorAction color(Color color, float duration, Interpolation interpolation){
        ColorAction action = action(ColorAction.class, ColorAction::new);
        action.setEndColor(color);
        action.setDuration(duration);
        action.setInterpolation(interpolation);
        return action;
    }

    /** Sets the actor's alpha instantly. */
    static public AlphaAction alpha(float a){
        return alpha(a, 0, null);
    }

    /** Transitions from the alpha at the time this action starts to the specified alpha. */
    static public AlphaAction alpha(float a, float duration){
        return alpha(a, duration, null);
    }

    /** Transitions from the alpha at the time this action starts to the specified alpha. */
    static public AlphaAction alpha(float a, float duration, Interpolation interpolation){
        AlphaAction action = action(AlphaAction.class, AlphaAction::new);
        action.setAlpha(a);
        action.setDuration(duration);
        action.setInterpolation(interpolation);
        return action;
    }

    /** Transitions from the alpha at the time this action starts to an alpha of 0. */
    static public AlphaAction fadeOut(float duration){
        return alpha(0, duration, null);
    }

    /** Transitions from the alpha at the time this action starts to an alpha of 0. */
    static public AlphaAction fadeOut(float duration, Interpolation interpolation){
        AlphaAction action = action(AlphaAction.class, AlphaAction::new);
        action.setAlpha(0);
        action.setDuration(duration);
        action.setInterpolation(interpolation);
        return action;
    }

    /** Transitions from the alpha at the time this action starts to an alpha of 1. */
    static public AlphaAction fadeIn(float duration){
        return alpha(1, duration, null);
    }

    /** Transitions from the alpha at the time this action starts to an alpha of 1. */
    static public AlphaAction fadeIn(float duration, Interpolation interpolation){
        AlphaAction action = action(AlphaAction.class, AlphaAction::new);
        action.setAlpha(1);
        action.setDuration(duration);
        action.setInterpolation(interpolation);
        return action;
    }

    static public VisibleAction show(){
        return visible(true);
    }

    static public VisibleAction hide(){
        return visible(false);
    }

    static public VisibleAction visible(boolean visible){
        VisibleAction action = action(VisibleAction.class, VisibleAction::new);
        action.setVisible(visible);
        return action;
    }

    static public TouchableAction touchable(Touchable touchable){
        TouchableAction action = action(TouchableAction.class, TouchableAction::new);
        action.setTouchable(touchable);
        return action;
    }

    static public RemoveActorAction removeActor(){
        return action(RemoveActorAction.class, RemoveActorAction::new);
    }

    static public RemoveActorAction removeActor(Element removeActor){
        RemoveActorAction action = action(RemoveActorAction.class, RemoveActorAction::new);
        action.setTarget(removeActor);
        return action;
    }

    static public DelayAction delay(float duration){
        DelayAction action = action(DelayAction.class, DelayAction::new);
        action.setDuration(duration);
        return action;
    }

    static public DelayAction delay(float duration, Action delayedAction){
        DelayAction action = action(DelayAction.class, DelayAction::new);
        action.setDuration(duration);
        action.setAction(delayedAction);
        return action;
    }

    static public TimeScaleAction timeScale(float scale, Action scaledAction){
        TimeScaleAction action = action(TimeScaleAction.class, TimeScaleAction::new);
        action.setScale(scale);
        action.setAction(scaledAction);
        return action;
    }

    static public SequenceAction sequence(Action action1){
        SequenceAction action = action(SequenceAction.class, SequenceAction::new);
        action.addAction(action1);
        return action;
    }

    static public SequenceAction sequence(Action action1, Action action2){
        SequenceAction action = action(SequenceAction.class, SequenceAction::new);
        action.addAction(action1);
        action.addAction(action2);
        return action;
    }

    static public SequenceAction sequence(Action action1, Action action2, Action action3){
        SequenceAction action = action(SequenceAction.class, SequenceAction::new);
        action.addAction(action1);
        action.addAction(action2);
        action.addAction(action3);
        return action;
    }

    static public SequenceAction sequence(Action action1, Action action2, Action action3, Action action4){
        SequenceAction action = action(SequenceAction.class, SequenceAction::new);
        action.addAction(action1);
        action.addAction(action2);
        action.addAction(action3);
        action.addAction(action4);
        return action;
    }

    static public SequenceAction sequence(Action action1, Action action2, Action action3, Action action4, Action action5){
        SequenceAction action = action(SequenceAction.class, SequenceAction::new);
        action.addAction(action1);
        action.addAction(action2);
        action.addAction(action3);
        action.addAction(action4);
        action.addAction(action5);
        return action;
    }

    static public SequenceAction sequence(Action... actions){
        SequenceAction action = action(SequenceAction.class, SequenceAction::new);
        for(int i = 0, n = actions.length; i < n; i++)
            action.addAction(actions[i]);
        return action;
    }

    static public SequenceAction sequence(){
        return action(SequenceAction.class, SequenceAction::new);
    }

    static public ParallelAction parallel(Action action1){
        ParallelAction action = action(ParallelAction.class, ParallelAction::new);
        action.addAction(action1);
        return action;
    }

    static public ParallelAction parallel(Action action1, Action action2){
        ParallelAction action = action(ParallelAction.class, ParallelAction::new);
        action.addAction(action1);
        action.addAction(action2);
        return action;
    }

    static public ParallelAction parallel(Action action1, Action action2, Action action3){
        ParallelAction action = action(ParallelAction.class, ParallelAction::new);
        action.addAction(action1);
        action.addAction(action2);
        action.addAction(action3);
        return action;
    }

    static public ParallelAction parallel(Action action1, Action action2, Action action3, Action action4){
        ParallelAction action = action(ParallelAction.class, ParallelAction::new);
        action.addAction(action1);
        action.addAction(action2);
        action.addAction(action3);
        action.addAction(action4);
        return action;
    }

    static public ParallelAction parallel(Action action1, Action action2, Action action3, Action action4, Action action5){
        ParallelAction action = action(ParallelAction.class, ParallelAction::new);
        action.addAction(action1);
        action.addAction(action2);
        action.addAction(action3);
        action.addAction(action4);
        action.addAction(action5);
        return action;
    }

    static public ParallelAction parallel(Action... actions){
        ParallelAction action = action(ParallelAction.class, ParallelAction::new);
        for(int i = 0, n = actions.length; i < n; i++)
            action.addAction(actions[i]);
        return action;
    }

    static public ParallelAction parallel(){
        return action(ParallelAction.class, ParallelAction::new);
    }

    static public RepeatAction repeat(int count, Action repeatedAction){
        RepeatAction action = action(RepeatAction.class, RepeatAction::new);
        action.setCount(count);
        action.setAction(repeatedAction);
        return action;
    }

    static public RepeatAction forever(Action repeatedAction){
        RepeatAction action = action(RepeatAction.class, RepeatAction::new);
        action.setCount(RepeatAction.FOREVER);
        action.setAction(repeatedAction);
        return action;
    }

    static public RunnableAction run(Runnable runnable){
        RunnableAction action = action(RunnableAction.class, RunnableAction::new);
        action.setRunnable(runnable);
        return action;
    }

    static public LayoutAction layout(boolean enabled){
        LayoutAction action = action(LayoutAction.class, LayoutAction::new);
        action.setLayoutEnabled(enabled);
        return action;
    }

    static public AfterAction after(Action action){
        AfterAction afterAction = action(AfterAction.class, AfterAction::new);
        afterAction.setAction(action);
        return afterAction;
    }

    static public AddListenerAction addListener(EventListener listener, boolean capture){
        AddListenerAction addAction = action(AddListenerAction.class, AddListenerAction::new);
        addAction.setListener(listener);
        addAction.setCapture(capture);
        return addAction;
    }

    static public AddListenerAction addListener(EventListener listener, boolean capture, Element targetActor){
        AddListenerAction addAction = action(AddListenerAction.class, AddListenerAction::new);
        addAction.setTarget(targetActor);
        addAction.setListener(listener);
        addAction.setCapture(capture);
        return addAction;
    }

    static public RemoveListenerAction removeListener(EventListener listener, boolean capture){
        RemoveListenerAction addAction = action(RemoveListenerAction.class, RemoveListenerAction::new);
        addAction.setListener(listener);
        addAction.setCapture(capture);
        return addAction;
    }

    static public RemoveListenerAction removeListener(EventListener listener, boolean capture, Element targetActor){
        RemoveListenerAction addAction = action(RemoveListenerAction.class, RemoveListenerAction::new);
        addAction.setTarget(targetActor);
        addAction.setListener(listener);
        addAction.setCapture(capture);
        return addAction;
    }
}
