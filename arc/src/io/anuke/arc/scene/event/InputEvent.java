/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package io.anuke.arc.scene.event;

import io.anuke.arc.input.KeyCode;
import io.anuke.arc.math.geom.Vector2;
import io.anuke.arc.scene.Element;
import io.anuke.arc.scene.Scene;

/**
 * Event for actor input: touch, mouse, keyboard, and scroll.
 * @see InputListener
 */
public class InputEvent extends Event{
    public Type type;
    public float stageX, stageY;
    public int pointer;
    public float scrollAmountX, scrollAmountY;
    public KeyCode keyCode;
    public char character;
    public Element relatedActor;

    public void reset(){
        super.reset();
        relatedActor = null;
    }

    /**
     * Sets actorCoords to this event's coordinates relative to the specified actor.
     * @param actorCoords Output for resulting coordinates.
     */
    public Vector2 toCoordinates(Element actor, Vector2 actorCoords){
        actorCoords.set(stageX, stageY);
        actor.stageToLocalCoordinates(actorCoords);
        return actorCoords;
    }

    /** Returns true of this event is a touchUp triggered by {@link Scene#cancelTouchFocus()}. */
    public boolean isTouchFocusCancel(){
        return stageX == Integer.MIN_VALUE || stageY == Integer.MIN_VALUE;
    }

    @Override
    public String toString(){
        return type.toString();
    }

    /** Types of low-level input events supported by scene2d. */
    public enum Type{
        /** A new touch for a pointer on the stage was detected */
        touchDown,
        /** A pointer has stopped touching the stage. */
        touchUp,
        /** A pointer that is touching the stage has moved. */
        touchDragged,
        /** The mouse pointer has moved (without a mouse button being active). */
        mouseMoved,
        /** The mouse pointer or an active touch have entered (i.e., {@link Element#hit(float, float, boolean) hit}) an actor. */
        enter,
        /** The mouse pointer or an active touch have exited an actor. */
        exit,
        /** The mouse scroll wheel has changed. */
        scrolled,
        /** A keyboard key has been pressed. */
        keyDown,
        /** A keyboard key has been released. */
        keyUp,
        /** A keyboard key has been pressed and released. */
        keyTyped
    }
}
