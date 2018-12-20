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

package io.anuke.arc.input;

import io.anuke.arc.ApplicationListener;
import io.anuke.arc.Input;

/**
 * An InputProcessor is used to receive input events from the keyboard and the touch screen (mouse on the desktop). For this it
 * has to be registered with the {@link Input#setInputProcessor(InputProcessor)} method. It will be called each frame before the
 * call to {@link ApplicationListener#update()}. Each method returns a boolean in case you want to use this with the
 * {@link InputMultiplexer} to chain input processors.
 * @author mzechner
 */
public interface InputProcessor{
    /**
     * Called when a key was pressed
     * @return whether the input was processed
     */
    default boolean keyDown(KeyCode keycode){
        return false;
    }

    /**
     * Called when a key was released
     * @return whether the input was processed
     */
    default boolean keyUp(KeyCode keycode){
        return false;
    }

    /**
     * Called when a key was typed
     * @param character The character
     * @return whether the input was processed
     */
    default boolean keyTyped(char character){
        return false;
    }

    /**
     * @param screenX The x coordinate, origin is in the upper left corner
     * @param screenY The y coordinate, origin is in the upper left corner
     * @param pointer the pointer for the event.
     * @param button the button
     * @return whether the input was processed
     */
    default boolean touchDown(int screenX, int screenY, int pointer, KeyCode button){
        return false;
    }

    /**
     * @param pointer the pointer for the event.
     * @param button the button
     * @return whether the input was processed
     */
    default boolean touchUp(int screenX, int screenY, int pointer, KeyCode button){
        return false;
    }

    /**
     * Called when a finger or the mouse was dragged.
     * @param pointer the pointer for the event.
     * @return whether the input was processed
     */
    default boolean touchDragged(int screenX, int screenY, int pointer){
        return false;
    }

    /**
     * @return whether the input was processed
     */
    default boolean mouseMoved(int screenX, int screenY){
        return false;
    }

    /**
     * Called when the mouse wheel was scrolled. Will not be called on iOS.
     * @param amountX the horizontal scroll amount, negative or positive depending on the direction the wheel was scrolled.
     * @param amountY the vertical scroll amount, negative or positive depending on the direction the wheel was scrolled.
     * @return whether the input was processed.
     */
    default boolean scrolled(float amountX, float amountY){
        return false;
    }
}
